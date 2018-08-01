package jmg.exp.thibaud;

import it.unimi.dsi.fastutil.ints.IntArrays;
import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.algo.CountBidirectionalArcs;
import jmg.plugins.JMGPlugin;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;
import toools.util.assertion.Assertions;

public class CountTriangles extends JMGPlugin<Graph, CountTriangles_Result>
{

	@Override
	public CountTriangles_Result process(Graph g)
	{
		return count(g, nbThreads);
	}

	@Override
	public void setParameters(PluginParms p)
	{
	}

	public static CountTriangles_Result count(Graph g, int nbThreads)
	{
		g.out.ensureLoaded(8);
		g.in.ensureLoaded(8);

		CountTriangles_Result r = new CountTriangles_Result();

		LongProcess l = new LongProcess("tracking transitive triangles", " vertex",
				g.getNbVertices());

		l.temporaryResult = r;

		new ParallelIntervalProcessing(g.getNbVertices(), nbThreads, l)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				long nbTransitiveTriangles = 0;
				long threeTimesNbCyclicTriangles = 0;
				long nbPotentialTriangles_computed = 0;
				long nbPotentialTriangles_incremented = 0;

				for (int u = lowerBound; u < upperBound; ++u)
				{
					int dinu = g.in.mem.b[u].length;
					int doutu = g.out.mem.b[u].length;

					// this counts each bidirectional-arc as two potential
					// triangles
					// it is corrected at the end (in the result object)
					nbPotentialTriangles_computed += dinu * doutu;

					for (int v : g.in.mem.b[u])
					{
						for (int w : g.out.mem.b[u])
						{
							if (v != w)
							{
								++nbPotentialTriangles_incremented;

								if (IntArrays.binarySearch(g.out.mem.b[v], w) >= 0)
								{
									++nbTransitiveTriangles;
								}

								if (IntArrays.binarySearch(g.in.mem.b[v], w) >= 0)
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

					++s.progressStatus;

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

		Assertions.ensureEquals(r.nbOfBidiArcs, CountBidirectionalArcs.count(g, 1));
		return r;
	}

}
