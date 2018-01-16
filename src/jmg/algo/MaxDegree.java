package jmg.algo;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import toools.thread.ParallelIntervalProcessing;

public class MaxDegree
{

	public static int computeMaxDegree_seq(int[][] adj)
	{
		int max = - 1;

		for (int[] s : adj)
		{
			int sz = s.length;

			if (sz > max)
			{
				max = sz;
			}
		}

		return max;
	}

	public static int computeMaxDegree_par(int[][] adj)
	{
		IntAVLTreeSet localMaximums = new IntAVLTreeSet();

		new ParallelIntervalProcessing(adj.length)
		{

			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				int max = - 1;

				for (int v = 0; v < upperBound; ++v)
				{
					int degree = adj[v].length;

					if (degree > max)
					{
						max = degree;
					}
				}

				synchronized (localMaximums)
				{
					localMaximums.add(max);
				}
			}
		};

		return localMaximums.lastInt();
	}

}
