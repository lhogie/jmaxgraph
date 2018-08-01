package jmg.gen;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.MatrixAdj;
import jmg.plugins.JMGPlugin;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class DirectedGNP
{

	public static int[][] out(int nbVertex, double p, Random prng, boolean allowLoops,
			int nbThreads)
	{
		LongProcess lp = new LongProcess("generating GNP graph", " adj-list", nbVertex);

		int[][] r = new int[nbVertex][];

		new ParallelIntervalProcessing(nbVertex, nbThreads, lp)
		{

			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
					throws Throwable
			{
				ThreadLocalRandom random = ThreadLocalRandom.current();
				int[] tmp = new int[(int) (nbVertex * p) + 1];

				for (int u = lowerBound; u < upperBound; ++u)
				{
					int nbNeighbors = 0;

					for (int v = 0; v < nbVertex; ++v)
					{
						if (allowLoops || u != v)
						{
							if (random.nextDouble() < p)
							{
								// if the tmp array is too small, double it
								if (tmp.length == nbNeighbors)
								{
									tmp = new int[tmp.length * 2];
								}

								tmp[nbNeighbors++] = v;
							}
						}
					}

					r[u] = Arrays.copyOf(tmp, nbNeighbors);
					s.progressStatus++;
				}
			}
		};

		lp.end();
		return r;
	}

	public static class Plugin extends JMGPlugin<Void, Graph>
	{
		public double p = 0.5;
		public int nbVertex = 1000;
		public Random r = new Random();
		private boolean allowLoops = false;

		@Override
		public Graph process(Void v)
		{
			Graph g = new Graph();
			g.out.mem = new MatrixAdj(out(nbVertex, p, r, allowLoops, nbThreads), null,
					nbThreads);
			return g;
		}

		@Override
		public void setParameters(PluginParms p)
		{
			super.setParameters(p);
			nbVertex = p.getInt("n");
			this.p = p.getDouble("p");
			this.allowLoops = p.getBoolean("allowLoops");

			long seed = p.contains("seed") ? p.getInt("seed")
					: System.currentTimeMillis();
			this.r = new Random(seed);

		}
	}
}
