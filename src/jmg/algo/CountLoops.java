package jmg.algo;

import java.util.concurrent.atomic.AtomicLong;

import jmg.Digraph;
import jmg.Utils;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class CountLoops
{
	public static long count(Digraph g)
	{
		AtomicLong count = new AtomicLong(0);
		LongProcess p = new LongProcess("count loops", g.getNbVertex());

		new ParallelIntervalProcessing(g.getNbVertex())
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					if (Utils.contains(g.out[u], u))
					{
						count.incrementAndGet();
					}

					if (u % 100 == 0)
						p.progressStatus.addAndGet(100);
				}
			}
		};

		p.end();
		return count.get();
	}
}
