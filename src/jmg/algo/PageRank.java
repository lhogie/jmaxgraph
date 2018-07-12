package jmg.algo;

import java.util.List;
import java.util.Random;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.chain.JMGPlugin;
import toools.io.Cout;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;

public class PageRank
{
	public static class PG extends JMGPlugin<Graph, int[]>
	{
		int walkLength, nbParallelWalks;

		@Override
		public int[] process(Graph in)
		{
			return f(in, new Random(), walkLength, nbParallelWalks);
		}

		@Override
		public void setParameters(PluginParms p)
		{
			super.setParameters(p);
			this.walkLength = p.getInt("wl");
			this.nbParallelWalks = p.getInt("n");
		}
	}

	public static int[] f(Graph g, Random r, int walkLength, int nbThreads)
	{
		g.out.ensureLoaded(nbThreads);
		int nbVertices = g.getNbVertices();
		final int[] ranks = new int[nbVertices];
		LongProcess lp = new LongProcess("page ranking", " iteration", walkLength);
		final int end = walkLength / nbThreads;

		new MultiThreadProcessing(nbThreads, lp)
		{
			@Override
			protected void runInParallel(ThreadSpecifics s, List<Thread> threads)
					throws Throwable
			{
				int u = r.nextInt();
				int[] adj;

				for (int l = 0; l < end; ++l)
				{
					s.progressStatus = l;

					while ((adj = g.out.mem.b[u]).length == 0)
					{
						u = r.nextInt(nbVertices);
					}

					++ranks[u = adj[r.nextInt(adj.length)]];
				}
			}
		};

		Cout.debug(walkLength * nbThreads, MathsUtilities.sum(ranks));
		return ranks;
	}
}
