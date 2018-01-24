package jmg.algo;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class Degrees
{
	public static class Plugin implements TooolsPlugin<int[][], int[]>
	{

		@Override
		public int[] process(int[][] adj)
		{
			return computeDegrees(adj);
		}

		@Override
		public void setup(PluginConfig p)
		{
		}

	}

	
	public static int[] computeDegrees(int[][] adj)
	{
		LongProcess computeDegrees = new LongProcess("computeDegrees", adj.length);
		int[] r = new int[adj.length];

		new ParallelIntervalProcessing(r.length)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
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

	public static int maxDegree(int[][] adj)
	{
		return MathsUtilities.max(computeDegrees(adj));
	}

}
