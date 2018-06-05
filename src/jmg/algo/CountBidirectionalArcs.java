package jmg.algo;

import java.util.concurrent.atomic.AtomicLong;

import j4u.chain.PluginConfig;
import jmg.Digraph;
import jmg.chain.JMGPlugin;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class CountBidirectionalArcs
{
	public static long count(Digraph g, int nbThreads)
	{
		LongProcess p = new LongProcess("count bidirectional arcs", " vertex",
				g.getNbVertices());
		AtomicLong count = new AtomicLong(0);
		p.temporaryResult = count;

		new ParallelIntervalProcessing(g.getNbVertices(), nbThreads, p)
		{

			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					for (int v : g.out.mem.b[u])
					{
						if (v < u)
						{
							if (g.arcExists(v, u))
							{
								count.incrementAndGet();
							}
						}
					}

					++s.progressStatus;
				}
			}
		};

		p.end(count.toString());
		return count.get();
	}

	public static class Plugin extends JMGPlugin<Digraph, Long>
	{
		@Override
		public Long process(Digraph in)
		{
			return count(in, nbThreads);
		}

		@Override
		public void setup(PluginConfig p)
		{
		}

	}
}
