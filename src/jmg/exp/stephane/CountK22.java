package jmg.exp.stephane;

import java.io.IOException;
import java.util.Random;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.JmgUtils;
import jmg.chain.JMGPlugin;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class CountK22 extends JMGPlugin<Graph, CountK22_Result>
{
	long printEach;
	long maxIteration;

	@Override
	public CountK22_Result process(Graph g)
	{
		return count(g, - 1);
	}

	@Override
	public void setParameters(PluginParms p)
	{
		printEach = p.getLong("printEach", 1);
		maxIteration = p.getLong("maxIteration", - 1);
	}

	public CountK22_Result count(Graph g, int stopIteration)
	{
		g.in.ensureLoaded(nbThreads);

		int nbVertices = g.getNbVertices();

		LongProcess assigningWeights = new LongProcess("assigning weights", " vertex",
				nbVertices);

		long[] weigths = new long[nbVertices];

		new ParallelIntervalProcessing(nbVertices, nbThreads, assigningWeights)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					long din = g.in.mem.b[u].length;

					if (din > 0)
					{
						weigths[u] = (din * (din - 1)) / 2;

						if (weigths[u] < 0)
							throw new IllegalStateException(
									"generating negative weights. Long is not long enough.");
					}

					++s.progressStatus;
				}
			}
		};

		assigningWeights.end();
		Random prng = new Random();

		long[] partialSums = MathsUtilities.partialSums(weigths);

		int[] v1s = new int[stopIteration];
		int[] v2s = new int[stopIteration];

		LongProcess picking = new LongProcess("picking " + stopIteration + " couples",
				" couple", stopIteration);

		for (int iteration = 0; iteration != stopIteration; ++iteration)
		{
			picking.sensor.progressStatus = iteration;
			int u = MathsUtilities.pick(partialSums, prng);
			int[] in = g.in.mem.b[u];

			if (in.length >= 2)
			{
				int v1 = in[prng.nextInt(in.length)];
				int v2 = in[prng.nextInt(in.length - 1)];

				if (v2 == v1)
				{
					v2 = in[in.length - 1];
				}

				v1s[iteration] = v1;
				v2s[iteration] = v2;
			}
			else
			{
				--iteration;
			}
		}

		picking.end();

		g.in.mem.b = null;
		System.gc();
		g.out.ensureLoaded(nbThreads);


		LongProcess l = new LongProcess("tracking K2,2 by Stephane", " couple",
				stopIteration);

		for (int iteration = 0; iteration != stopIteration; ++iteration)
		{
			l.sensor.progressStatus = iteration;

			int v1 = v1s[iteration];
			int v2 = v2s[iteration];

			int[] adjV1 = g.out.mem.b[v1];
			int[] adjV2 = g.out.mem.b[v2];

			int nbCommonNeighbors = JmgUtils.countElementsInCommon_dichotomic(adjV1, adjV2)
					- 1;

			long nbPotentialK22 = adjV1.length + adjV2.length - 2;

			if (g.arcExists(v1, v2))
			{
				--nbPotentialK22;
			}

			if (g.arcExists(v2, v1))
			{
				--nbPotentialK22;
			}

			Cout.result("forsteph " + nbCommonNeighbors + " " + nbPotentialK22);
			//Cout.result(r.forSteph());
		}

		l.end();

		return null;
	}

	public static void main(String[] args) throws IOException
	{
		JMGDirectory d = new JMGDirectory(args[0]);
		Graph g = d.mapGraph(8, false);
		int maxIteration = Integer.valueOf(args[1]);
		new CountK22().count(g, maxIteration);
		Cout.info("completed");
	}
}
