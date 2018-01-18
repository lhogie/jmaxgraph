package jmg.exp.stephane;

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

public class CountK22
		implements TooolsPlugin<Digraph, CountK22_Result>
{

	@Override
	public CountK22_Result process(Digraph g)
	{
		return count(g);
	}

	public static CountK22_Result count(Digraph g)
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

		CountK22_Result r = new CountK22_Result();
		r.sumDegrees = partialSums[partialSums.length - 1];

		LongProcess l = new LongProcess("tracking K2,2 by Stephane", - 1);
		l.temporaryResult = r;

		new MultiThreadProcessing()
		{
			@Override
			protected void runInParallel(int rank, List<Thread> threads) throws Throwable
			{
				CountK22_Result _r = new CountK22_Result();

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

						_r.nbPotentialK22 += g.out[v1].length + g.out[v2].length
								- nbCommonNeighbors - 1;
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
							r.nbPotentialK22 += _r.nbPotentialK22;
							_r.nbPotentialK22 = 0;
						}
					}
				}
			}
		};

		// we counted each K22 twice
		r.nK22 /= 2;
		l.end();
		return r;
	}

	@Override
	public void setup(PluginConfig p)
	{
	}

	public static void main(String[] args) throws IOException
	{
		Directory d = new Directory("$HOME/datasets/twitter.jmg");
		Digraph g = JMGReader.readDirectory(d, 8, false);
		CountK22.count(g);
	}
}
