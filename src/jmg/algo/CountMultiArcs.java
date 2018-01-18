package jmg.algo;

import jmg.Digraph;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class CountMultiArcs
{
	public static long count(Digraph g)
	{
		long[] count = new long[1];
		LongProcess p = new LongProcess("count loops", g.getNbVertex());

		new ParallelIntervalProcessing(g.getNbVertex())
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				int[][] adj = g.getRefAdj();
				long _count = 0;

				for (int u = lowerBound; u < upperBound; ++u)
				{
					int[] N = adj[u];
					int sz = N.length;

					if (sz > 1)
					{
						int previous = N[0];

						for (int i = 1; i < sz; ++i)
						{
							int current = N[i];

							if (current == previous)
							{
								++_count;
							}

							previous = current;
						}
					}

					if (u % 100 == 0)
						p.progressStatus.addAndGet(100);
				}

				synchronized (count)
				{
					count[0] += _count;
				}
			}
		};

		p.end();
		return count[0];
	}
}
