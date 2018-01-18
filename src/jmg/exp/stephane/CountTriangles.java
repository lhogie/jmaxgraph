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

public class CountTriangles
		implements TooolsPlugin<Digraph, Count_Triangle_Stephane_Result>
{

	@Override
	public Count_Triangle_Stephane_Result process(Digraph g)
	{
		return count(g);
	}

	public static Count_Triangle_Stephane_Result count(Digraph g)
	{
		g.ensureBothDirections();
		// g.symmetrize();

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
					int dout = g.out[u].length;
					weigths[u] = din * dout;

					if (u % 1000 == 0)
						assigningWeights.progressStatus.addAndGet(1000);
				}
			}
		};

		assigningWeights.end();

		long[] partialSums = Utils.partialSums(weigths);

		Count_Triangle_Stephane_Result r = new Count_Triangle_Stephane_Result();

		LongProcess l = new LongProcess("tracking K2,2 by Stephane", - 1);
		l.temporaryResult = r;

		new MultiThreadProcessing()
		{
			@Override
			protected void runInParallel(int rank, List<Thread> threads) throws Throwable
			{
				Count_Triangle_Stephane_Result _r = new Count_Triangle_Stephane_Result();

				while (true)
				{
					l.progressStatus.incrementAndGet();

					Random prng = new Random();
					int u = Utils.pick(partialSums, prng);
					int[] in = g.in[u];
					int[] out = g.out[u];

					int v1 = in[prng.nextInt(in.length)];
					int v2 = out[prng.nextInt(out.length)];

					if (v1 != v2)
					{
						if (g.exists(v1, v2))
						{
							++_r.nbTransitiveTriangles;
						}

						if (g.exists(v1, v2))
						{
							++_r.nbCyclicTriangles;
						}

						++_r.nbIteration;
					}

					if (_r.nbIteration % 1000 == 0)
					{
						synchronized (r)
						{
							r.nbCyclicTriangles += _r.nbCyclicTriangles;
							r.nbTransitiveTriangles += _r.nbTransitiveTriangles;
							r.nbIteration += _r.nbIteration;

							_r.nbCyclicTriangles = 0;
							_r.nbTransitiveTriangles = 0;
							_r.nbIteration = 0;
						}
					}
				}
			}
		};

		// we counted each K22 twice
		r.nbCyclicTriangles /= 3;
		l.end();
		return r;
	}

	@Override
	public void setup(PluginConfig p)
	{
	}

	public static void main(String[] args) throws IOException
	{
		Directory d = new Directory("$HOME/datasets/sample-0.001b.jmg");
		Digraph g = JMGReader.readDirectory(d, 8, false);
		CountTriangles.count(g);
	}
}
