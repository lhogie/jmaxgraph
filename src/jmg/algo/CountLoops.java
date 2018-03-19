package jmg.algo;

import java.util.concurrent.atomic.AtomicLong;

import jmg.Digraph;
import jmg.Utils;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class CountLoops
{
	public static long count(Digraph g, int nbVertex)
	{
		AtomicLong count = new AtomicLong(0);
		LongProcess p = new LongProcess("count loops", " elements", g.getNbVertices());

		new ParallelIntervalProcessing(g.getNbVertices(), nbVertex, p)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					if (Utils.contains(g.out.adj[u], u))
					{
						count.incrementAndGet();
					}

					++s.progressStatus;
				}
			}
		};

		p.end();
		return count.get();
	}
}
