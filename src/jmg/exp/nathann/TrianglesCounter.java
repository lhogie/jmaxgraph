package jmg.exp.nathann;

import java.io.IOException;
import java.util.Iterator;

import jmg.Graph;
import jmg.ParallelAdjProcessing;
import jmg.VertexCursor;
import jmg.io.jmg.ArcFileParallelProcessor;
import jmg.io.jmg.ArcFileVertexIterator.ArcFileCursor;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;
import toools.io.IORuntimeException;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;

public class TrianglesCounter
{

	static boolean[][] preceedsX;

	public static synchronized LocalCount count(Graph g, int startVertex, int endVertex,
			int nbThreads)
	{
		if (g.jmgDirectory == null)
		{
			JMGDirectory d = new JMGDirectory("$HOME/tmp/flsjklkj");

			if (d.exists())
				d.deleteRecursively();

			g.out.ensureLoaded(nbThreads);
			g.in.ensureLoaded(nbThreads);

			try
			{
				g.write(d);
			}
			catch (IOException e)
			{
				throw new IORuntimeException(e);
			}

			g.setDataset(d);
		}

		g.out.ensureLoaded(8);

		int range = endVertex - startVertex;
		LocalCount r = new LocalCount(startVertex, endVertex);
		r.nbK22sPerVertex_times2 = new long[range];
		r.nbTrianglesPerVertex = new long[range];

		if (preceedsX == null || preceedsX.length != nbThreads
				|| preceedsX[0].length != g.getNbVertices())
		{
			System.gc();
			Cout.info("Allocating BIG ARRAYS");
			preceedsX = new boolean[nbThreads][];

			for (int ra = 0; ra < nbThreads; ++ra)
			{
				preceedsX[ra] = new boolean[g.getNbVertices()];
			}
			Cout.info("DONE");
		}

		LongProcess lp = new LongProcess(
				"Nathann tracking triangles from " + startVertex + " to " + endVertex,
				" vertex", range);
		lp.temporaryResult = r;

		if ( ! g.in.disk.isDefined())
			throw new IllegalStateException();

		new ParallelAdjProcessing(g.in.disk, nbThreads, lp)
		{
			
			@Override
			public void f(ThreadSpecifics s, Iterator<VertexCursor> iterator)
			{
				long nbTransitiveTriangles = 0;
				long nbCyclicTriangles_times3 = 0;
				long nbTrianglesPot = 0;
				boolean[] _preceedsX = preceedsX[s.rank];
				int[][] outAdjTable = g.out.mem.b;

				int nbVerticesComputedSinceLastReport = 0;

				while (iterator.hasNext())
				{
					++s.progressStatus;
					++nbVerticesComputedSinceLastReport;
					VertexCursor x = iterator.next();

					long dinx = x.adj.length;
					long nbTrianglesForX = dinx * g.out.mem.b[x.vertex].length;
					nbTrianglesPot += nbTrianglesForX;

					if (x.adj.length < 2)
						continue;

					long nbInternalArcs = 0;
					long nbInternalArcs_cyclic = 0;

					for (int u : x.adj)
					{
						_preceedsX[u] = true;
					}

					// compute #transitiveTriangles
					for (int u : x.adj)
					{
						for (int v : outAdjTable[u])
						{
							if (_preceedsX[v])
							{
								++nbInternalArcs;
							}
						}
					}

					// compute #cyclicTriangles
					for (int u : outAdjTable[x.vertex])
					{
						for (int v : outAdjTable[u])
						{
							if (_preceedsX[v])
							{
								++nbInternalArcs_cyclic;
							}
						}
					}

					for (int u : x.adj)
					{
						_preceedsX[u] = false;
					}

					nbTransitiveTriangles += nbInternalArcs;
					r.nbTrianglesPerVertex[x.vertex - startVertex] = nbInternalArcs;
					nbCyclicTriangles_times3 += nbInternalArcs_cyclic;

					if (nbVerticesComputedSinceLastReport > 10000)
					{
						synchronized (r)
						{
							r.nbTrianglesPot += nbTrianglesPot;
							nbTrianglesPot = 0;

							r.nbTransitiveTriangles += nbTransitiveTriangles;
							nbTransitiveTriangles = 0;

							r.nbCyclicTriangles_times3 += nbCyclicTriangles_times3;
							nbCyclicTriangles_times3 = 0;

							nbVerticesComputedSinceLastReport = 0;
						}
					}
				}

				synchronized (r)
				{
					r.nbTrianglesPot += nbTrianglesPot;
					r.nbTransitiveTriangles += nbTransitiveTriangles;
					r.nbCyclicTriangles_times3 += nbCyclicTriangles_times3;
				}
				}
		};
	

		lp.end();
		return r;
	}
}
