package jmg.algo;

import jmg.Digraph;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class CountMultiArcs
{
	public static long count(Digraph g, int nbThreads)
	{
		long[] count = new long[1];
		LongProcess p = new LongProcess("count loops", " elements", g.getNbVertices());

		new ParallelIntervalProcessing(g.getNbVertices(), nbThreads, p)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				int[][] adj = g.out == null ? g.in.adj : g.out.adj;

				if (adj == null)
					throw new IllegalStateException("there is no ADJ in the graph");

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

					++s.progressStatus;
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
