package jmg.gen;

import java.util.Random;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.Utils;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class GNPGenerator_quick
{
	public static class Plugin implements TooolsPlugin<Void, Digraph>
	{
		public double p = 0.5;
		public int nbVertex = 1000;
		public boolean acceptLoops = true;
		public Random r = new Random();

		@Override
		public Digraph process(Void v)
		{
			Digraph g = new Digraph();
			g.out = random(nbVertex, p, r, acceptLoops);
			return g;
		}

		@Override
		public void setup(PluginConfig p)
		{
			nbVertex = p.getInt("n");
			this.p = p.getDouble("p");
			this.r = new Random(p.getInt("seed"));
		}
	}

	public static int[][] random(int nbVertex, double p, Random r, boolean acceptLoops)
	{
		final int seed = r.nextInt();
		LongProcess lp = new LongProcess("generating GNP graph", nbVertex);
		int[][] adj = new int[nbVertex][];
		int[] vertexSequence = new int[nbVertex];

		for (int v = 0; v < nbVertex; ++v)
		{
			vertexSequence[v] = v;
		}

		new ParallelIntervalProcessing(nbVertex)
		{

			@Override
			protected void process(int rank, int lowerBound, int upperBound)
					throws Throwable
			{
				Random lr = new Random(seed + rank);

				for (int v = lowerBound; v < upperBound; ++v)
				{
					lp.progressStatus.incrementAndGet();
					int nbNeighbors = (int) (nbVertex * p);
					int[] neighbors = adj[v] = new int[nbNeighbors];

					for (int i = 0; i < nbNeighbors; ++i)
					{
						int randomIndex = lr.nextInt(vertexSequence.length - i);
						neighbors[i] = vertexSequence[randomIndex];

						vertexSequence[randomIndex] = vertexSequence[vertexSequence.length
								- i - 1];
						vertexSequence[vertexSequence.length - i - 1] = neighbors[i];
					}
				}

			}
		};
		lp.end();
		Utils.ensureSorted(adj);
		return adj;
	}

}
