package jmg.io;

import java.io.IOException;
import java.io.InputStream;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java4unix.pluginchain.PluginConfig;
import jmg.Digraph;
import jmg.Utils;
import jmg.Vertex2LabelMap;
import toools.io.BinaryReader;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;
import toools.util.Conversion;

public class JMGReader
{
	public static class Plugin extends DatasetReaderPlugin
	{
		public Directory from;
		public boolean useLabels;

		@Override
		public Digraph read()
		{
			return readDirectory(from, nbThreads, useLabels);
		}

		@Override
		public void setup(PluginConfig parms)
		{
			super.setup(parms);

			if (parms.contains("useLabels"))
			{
				useLabels = parms.getBoolean("useLabels");
			}
		}
	}

	public static Digraph readDirectory(Directory from, int nbThreads, boolean useLabels)
	{
		try
		{
			JMGDataset dataset = new JMGDataset(from);
			LongProcess loading = new LongProcess("loading graph", - 1);
			Digraph g = new Digraph();
			g.origin = dataset;

			if (useLabels && dataset.label2vertexFile.exists())
			{
				g.label2vertex = Conversion
						.toIntArray(dataset.label2vertexFile.readValues(nbThreads));

				assert new IntOpenHashSet(g.label2vertex)
						.size() == g.label2vertex.length : new IntOpenHashSet(
								g.label2vertex).size() + " != " + g.label2vertex.length;

				g.vertex2label = new Vertex2LabelMap(g.label2vertex);
			}

			int nbVertices = dataset.getNbVertex();

			if (dataset.ADJFile.exists())
			{
				int[][] adj = readADJ(dataset, nbVertices, nbThreads);

				String adjType = dataset.getProperties().getProperty("adjType");

				if (adjType.equals("in"))
				{
					g.in = adj;
				}
				else
				{
					g.out = adj;
				}

			}

			loading.end();
			return g;
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public static int[][] readADJ(JMGDataset dataset, int nbVertex, int nbThreads)
			throws IOException
	{
		RegularFile adjFile = JMGDataset.getADJFile(dataset.directory);

		int[] degrees = Conversion.toIntArray(
				JMGDataset.getDegreesFile(dataset.directory).readValues(nbThreads));

		long[] index = JMGDataset.getIndexFile(dataset.directory).readValues(nbThreads);

		int[][] adj = new int[nbVertex][];
		LongProcess reading = new LongProcess(
				"reading " + adjFile + " using " + nbThreads + " threads", "B",
				adjFile.getSize());

		new ParallelIntervalProcessing(nbVertex, nbThreads)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
					throws IOException
			{
				InputStream _in = adjFile.createReadingStream(0);
				_in.skip(index[lowerBound]);
				BinaryReader _r = new BinaryReader(_in, 65530 * 256);
				int nbBytesRead = 0;

				for (int _u = lowerBound; _u < upperBound; ++_u)
				{
					int _nbNeighbor = degrees[_u];

					if (_nbNeighbor == 0)
					{
						adj[_u] = Utils.emptyArray;
					}
					else
					{
						int[] _neighbors = new int[_nbNeighbor];
						_neighbors[0] = Conversion.long2int(_r.next(8));
						nbBytesRead += 8;

						if (_nbNeighbor > 1)
						{
							int encoding = (int) _r.next(1);
							int previous = _neighbors[0];

							for (int i = 1; i < _nbNeighbor; ++i)
							{
								long delta = _r.next(encoding);
								long neighbor = previous + delta;
								previous = _neighbors[i] = Conversion.long2int(neighbor);
							}

							nbBytesRead += 1 + (_nbNeighbor - 1) * encoding;

							if (nbBytesRead > 1000000)
							{
								reading.progressStatus.addAndGet(1000000);
								nbBytesRead -= 1000000;
							}
						}

						adj[_u] = _neighbors;
					}
				}
			}
		};

		reading.end();
		return adj;
	}
}
