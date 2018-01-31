package jmg.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import jmg.Digraph;
import toools.io.BinaryReader;
import toools.io.Cout;
import toools.io.DataBinaryEncoding;
import toools.io.file.RegularFile;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.util.Conversion;

public class BigGrphReader
{
	public static Digraph load(List<RegularFile> files) throws IOException
	{
		int[] nbEntries = countEntries(files);
		int totalNbEntry = Conversion.long2int(MathsUtilities.sum(nbEntries));
		Cout.result("total number of vertices in " + files.size() + " files: "
				+ totalNbEntry);
		Digraph g = new Digraph();
		g.out.adj = new int[totalNbEntry][];
		int[] label2vertex = new int[totalNbEntry];
		Arrays.fill(label2vertex, - 2);

		LongProcess pm = new LongProcess("loading files", totalNbEntry);

		new MultiThreadProcessing(files.size(), pm)
		{
			@Override
			protected void runInParallel(ThreadSpecifics s, List<Thread> threads) throws Throwable
			{
				loadBigrphFile(files.get(s.rank), g, getOffset(s.rank), label2vertex, pm);
			}

			private int getOffset(int rank)
			{
				int offset = 0;

				for (int i = 0; i < rank; ++i)
				{
					offset += nbEntries[i];
				}

				return offset;
			}
		};

		System.out.println("ok");

		pm.end();
		return g;
	}

	private static void loadBigrphFile(RegularFile f, Digraph g, int offset,
			int[] label2vertex, LongProcess pm) throws IOException
	{
		Cout.progress("loading file " + f);

		InputStream is = f.createReadingStream();
		// FastReader ls = new BinaryReader(new ParallelBlockReader(is, 1000000,
		// 3));
		byte[] b = new byte[8];

		is.read(b, 0, 4);
		int nbVerticesInFile = DataBinaryEncoding.readInt(b, 0);

		for (int entryIndex = 0; entryIndex < nbVerticesInFile; ++entryIndex)
		{
			int label = offset++;

			is.read(b, 0, 8);
			int v = DataBinaryEncoding.readInt(b, 0);
			if (403976281 == v)
				System.out.println("COUCOUCOUCOUCOUCCO");

			assert label2vertex[label] == - 2;
			label2vertex[label] = v;

			is.read(b, 0, 4);
			int nbNeighbors = DataBinaryEncoding.readInt(b, 0);
			int[] inNeighbors = new int[nbNeighbors];
			g.out.adj[label] = inNeighbors;

			++pm.sensor.progressStatus;

			is.read(b, 0, 1);
			boolean is32bit = DataBinaryEncoding.readBoolean(b, 0);

			if (is32bit)
			{
				for (int i = 0; i < nbNeighbors; ++i)
				{
					is.read(b, 0, 4);
					int dest = DataBinaryEncoding.readInt(b, 0);
					inNeighbors[i] = dest;
				}
			}
			else
			{
				for (int i = 0; i < nbNeighbors; ++i)
				{
					is.read(b, 0, 8);
					int dest = (int) DataBinaryEncoding.readLong(b, 0);
					inNeighbors[i] = dest;
				}
			}
		}
	}

	public static int[] countEntries(List<RegularFile> files)
			throws FileNotFoundException, IOException
	{
		int[] r = new int[files.size()];
		int i = 0;

		for (RegularFile f : files)
		{
			InputStream is = f.createReadingStream();
			int nbEntry = new BinaryReader(is, 65536 * 256).nextInt();
			r[i++] = nbEntry;
			is.close();
		}

		return r;
	}

}
