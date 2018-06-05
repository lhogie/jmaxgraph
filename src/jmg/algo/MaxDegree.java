package jmg.algo;

import java.util.Collections;
import java.util.Vector;

import toools.thread.MultiThreadProcessing.ThreadSpecifics;
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

	public static int computeMaxDegree_par(int[][] adj, int nbThreads)
	{
		Vector<Integer> degrees = new Vector<>();

		new ParallelIntervalProcessing(adj.length, nbThreads, null)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				int max = - 1;
				int degree;

				for (int v = lowerBound; v < upperBound; ++v)
				{
					degree = adj[v].length;

					if (degree > max)
					{
						max = degree;
					}
				}

				degrees.set(s.rank, max);
			}
		};

		return Collections.max(degrees);
	}

}
