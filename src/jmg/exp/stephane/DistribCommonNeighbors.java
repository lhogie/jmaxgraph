package jmg.exp.stephane;

import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrays;
import jmg.Graph;
import jmg.JmgUtils;
import jmg.io.jmg.JMGDirectory;
import toools.SystemMonitor;
import toools.io.CSVStream;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;

public class DistribCommonNeighbors
{
	public static void main(String[] args)
	{
		SystemMonitor.defaultMonitor.start();
		String path = "$HOME/datasets/twitter/big.jmg";

		if (args.length > 0)
			path = args[0];

		JMGDirectory d = new JMGDirectory(path);
		int nbThreads = Runtime.getRuntime().availableProcessors();
		Graph g = new Graph(d, false, nbThreads);

		int[] inDegrees = g.in.degrees();

		int[] verticesSortedByInDegree = JmgUtils.sortVerticesBy(inDegrees);
		inDegrees = null;

		int n = 1000;

		int[] verticesWithLargerDegrees = IntArrays.copy(verticesSortedByInDegree,
				verticesSortedByInDegree.length - n, n);
		IntArrays.shuffle(verticesWithLargerDegrees, new Random());
		verticesSortedByInDegree = null;

		g.in.ensureLoaded(8);

		CSVStream out = new RegularFile("nbOfCommonInNeighborsForBigVertices.csv")
				.createCSVStream("u", "d_in(u)", "v", "d_in(v)", "in(u).inter(in(v)).size()");

		LongProcess lp = new LongProcess("computing distribution", " couple",
				Math.pow(verticesWithLargerDegrees.length, 2) / 2);

		new MultiThreadProcessing(nbThreads, lp)
		{
			@Override
			protected void runInParallel(ThreadSpecifics s, List<Thread> threads)
			{
				for (int u : verticesWithLargerDegrees)
				{
					int destinationThread = u % s.threads.size();

					if (destinationThread == s.rank)
					{
						int[] Nu = g.in.mem.get(u);

						for (int v : verticesWithLargerDegrees)
						{
							if (u < v)
							{
								int[] Nv = g.in.mem.get(v);
								int nbCommonNeighbors = JmgUtils.sizeOfIntersection(Nu,
										Nv);

								synchronized (out)
								{
									out.println(u, Nu.length, v, Nv.length,
											nbCommonNeighbors);
								}

								s.progressStatus++;
							}
						}
					}
				}
			}

		};

		out.close();
		lp.end();

	}

	private static void seen(boolean[] seen, int[] s)
	{
		for (int u : s)
		{
			seen[u] = true;
		}
	}
}
