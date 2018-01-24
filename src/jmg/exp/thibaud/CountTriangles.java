package jmg.exp.thibaud;

import it.unimi.dsi.fastutil.ints.IntArrays;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.algo.CountBidirectionalArcs;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;
import toools.util.assertion.Assertions;

public class CountTriangles implements TooolsPlugin<Digraph, CountTriangles_Result>
{

	@Override
	public CountTriangles_Result process(Digraph g)
	{
		return count(g);
	}

	@Override
	public void setup(PluginConfig p)
	{
	}

	public static CountTriangles_Result count(Digraph g)
	{
		g.out.ensureDefined();
		g.in.ensureDefined();

		CountTriangles_Result r = new CountTriangles_Result();

		LongProcess l = new LongProcess("tracking transitive triangles", g.getNbVertex());

		l.temporaryResult = r;

		new ParallelIntervalProcessing(g.getNbVertex())
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				long nbTransitiveTriangles = 0;
				long threeTimesNbCyclicTriangles = 0;
				long nbPotentialTriangles_computed = 0;
				long nbPotentialTriangles_incremented = 0;

				for (int u = lowerBound; u < upperBound; ++u)
				{
					int dinu = g.in.adj[u].length;
					int doutu = g.out.adj[u].length;

					// this counts each bidirectional-arc as two potential
					// triangles
					// it is corrected at the end (in the result object)
					nbPotentialTriangles_computed += dinu * doutu;

					for (int v : g.in.adj[u])
					{
						for (int w : g.out.adj[u])
						{
							if (v != w)
							{
								++nbPotentialTriangles_incremented;

								if (IntArrays.binarySearch(g.out.adj[v], w) >= 0)
								{
									++nbTransitiveTriangles;
								}

								if (IntArrays.binarySearch(g.in.adj[v], w) >= 0)
								{
									++threeTimesNbCyclicTriangles;
								}
							}
							else
							{
								++r.nbOfBidiArcs;
							}
						}
					}

					++l.progressStatus;

					if (u % 1000 == 0)
					{
						synchronized (r)
						{
							r.nbPotentialTriangles_computed += nbPotentialTriangles_computed;
							r.nbPotentialTriangles_incremented += nbPotentialTriangles_incremented;
							r.threeTimesNbCyclicTriangles += threeTimesNbCyclicTriangles;
							r.nbTransitiveTriangles += nbTransitiveTriangles;

							nbPotentialTriangles_computed = 0;
							nbPotentialTriangles_incremented = 0;
							threeTimesNbCyclicTriangles = 0;
							nbTransitiveTriangles = 0;
						}
					}
				}

				synchronized (r)
				{
					r.nbPotentialTriangles_computed += nbPotentialTriangles_computed;
					r.nbPotentialTriangles_incremented += nbPotentialTriangles_incremented;
					r.threeTimesNbCyclicTriangles += threeTimesNbCyclicTriangles;
					r.nbTransitiveTriangles += nbTransitiveTriangles;
				}
			}
		};

		l.end();
		r.nbOfBidiArcs /= 2;

		r.nbPotentialTriangles_computed = r.nbPotentialTriangles_computed
				- 2 * r.nbOfBidiArcs;

		Assertions.ensureEquals(r.nbOfBidiArcs, CountBidirectionalArcs.count(g));
		return r;
	}

}