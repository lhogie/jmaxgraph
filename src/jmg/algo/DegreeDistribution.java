package jmg.algo;

import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import toools.progression.LongProcess;

public class DegreeDistribution
{
	public static Int2ObjectMap<AtomicInteger> getDegreeDistribution(int[][] adj)
	{
		LongProcess pm = new LongProcess("computing degree distribution", adj.length);
		Int2ObjectMap<AtomicInteger> degreeDistribution = new Int2ObjectAVLTreeMap<>();

		for (int[] neighbors : adj)
		{
			pm.progressStatus.incrementAndGet();

			int degree = neighbors.length;

			if (degreeDistribution.containsKey(degree))
			{
				degreeDistribution.get(degree).incrementAndGet();
			}
			else
			{
				degreeDistribution.put(degree, new AtomicInteger(0));
			}
		}

		pm.end();
		return degreeDistribution;
	}
}
