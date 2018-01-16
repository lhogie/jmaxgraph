package jmg.algo;

import java.util.concurrent.atomic.AtomicLong;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class CountBidirectionalArcs
{
	public static long count(Digraph g)
	{
		LongProcess p = new LongProcess("count bidirectional arcs", g.getNbVertex());
		AtomicLong count = new AtomicLong(0);

		new ParallelIntervalProcessing(g.getNbVertex())
		{

			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					for (int v : g.out[u])
					{
						if (g.exists(v, u))
						{
							count.incrementAndGet();
						}
					}

					p.progressStatus.incrementAndGet();
				}
			}
		};

		p.end();
		return count.get();
	}

	public static class Plugin implements TooolsPlugin<Digraph, Long>
	{
		@Override
		public Long process(Digraph in)
		{
			return count(in);
		}

		@Override
		public void setup(PluginConfig p)
		{
		}

	}
}
