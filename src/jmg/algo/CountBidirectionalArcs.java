package jmg.algo;

import java.util.concurrent.atomic.AtomicLong;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.chain.JMGPlugin;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class CountBidirectionalArcs
{
	public static long count(Graph g, int nbThreads)
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

	public static class Plugin extends JMGPlugin<Graph, Long>
	{
		@Override
		public Long process(Graph in)
		{
			return count(in, nbThreads);
		}

		@Override
		public void setParameters(PluginParms p)
		{
		}

	}
}
