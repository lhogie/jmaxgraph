package jmg.exp.thibaud;

import it.unimi.dsi.fastutil.ints.IntArrays;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.io.DotWriter;
import toools.io.Cout;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class Count_Triangles_Undirected
{

	public static class Plugin
			implements TooolsPlugin<Digraph, Count_Triangles_Undirected_Result>
	{
		@Override
		public Count_Triangles_Undirected_Result process(Digraph g)
		{
			return count(g);
		}

		@Override
		public void setup(PluginConfig p)
		{
		}
	}
	
	public static Count_Triangles_Undirected_Result count(Digraph g)
	{
		g.ensureBothDirections();
		g.symmetrize();


		Count_Triangles_Undirected_Result r = new Count_Triangles_Undirected_Result();

		LongProcess l = new LongProcess("tracking transitive triangles", g.getNbVertex());

		l.temporaryResult = r;

		new ParallelIntervalProcessing(g.getNbVertex())
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				long nbTriangles = 0;
				long nbPotentialTrianglesComputed = 0;
				long nbPotentialTrianglesIncremented = 0;

				for (int u = lowerBound; u < upperBound; ++u)
				{
					int[] Nu = g.out[u];
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

							if (IntArrays.binarySearch(g.out[v], w) >= 0)
							{
								++nbTriangles;
							}
						}
					}

					l.progressStatus.incrementAndGet();

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
