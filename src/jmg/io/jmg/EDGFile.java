package jmg.io.jmg;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import jmg.Utils;
import jmg.io.jmg.EDGFileVertexIterator.EDGFileCursor;
import toools.io.Bits;
import toools.io.IORuntimeException;
import toools.io.NBSFile;
import toools.io.Utilities;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.util.Conversion;

public class EDGFile extends RegularFile
		implements Iterable<EDGFileVertexIterator.EDGFileCursor>
{
	private long[] index;

	public EDGFile(Directory d, String name)
	{
		super(d, name);
	}

	public long getIndexOf(int u)
	{
		return getIndex(8)[u];
	}

	public NBSFile getIndexFile()
	{
		return new NBSFile(getParent(), getNameWithoutExtension() + "-index.nbs");
	}

	public int[] readEntry(int u)
	{
		// use short buffer here
		EDGFileVertexIterator rr = new EDGFileVertexIterator(EDGFile.this, u, u + 1, 0,
				256);
		return rr.next().adj;
	}

	public void loadIndex(int nbThreads)
	{
		try
		{
			this.index = getIndexFile().readValues(nbThreads);
		}
		catch (IOException e)
		{
			throw new IORuntimeException(e);
		}
	}

	synchronized long[] getIndex(int nbThreads)
	{
		if (index == null)
		{
			loadIndex(nbThreads);
		}

		return index;
	}

	public void writeADJ(int[][] adj) throws IOException
	{
		LongProcess saving = new LongProcess("saving ADJ", " adj-list", adj.length);

		OutputStream os = createWritingStream(false, 1024 * 1024 * 1024);

		byte[] b = new byte[8];

		int nbVertex = adj.length;
		long[] index = new long[nbVertex];

		// tracks the index for each entry
		long pos = 0;

		for (int u = 0; u < nbVertex; ++u)
		{
			index[u] = pos;

			// write the number of neighbors
			int[] neighbors = adj[u];

			if (neighbors.length > 0)
			{
				// writes the first neighbor
				Bits.putLong(b, 0, neighbors[0]);
				int previous = neighbors[0];
				os.write(b, 0, 8);
				pos += 8;

				if (neighbors.length > 1)
				{
					// write the encoding for the other neighbors
					int encoding = Utilities.getNbBytesRequireToEncode(maxgap(neighbors));
					Bits.putLong(b, 0, encoding, 1);
					os.write(b, 0, 1);
					pos += 1;

					// write other neighbors
					for (int i = 1; i < neighbors.length; ++i)
					{
						int v = neighbors[i];
						int delta = v - previous;
						Bits.putLong(b, 0, delta, encoding);
						os.write(b, 0, encoding);
						pos += encoding;
						previous = v;
					}
				}
			}

			++saving.progressStatus;
		}

		os.close();

		getIndexFile().saveValues(index);
		saving.end();
	}

	private static int maxgap(int[] a)
	{
		int r = 0;

		for (int i = 1; i < a.length; ++i)
		{
			int diff = a[i] - a[i - 1];
			assert diff >= 0;

			if (diff > r)
				r = diff;
		}

		return r;
	}

	@Override
	public Iterator<EDGFileCursor> iterator()
	{
		return iterator(0, Integer.MAX_VALUE, 0);
	}

	public Iterator<EDGFileCursor> iterator(int from, int to, int nbPreallocatedArrays)
	{
		return new EDGFileVertexIterator(this, from, to, nbPreallocatedArrays,
				65536 * 256);
	}

	public int getNbEntries()
	{
		return Conversion.long2int(getIndexFile().getNbValues());
	}

	public static interface VertexListener
	{
		void vertexFound(EDGFileCursor c);
	}

	public void process_seq(String processName, int nbPreallocatedArrays,
			VertexListener p)
	{
		int nbVertices = getNbEntries();
		LongProcess compute = new LongProcess(processName + " from file " + this,
				nbVertices);

		Iterator<EDGFileCursor> iterator = iterator(0, nbVertices, nbPreallocatedArrays);

		while (iterator.hasNext())
		{
			EDGFileCursor c = iterator.next();
			p.vertexFound(c);
			++compute.progressStatus;
		}

		compute.end();
	}

	public int[][] readADJ(int nbThreads)
	{
		// preload index
		loadIndex(nbThreads);
		LongProcess lp = new LongProcess(
				"loading " + this + " using " + nbThreads + " threads", "B", getSize());

		int nbVertex = getNbEntries();
		int[][] adj = new int[nbVertex][];

		new EDGParallelProcessor(this, 1000, 8)
		{
			@Override
			protected void process(int rank, Iterator<EDGFileCursor> iterator)
			{
				while (iterator.hasNext())
				{
					EDGFileCursor c = iterator.next();
					adj[c.vertex] = c.adj;
					lp.progressStatus += c.nbBytes;
				}
			}
		};

		lp.end();
		return adj;
	}

	public boolean exists(int u, int neighbor)
	{
		return Utils.contains(readEntry(u), neighbor);
	}

	public int[][] readAndComputeOppositeADJ(int nbThreads)
	{
		int nbVertex = getNbEntries();
		int[][] r = new int[nbVertex][];
		int[] pos = new int[nbVertex];
		int[] invDegree = computeReverseDegrees(true);

		process_seq("compute inv adj", 1000, new VertexListener()
		{
			@Override
			public void vertexFound(EDGFileCursor c)
			{
				for (int neighbor : c.adj)
				{
					int[] invAdjList = r[neighbor];

					if (invAdjList == null)
					{
						invAdjList = r[neighbor] = new int[invDegree[neighbor]];
					}

					invAdjList[pos[neighbor]++] = c.vertex;
				}
			}
		});

		// vertices that had no out-neighbors
		for (int u = 0; u < r.length; ++u)
		{
			if (r[u] == null)
			{
				r[u] = Utils.emptyArray;
			}
		}

		return r;
	}

	public int[] computeReverseDegrees(boolean write)
	{
		int nbVertices = getNbEntries();
		int[] degrees = new int[nbVertices];

		process_seq("compute reverse degrees", 1000, new EDGFile.VertexListener()
		{
			@Override
			public void vertexFound(EDGFileCursor c)
			{
				for (int v : c.adj)
				{
					++degrees[v];
				}
			}
		});

		if (write)
		{
			String adjType = getName().startsWith("out") ? "in" : "out";
			new NBSFile(getParent(), adjType + "-degrees.nbs").saveValues(degrees);
		}

		return degrees;
	}

	public int[] computeDegrees(boolean write)
	{
		int nbVertices = getNbEntries();
		int[] degrees = new int[nbVertices];
		LongProcess lp = new LongProcess("compute degrees", getSize());

		new EDGParallelProcessor(this, 1000, MultiThreadProcessing.NB_THREADS_TO_USE)
		{
			@Override
			protected void process(int rank, Iterator<EDGFileCursor> iterator)
			{
				while (iterator.hasNext())
				{
					EDGFileCursor c = iterator.next();
					degrees[c.vertex] = c.adj.length;
					++lp.progressStatus;
				}
			}
		};

		lp.end();

		if (write)
		{
			new NBSFile(getPath().replace(".edg", "-degrees.nbs")).saveValues(degrees);
		}

		return degrees;
	}

}
