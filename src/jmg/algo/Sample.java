package jmg.algo;

import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrays;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.Vertex2LabelMap;
import toools.io.Cout;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class Sample implements TooolsPlugin<Digraph, Digraph>
{
	public double p = 0.5;
	public long seed = System.currentTimeMillis();

	@Override
	public Digraph process(Digraph g)
	{
		Digraph sampleGraph = new Digraph();

		if (g.label2vertex != null)
		{
			sampleGraph.label2vertex = IntArrays.copy(g.label2vertex);
			sampleGraph.vertex2label = new Vertex2LabelMap(g.vertex2label);
		}

		if (g.out != null && g.in != null)
		{
			// free some memory before sample g.out
			g.in = null;
			g.out = sample(g.out);
			Cout.progress("Sampling completed, now needs to update IN ADJ");
			g.in = ReverseGraph.computeInverseADJ(g.out, true);
		}
		else if (g.out == null)
		{
			sampleGraph.in = sample(g.in);
		}
		else if (g.in == null)
		{
			sampleGraph.out = sample(g.out);
		}

		return sampleGraph;
	}

	private int[][] sample(int[][] adj)
	{
		LongProcess sampling = new LongProcess("sampling with p=" + p, adj.length);
		int[][] r = new int[adj.length][];

		new ParallelIntervalProcessing(adj.length)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				Random prng = new Random(rank + seed);

				for (int u = lowerBound; u < upperBound; ++u)
				{
					int[] retain = new int[adj[u].length];
					int nbRetained = 0;

					for (int v : adj[u])
					{
						if (prng.nextDouble() < p)
						{
							retain[nbRetained++] = v;
						}
					}

					r[u] = IntArrays.copy(retain, 0, nbRetained);
					sampling.progressStatus.incrementAndGet();
				}
			}
		};

		sampling.end();
		return r;
	}

	@Override
	public void setup(PluginConfig parms)
	{
		p = parms.getDouble("p");
	}

}
