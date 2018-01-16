package jmg.algo;

import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrays;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.Vertex2LabelMap;
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
		sampleGraph.label2vertex = IntArrays.copy(g.label2vertex);
		sampleGraph.vertex2label = new Vertex2LabelMap(g.vertex2label);

		sampleGraph.out = new int[g.out.length][];
		LongProcess sampling = new LongProcess("sampling with p=" + p,
				sampleGraph.out.length);

		new ParallelIntervalProcessing(g.out.length)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				Random prng = new Random(rank + seed);

				for (int u = lowerBound; u < upperBound; ++u)
				{
					int[] retain = new int[g.out[u].length];
					int nbRetained = 0;

					for (int v : g.out[u])
					{
						if (prng.nextDouble() < p)
						{
							retain[nbRetained++] = v;
						}
					}

					sampleGraph.out[u] = IntArrays.copy(retain, 0, nbRetained);
					sampling.progressStatus.incrementAndGet();
				}
			}
		};

		sampling.end();
		return sampleGraph;
	}

	@Override
	public void setup(PluginConfig parms)
	{
		p = parms.getDouble("p");
	}

}
