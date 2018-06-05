package jmg.exp.nathann;

import java.io.IOException;
import java.util.Iterator;

import jmg.Digraph;
import jmg.io.jmg.ArcFileParallelProcessor;
import jmg.io.jmg.ArcFileVertexIterator.ArcFileCursor;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;
import toools.io.IORuntimeException;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;

public class K22AndTransitiveTrianglesCounter
{
	static long[][] inDegreeInduced;
	static boolean[][] preceedsX;

	public static synchronized LocalCount count(Digraph g, int startVertex, int endVertex,
			int nbThreads)
	{
		if (g.jmgDirectory == null)
		{
			JMGDirectory d = new JMGDirectory("$HOME/tmp/flsjklkj");

			if (d.exists())
				d.deleteRecursively();

			g.out.ensureDefined(nbThreads);
			g.in.ensureDefined(nbThreads);

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

		g.out.ensureDefined(8);

		int range = endVertex - startVertex;
		LocalCount r = new LocalCount();
		r.startVertex = startVertex;
		r.endVertex = endVertex;

		if (inDegreeInduced == null || inDegreeInduced.length != nbThreads
				|| inDegreeInduced[0].length != g.getNbVertices())
		{
			System.gc();
			Cout.info("Allocating BIG ARRAYS");
			inDegreeInduced = new long[nbThreads][];
			preceedsX = new boolean[nbThreads][];

			for (int ra = 0; ra < nbThreads; ++ra)
			{
				inDegreeInduced[ra] = new long[g.getNbVertices()];
				preceedsX[ra] = new boolean[g.getNbVertices()];
			}
			Cout.info("DONE");
		}

		r.nbK22sPerVertex_times2 = new long[range];
		r.nbK22sPotPerVertex = new long[range];
		r.nbTrianglesPerVertex = new long[range];
		r.nbTrianglesPotPerVertex = new long[range];
		LongProcess lp = new LongProcess("Nathann tracking K2,2 and triangles from "
				+ startVertex + " to " + endVertex, " vertex", range);
		lp.temporaryResult = r;

		if (g.in.disk.file == null)
			throw new IllegalStateException();

		new ArcFileParallelProcessor(g.in.disk.file, startVertex, endVertex, 0, nbThreads,
				lp)
		{
			@Override
			protected void process(ThreadSpecifics s, Iterator<ArcFileCursor> iterator)
			{
				long nbTransitiveTriangles = 0;
				long nbTtransitiveTrianglesPot = 0;
				long nbK22_times2 = 0;
				long sumNbPotK22 = 0;
				long[] _inDegreeInduced = inDegreeInduced[s.rank];
				boolean[] _preceedsX = preceedsX[s.rank];
				int[][] outAdjTable = g.out.mem.b;

				int nbVerticesComputedSinceLastReport = 0;

				while (iterator.hasNext())
				{
					++s.progressStatus;
					++nbVerticesComputedSinceLastReport;
					ArcFileCursor x = iterator.next();

					r.nbK22sPerVertex_times2[x.vertex - startVertex] = 0;

					long dinx = x.adj.length;
					long nbTrianglesForX = dinx * g.out.mem.b[x.vertex].length;
					nbTtransitiveTrianglesPot += nbTrianglesForX;
					r.nbTrianglesPotPerVertex[x.vertex - startVertex] = nbTrianglesForX;

					if (x.adj.length < 2)
						continue;

					long nbInternalArcs = 0;

					for (int u : x.adj)
					{
						_preceedsX[u] = true;
					}

					for (int u : x.adj)
					{
						for (int v : outAdjTable[u])
						{
							if (_preceedsX[v])
							{
								++nbInternalArcs;
							}

							++_inDegreeInduced[v];
						}
					}

					_inDegreeInduced[x.vertex] = 0;

					long sumOfDegrees = 0;
					long n;

					for (int u : x.adj)
					{
						for (int v : outAdjTable[u])
						{
							n = _inDegreeInduced[v];

							if (n > 1)
							{
								long a = (n * (n - 1)) >> 1;
								nbK22_times2 += a;
								r.nbK22sPerVertex_times2[x.vertex - startVertex] += a;
							}

							_inDegreeInduced[v] = 0;
						}

						sumOfDegrees += outAdjTable[u].length;
						_preceedsX[u] = false;
					}

					long nbK22PotForX = (sumOfDegrees - x.adj.length) * (x.adj.length - 1)
							- nbInternalArcs;
					sumNbPotK22 += nbK22PotForX;
					r.nbK22sPotPerVertex[x.vertex - startVertex] = nbK22PotForX;

					nbTransitiveTriangles += nbInternalArcs;
					r.nbTrianglesPerVertex[x.vertex - startVertex] = nbInternalArcs;

					if (nbVerticesComputedSinceLastReport > 10000)
					{
						synchronized (r)
						{
							r.nbK22s_times2 += nbK22_times2;
							nbK22_times2 = 0;

							r.nbK22sPot += sumNbPotK22;
							sumNbPotK22 = 0;

							r.nbTransitiveTriangles += nbTransitiveTriangles;
							nbTransitiveTriangles = 0;

							r.nbTrianglesPot += nbTtransitiveTrianglesPot;
							nbTtransitiveTrianglesPot = 0;

							nbVerticesComputedSinceLastReport = 0;
						}
					}
				}

				synchronized (r)
				{
					r.nbK22s_times2 += nbK22_times2;
					r.nbK22sPot += sumNbPotK22;
					r.nbTransitiveTriangles += nbTransitiveTriangles;
					r.nbTrianglesPot += nbTtransitiveTrianglesPot;
				}
			}
		};

		lp.end();
		return r;
	}
}
