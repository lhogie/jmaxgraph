package jmg.exp;

import it.unimi.dsi.fastutil.ints.IntArrays;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class Count_Triangles
		implements TooolsPlugin<Digraph, Count_Triangle_Result>
{
	@Override
	public Count_Triangle_Result process(Digraph g)
	{
		g.ensureBothDirections();

		Count_Triangle_Result r = new Count_Triangle_Result();

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
					int dinu = g.in[u].length;
					int doutu = g.out[u].length;
					nbPotentialTriangles_computed += dinu * doutu;

					for (int v : g.in[u])
					{
						for (int w : g.out[u])
						{
							if (v != w)
							{
								++nbPotentialTriangles_incremented;

								if (IntArrays.binarySearch(g.out[v], w) >= 0)
								{
									++nbTransitiveTriangles;
								}

								if (IntArrays.binarySearch(g.in[v], w) >= 0)
								{
									++threeTimesNbCyclicTriangles;
								}
							}
						}
					}

					l.progressStatus.incrementAndGet();

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
		return r;
	}

	@Override
	public void setup(PluginConfig p)
	{
	}
}
