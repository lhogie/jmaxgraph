package jmg.io.jmg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrays;
import jmg.Utils;
import jmg.io.jmg.ArcFileVertexIterator.ArcFileCursor;
import jmg.io.jmg.IndexFile.Bounds;
import toools.io.BinaryReader;
import toools.io.Bits;
import toools.io.IORuntimeException;
import toools.io.Utilities;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.io.file.nbs.NBSFile;
import toools.progression.LongProcess;
import toools.text.TextUtilities;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.util.Conversion;

public class ArcFile extends RegularFile
		implements Iterable<ArcFileVertexIterator.ArcFileCursor>
{
	private long[] index;

	public ArcFile(String name)
	{
		super(name);
	}

	public ArcFile(Directory d, String name)
	{
		super(d, name);
	}

	public long getIndexOf(int u)
	{
		return getIndex(8)[u];
	}

	public IndexFile getIndexFile()
	{
		return new IndexFile(getParent(), getNameWithoutExtension() + "-index.nbs", this);
	}

	public NBSFile getDegreeFile()
	{
		return new NBSFile(getParent(), getNameWithoutExtension() + "-degrees.nbs");
	}

	public static class Entry
	{
		public int[] adj;
		public Bounds bounds;

		@Override
		public String toString()
		{
			return bounds + " => " + adj.length + " neighbors as "
					+ TextUtilities.toString(adj);
		}
	}

	public Entry readEntry(int u)
	{
		Entry r = new Entry();

		try
		{
			r.bounds = getIndexFile().readBounds(u);

			if (r.bounds.length() == 0)
			{
				r.adj = Utils.emptyArray;
			}
			else
			{
				InputStream is = createReadingStream();
				is.skip(r.bounds.start);
				BinaryReader reader = new BinaryReader(is, 65536);
				r.adj = readADJList(r.bounds.length(), reader, new int[0][]);
			}

			return r;
		}
		catch (IOException e)
		{
			throw new IORuntimeException(e);
		}

	}

	public void loadIndex(int nbThreads)
	{
		this.index = getIndexFile().readValues(nbThreads);
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
		LongProcess saving = new LongProcess("writing " + this, " adj-list", adj.length);

		OutputStream os = createWritingStream(false, 1024 * 1024 * 1024);

		byte[] b = new byte[8];

		int nbVertex = adj.length;
		long[] index = new long[nbVertex];

		// tracks the index for each entry
		long pos = 0;

		for (int u = 0; u < nbVertex; ++u)
		{
			index[u] = pos;

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

			++saving.sensor.progressStatus;
		}

		os.close();
		getIndexFile().saveValues(index);
		saving.end();
	}

	static byte[] buf = new byte[8];

	public long writeADJ(int u, int[] neighbors, OutputStream os, long[] index)
			throws IOException
	{
		// tracks the index for each entry
		long nbBytes = 0;

		index[u] = nbBytes;

		if (neighbors.length > 0)
		{
			// writes the first neighbor
			Bits.putLong(buf, 0, neighbors[0]);
			int previous = neighbors[0];
			os.write(buf, 0, 8);
			nbBytes += 8;

			if (neighbors.length > 1)
			{
				// write the encoding for the other neighbors
				int encoding = Utilities.getNbBytesRequireToEncode(maxgap(neighbors));
				Bits.putLong(buf, 0, encoding, 1);
				os.write(buf, 0, 1);
				nbBytes += 1;

				// write other neighbors
				for (int i = 1; i < neighbors.length; ++i)
				{
					int v = neighbors[i];
					int delta = v - previous;
					Bits.putLong(buf, 0, delta, encoding);
					os.write(buf, 0, encoding);
					nbBytes += encoding;
					previous = v;
				}
			}
		}

		return nbBytes;
	}

	public static int maxgap(int[] a)
	{
		return maxgap(a, 0, a.length);
	}

	public static int maxgap(int[] a, int start, int len)
	{
		int r = 0;
		int end = start + len;

		for (int i = start + 1; i < end; ++i)
		{
			int diff = a[i] - a[i - 1];
			assert diff >= 0;

			if (diff > r)
				r = diff;
		}

		return r;
	}

	@Override
	public Iterator<ArcFileCursor> iterator()
	{
		return iterator(0, getNbEntries(), 0, 65536 * 256);
	}

	public ArcFileVertexIterator iterator(int from, int to, int nbPreallocatedArrays,
			int bufSize)
	{
		return new ArcFileVertexIterator(this, from, to, nbPreallocatedArrays, bufSize);
	}

	public int getNbEntries()
	{
		return Conversion.long2int(getIndexFile().getNbValues());
	}

	public static interface VertexListener
	{
		void vertexFound(ArcFileCursor c);
	}

	public int[][] readADJ(double p, long seed, int nbThreads)
	{
		LongProcess lp = new LongProcess(
				"loading " + this + " using " + nbThreads + " threads", " arc",
				getSize());

		int nbVertex = getNbEntries();
		int[][] adj = new int[nbVertex][];
		Random prng = new Random(seed);

		new ArcFileParallelProcessor(this, 0, nbVertex, 0, nbThreads, lp)
		{
			@Override
			protected void process(ThreadSpecifics s, Iterator<ArcFileCursor> iterator)
			{
				while (iterator.hasNext())
				{
					ArcFileCursor c = iterator.next();

					// no sampling
					if (p == 1)
					{
						adj[c.vertex] = c.adj;
					}
					else
					{
						int[] retain = new int[c.adj.length];
						int nbRetained = 0;

						for (int v : c.adj)
						{
							if (prng.nextDouble() < p)
							{
								retain[nbRetained++] = v;
							}
						}

						// if none is retained
						if (nbRetained == 0)
						{
							adj[c.vertex] = IntArrays.EMPTY_ARRAY;
						}
						// if all are retained
						else if (nbRetained == c.adj.length)
						{
							adj[c.vertex] = c.adj;
						}
						else
						{
							adj[c.vertex] = IntArrays.copy(retain, 0, nbRetained);
						}
					}
					
					s.progressStatus += c.adj.length;
				}
			}
		};

		lp.end();
		return adj;
	}

	public boolean exists(int u, int neighbor)
	{
		return Utils.contains(readEntry(u).adj, neighbor);
	}

	public int[][] readAndComputeOppositeADJ(int nbThreads)
	{
		int nbVertex = getNbEntries();
		int[][] r = new int[nbVertex][];
		int[] pos = new int[nbVertex];
		int[] invDegree = computeReverseDegrees(true);
		Iterator<ArcFileCursor> i = iterator();

		LongProcess lp = new LongProcess("computing opposite ADJ on the fly", " vertex",
				nbVertex);

		while (i.hasNext())
		{
			ArcFileCursor c = i.next();

			for (int neighbor : c.adj)
			{
				int[] invAdjList = r[neighbor];

				if (invAdjList == null)
				{
					invAdjList = r[neighbor] = new int[invDegree[neighbor]];
				}

				invAdjList[pos[neighbor]++] = c.vertex;
				++lp.sensor.progressStatus;
			}
		}

		lp.end();

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

		LongProcess lp = new LongProcess("computing inverse degrees from " + this,
				" adj-list", nbVertices);
		Iterator<ArcFileCursor> i = iterator();

		while (i.hasNext())
		{
			for (int v : i.next().adj)
			{
				++degrees[v];
			}

			++lp.sensor.progressStatus;
		}

		lp.end();

		if (write)
		{
			String targetAdjType = getName().startsWith("out") ? "in" : "out";
			new NBSFile(getParent(), targetAdjType + "-degrees.nbs").saveValues(degrees);
		}

		return degrees;
	}

	public int[] computeDegrees(boolean write, int nbThreads)
	{
		int nbVertices = getNbEntries();
		int[] degrees = new int[nbVertices];
		LongProcess lp = new LongProcess("compute degrees", " vertex", getSize());

		new ArcFileParallelProcessor(this, 0, getNbEntries(), 1000, nbThreads, lp)
		{
			@Override
			protected void process(ThreadSpecifics s, Iterator<ArcFileCursor> iterator)
			{
				while (iterator.hasNext())
				{
					ArcFileCursor c = iterator.next();
					degrees[c.vertex] = c.adj.length;
					++s.progressStatus;
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

	public static int[] readADJList(long nbBytes, BinaryReader reader,
			int[][] preallocatedArrays)
	{
		// if it has no neighbors
		if (nbBytes == 0)
		{
			return Utils.emptyArray;
		}
		else
		{
			int firstNeighbor = Conversion.long2int(reader.next(8));

			// if it has only one neighbor
			if (nbBytes == 8)
			{
				int[] adj = 1 < preallocatedArrays.length ? preallocatedArrays[1]
						: new int[1];
				adj[0] = firstNeighbor;
				return adj;
			}
			else
			{
				int encoding = (int) reader.next(1);

				if (encoding < 1 || encoding > 8)
					throw new IllegalStateException();

				if ((nbBytes - 9) % encoding > 0)
					throw new IllegalStateException();

				int nbNeighbor = 1 + Conversion.long2int((nbBytes - 9) / encoding);

				int[] neighbors = nbNeighbor < preallocatedArrays.length
						? preallocatedArrays[nbNeighbor]
						: new int[nbNeighbor];

				neighbors[0] = firstNeighbor;
				int previous = firstNeighbor;

				for (int i = 1; i < nbNeighbor; ++i)
				{
					long delta = reader.next(encoding);
					long neighbor = previous + delta;
					previous = neighbors[i] = Conversion.long2int(neighbor);
				}

				return neighbors;
			}
		}
	}

	public int[] getDegrees(int nbThreads)
	{
		NBSFile df = getDegreeFile();

		if (df.exists())
		{
			return df.readValuesAsInts(nbThreads);
		}
		else
		{
			return computeDegrees(true, nbThreads);
		}
	}

}
