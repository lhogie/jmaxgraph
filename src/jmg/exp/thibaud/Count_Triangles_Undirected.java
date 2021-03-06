package jmg.exp.thibaud;

import it.unimi.dsi.fastutil.ints.IntArrays;
import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.plugins.JMGPlugin;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class Count_Triangles_Undirected
{

	public static class Plugin
			extends JMGPlugin<Graph, CountTriangles_Undirected_Result>
	{
		@Override
		public CountTriangles_Undirected_Result process(Graph g)
		{
			return count(g, nbThreads);
		}

		@Override
		public void setParameters(PluginParms p)
		{
		}
	}

	public static CountTriangles_Undirected_Result count(Graph g, int nbThreads)
	{
		g.out.ensureLoaded(8);
		g.in.ensureLoaded(8);

		g.symmetrize(8);

		CountTriangles_Undirected_Result r = new CountTriangles_Undirected_Result();

		LongProcess l = new LongProcess("tracking transitive triangles", " vertex", g.getNbVertices());

		l.temporaryResult = r;

		new ParallelIntervalProcessing(g.getNbVertices(), nbThreads, l)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				long nbTriangles = 0;
				long nbPotentialTrianglesComputed = 0;
				long nbPotentialTrianglesIncremented = 0;

				for (int u = lowerBound; u < upperBound; ++u)
				{
					int[] Nu = g.out.mem.b[u];
					int du = Nu.length;
					nbPotentialTrianglesComputed += du * (du - 1) / 2;

					int posU = IntArrays.binarySearch(Nu, u);
					int startI = posU < 0 ? - posU - 1 : posU + 1;

					for (int i = startI; i < du; ++i)
					{
						int v = Nu[i];
						int posV = IntArrays.binarySearch(Nu, v);
						int startJ = posV < 0 ? - posV - 1 : posV + 1;

						for (int j = startJ; j < du; ++j)
						{
							int w = Nu[j];

							// this one is wrong
							++nbPotentialTrianglesIncremented;

							if (IntArrays.binarySearch(g.out.mem.b[v], w) >= 0)
							{
								++nbTriangles;
							}
						}
					}

					++s.progressStatus;

					if (u % 1000 == 0)
					{
						synchronized (r)
						{
							r.nbPotentialTrianglesComputed += nbPotentialTrianglesComputed;
							r.nbPotentialTrianglesIncremented += nbPotentialTrianglesIncremented;
							r.nbTriangles += nbTriangles;

							nbPotentialTrianglesComputed = 0;
							nbPotentialTrianglesIncremented = 0;
							nbTriangles = 0;
						}
					}
				}

				synchronized (r)
				{
					r.nbPotentialTrianglesComputed += nbPotentialTrianglesComputed;
					r.nbPotentialTrianglesIncremented += nbPotentialTrianglesIncremented;
					r.nbTriangles += nbTriangles;
				}
			}

		};

		l.end();

		return r;
	}

}
