package jmg.gen;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import jmg.JmgUtils;
import toools.progression.LongProcess;

public class ErdosRenyiFromNetworkit
{

	public static int[][] out(int nNodes, double prob)
	{
		LongProcess longProcess = new LongProcess("generating Erdos Renyi", "arcs",
				nNodes);

		int adjList[][] = new int[nNodes][];
		double log_cp = Math.log(1.0 - prob);
		Random random = ThreadLocalRandom.current();

		int curr = 0, next = - 1;

		while (curr < nNodes)
		{
			next += 1 + Math.floor(Math.log(1.0 - random.nextDouble()) / log_cp);

			while ((next >= curr) && (curr < nNodes))
			{
				next -= curr;
				curr++;
				longProcess.sensor.progressStatus++;
			}

			if (curr < nNodes)
			{
				int currNeib[] = adjList[curr];

				if (currNeib != null)
				{
					int extendedArray[] = Arrays.copyOf(currNeib, currNeib.length + 1);
					extendedArray[extendedArray.length - 1] = next;
					adjList[curr] = extendedArray;
				}
				else
				{
					adjList[curr] = new int[] { next };
				}
			}
		}

		while (nNodes-- > 0)
			if (adjList[nNodes] == null)
				adjList[nNodes] = JmgUtils.emptyArray;

		longProcess.end();

		return adjList;
	}

}
