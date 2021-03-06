package jmg.algo;

import j4u.chain.PluginParms;
import jmg.plugins.JMGPlugin;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class Degrees
{
	public static class Plugin extends JMGPlugin<int[][], int[]>
	{

		@Override
		public int[] process(int[][] adj)
		{
			return computeDegrees(adj, nbThreads);
		}

		@Override
		public void setParameters(PluginParms p)
		{
		}

	}

	private static int nbThreads;

	public static int[] computeDegrees(int[][] adj, int nbThreads)
	{
		LongProcess computeDegrees = new LongProcess("computeDegrees", " vertex",
				adj.length);
		int[] r = new int[adj.length];

		new ParallelIntervalProcessing(r.length, nbThreads, computeDegrees)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int v = lowerBound; v < upperBound; ++v)
				{
					r[v] = adj[v].length;
				}
			}
		};

		computeDegrees.end();
		return r;
	}



	public static double stdDev(int[][] adj)
	{
		return MathsUtilities.stdDev(computeDegrees(adj, nbThreads));
	}

}
