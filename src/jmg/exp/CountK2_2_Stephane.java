package jmg.exp;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.Utils;
import jmg.io.JMGReader;
import toools.io.file.Directory;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.thread.ParallelIntervalProcessing;

public class CountK2_2_Stephane
		implements TooolsPlugin<Digraph, CountK2_2_Stephane_Result>
{
	@Override
	public CountK2_2_Stephane_Result process(Digraph g)
	{
		g.ensureBothDirections();

		int nbVertices = g.getNbVertex();

		LongProcess assigningWeights = new LongProcess("assigning weights",
				g.getNbVertex());

		int[] weigths = new int[nbVertices];

		new ParallelIntervalProcessing(g.getNbVertex())
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					int din = g.in[u].length;
					weigths[u] = (din * (din - 1)) / 2;

					if (u % 1000 == 0)
						assigningWeights.progressStatus.addAndGet(1000);
				}
			}
		};

		assigningWeights.end();

		long[] partialSums = Utils.partialSums(weigths);

		CountK2_2_Stephane_Result r = new CountK2_2_Stephane_Result();
		r.sumDegrees = partialSums[partialSums.length - 1];

		LongProcess l = new LongProcess("tracking K2,2 by Stephane", - 1);
		l.temporaryResult = r;

		new MultiThreadProcessing()
		{
			@Override
			protected void runInParallel(int rank, List<Thread> threads) throws Throwable
			{
				CountK2_2_Stephane_Result _r = new CountK2_2_Stephane_Result();

				while (true)
				{
					l.progressStatus.incrementAndGet();

					Random prng = new Random();
					int u = Utils.pick(partialSums, prng);
					int[] in = g.in[u];

					if (in.length >= 2)
					{
						int v1 = in[prng.nextInt(in.length)];
						int v2 = in[prng.nextInt(in.length - 1)];

						if (v2 == v1)
						{
							v2 = in[in.length - 1];
						}

						int nbCommonNeighbors = Utils.countElementsInCommon_dichotomic(
								g.out[v1], g.out[v2]) - 1;
						_r.nK22 += nbCommonNeighbors;
					}

					++_r.nbIteration;

					if (_r.nbIteration % 1000 == 0)
					{
						synchronized (r)
						{
							r.nbIteration += _r.nbIteration;
							_r.nbIteration = 0;
							r.nK22 += _r.nK22;
							_r.nK22 = 0;
						}

						// return;
					}
				}
			}
		};

		l.end();
		return r;
	}

	@Override
	public void setup(PluginConfig p)
	{
	}

	public static void main(String[] args) throws IOException
	{
		Directory d = new Directory("$HOME/datasets/sample-0.01b.jmg");
		Digraph g = JMGReader.readDirectory(d, 8, false);
		new CountK2_2_Stephane().process(g);
	}
}
