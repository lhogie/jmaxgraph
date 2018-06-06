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

public class CountCyclicTriangles
{

	static boolean[][] preceedsX;

	public static synchronized LocalCount count(Digraph g, int startVertex,
			int endVertex, int nbThreads)
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
		r.nbK22sPerVertex_times2 = new long[range];

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
				"Nathann tracking cyclic triangles from " + startVertex + " to " + endVertex,
				" vertex", range);
		lp.temporaryResult = r;

		if (g.in.disk.file == null)
			throw new IllegalStateException();

		new ArcFileParallelProcessor(g.in.disk.file, startVertex, endVertex, 0, nbThreads, lp)
		{
			@Override
			protected void process(ThreadSpecifics s, Iterator<ArcFileCursor> iterator)
			{
				long nbCyclicTriangles_times3 = 0;
				boolean[] _preceedsX = preceedsX[s.rank];
				int[][] outAdjTable = g.out.mem.b;

				int nbVerticesComputedSinceLastReport = 0;

				while (iterator.hasNext())
				{
					++s.progressStatus;
					++nbVerticesComputedSinceLastReport;
					ArcFileCursor x = iterator.next();

					if (x.adj.length < 2)
						continue;


					for (int u : x.adj)
					{
						_preceedsX[u] = true;
					}

					long nbInternalArcs_cyclic = 0;
	
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

					nbCyclicTriangles_times3 += nbInternalArcs_cyclic;

					if (nbVerticesComputedSinceLastReport > 10000)
					{
						synchronized (r)
						{
							r.nbCyclicTriangles_times3 += nbCyclicTriangles_times3;
							nbCyclicTriangles_times3 = 0;

							nbVerticesComputedSinceLastReport = 0;
						}
					}
				}

				synchronized (r)
				{
					r.nbCyclicTriangles_times3 += nbCyclicTriangles_times3;
				}
			}
		};

		lp.end();
		return r;
	}
}
