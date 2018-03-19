package jmg.exp.stephane;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import java4unix.pluginchain.PluginConfig;
import jmg.Digraph;
import jmg.chain.JMGPlugin;
import jmg.io.jmg.JMGDirectory;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class CountTriangles extends JMGPlugin<Digraph, CountTriangleResult>
{

	@Override
	public CountTriangleResult process(Digraph g)
	{
		return count(g, nbThreads);
	}

	public static CountTriangleResult count(Digraph g, int nbThreads)
	{
		g.out.ensureDefined(8);
		g.in.ensureDefined(8);

		// g.symmetrize();

		int nbVertices = g.getNbVertices();

		LongProcess assigningWeights = new LongProcess("assigning weights", " vertex",
				g.getNbVertices());

		long[] weigths = new long[nbVertices];

		new ParallelIntervalProcessing(g.getNbVertices(), nbThreads, assigningWeights)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					long din = g.in.adj[u].length;
					long dout = g.out.adj[u].length;
					weigths[u] = din * dout;
					++s.progressStatus;
				}
			}
		};

		assigningWeights.end();

		long[] partialSums = MathsUtilities.partialSums(weigths);

		CountTriangleResult r = new CountTriangleResult();

		LongProcess l = new LongProcess("tracking K2,2 by Stephane", " couple", - 1);
		l.temporaryResult = r;

		new MultiThreadProcessing(nbThreads, l)
		{
			@Override
			protected void runInParallel(ThreadSpecifics s, List<Thread> threads)
					throws Throwable
			{
				CountTriangleResult _r = new CountTriangleResult();

				while (true)
				{
					++s.progressStatus;

					Random prng = new Random();
					int u = MathsUtilities.pick(partialSums, prng);
					int[] in = g.in.adj[u];
					int[] out = g.out.adj[u];

					int v1 = in[prng.nextInt(in.length)];
					int v2 = out[prng.nextInt(out.length)];

					if (v1 != v2)
					{
						if (g.arcExists(v1, v2))
						{
							++_r.nbTransitiveTriangles;
						}

						if (g.arcExists(v1, v2))
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

		// we counted each K22 three twice
		r.nPossibleEvents = partialSums[partialSums.length - 1];
		l.end();
		return r;
	}

	@Override
	public void setup(PluginConfig p)
	{
	}

	public static void main(String[] args) throws IOException
	{
		JMGDirectory d = new JMGDirectory("$HOME/datasets/sample-0.001b.jmg");
		Digraph g = d.mapGraph(8, false);
		CountTriangles.count(g, 1);
	}
}
