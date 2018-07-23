package jmg.algo;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import jmg.Adjacency;
import jmg.Graph;
import jmg.JmgUtils;
import jmg.VertexCursor;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class ReverseGraph implements TooolsPlugin<Graph, Graph>
{
	static class TimeWasted
	{
		long allocating, reusing;

		public String toString()
		{
			return "time wasted allocating= " + allocating + " reusing=" + reusing;
		}
	}

	public static int[][] opposite(Adjacency opposite, boolean freeMemOnTheFly)
	{
		int nbVertex = opposite.getNbVertices();
		int[] invDegree = computeReverseDegrees(nbVertex, opposite);
		int[][] r = new int[invDegree.length][];
		int[] pos = new int[nbVertex];
		LongProcess computing = new LongProcess(
				"computing inverse adjacencies, freeing memory=" + freeMemOnTheFly,
				" vertex", nbVertex);

		TimeWasted timeWastedInAllocations = new TimeWasted();
		computing.temporaryResult = timeWastedInAllocations;

		for (VertexCursor c : opposite)
		{
			for (int neighbor : c.adj)
			{
				int[] invAdjList = r[neighbor];

				if (invAdjList == null)
				{
					long a = System.currentTimeMillis();
					invAdjList = r[neighbor] = new int[invDegree[neighbor]];
					timeWastedInAllocations.allocating += System.currentTimeMillis() - a;
				}

				invAdjList[pos[neighbor]++] = c.vertex;
			}

			if (freeMemOnTheFly)
			{
				c.adj = null;
			}

			++computing.sensor.progressStatus;
		}

		// vertices that had no out-neighbors
		for (int u = 0; u < r.length; ++u)
		{
			if (r[u] == null)
			{
				r[u] = JmgUtils.emptyArray;
			}
		}

		computing.end("timeWastedInAllocations=" + timeWastedInAllocations);
		return r;
	}

	public static int[][] computeInverseADJ_par(Adjacency opposite, boolean pruneSrc,
			int nbThreads)
	{
		int nbVertex = opposite.getNbVertices();
		int[] degrees = computeReverseDegrees(nbVertex, opposite);
		int[][] r = allocates(degrees, nbThreads);
		int[] pos = new int[nbVertex];

		LongProcess computing = new LongProcess("computing inverse adjacencies",
				" vertex", nbVertex);

		new ParallelIntervalProcessing(nbVertex, nbThreads, computing)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				Iterator<VertexCursor> i = opposite.iterator(lowerBound, upperBound);

				while (i.hasNext())
				{
					VertexCursor c = i.next();

					for (int v : c.adj)
					{
						int[] vAdj = r[v];
						vAdj[pos[v]++] = c.vertex;
					}

					if (pruneSrc)
					{
						c.adj = null;
					}

					++s.progressStatus;
				}
			}
		};

		// vertices that had no in-neighbors
		for (int u = 0; u < r.length; ++u)
		{
			if (r[u] == null)
			{
				r[u] = JmgUtils.emptyArray;
			}
		}

		// check
		AtomicLong nbError = new AtomicLong(0);
		new ParallelIntervalProcessing(nbVertex, nbThreads, null)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					if (pos[u] != degrees[u])
					{
						nbError.incrementAndGet();
					}

				}
			}
		};

		if (nbError.get() > 0)
		{
			throw new IllegalStateException("ADJ ERROR " + nbError.get());
		}

		computing.end();
		return r;
	}

	public static int[][] allocates(int[] degree, int nbThreads)
	{
		LongProcess allocating = new LongProcess("preallocating out-adj", " array",
				degree.length);
		int[][] r = new int[degree.length][];

		new ParallelIntervalProcessing(degree.length, nbThreads, allocating)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					r[u] = new int[degree[u]];

					++s.progressStatus;
				}
			}
		};

		allocating.end();
		return r;
	}

	public static int[] computeReverseDegrees(int nbVertices, Iterable<VertexCursor> adj)
	{
		LongProcess compute = new LongProcess("computeReverseDegrees", " vertex",
				nbVertices);
		int[] degree = new int[nbVertices];

		for (VertexCursor c : adj)
		{
			for (int v : c.adj)
			{
				++degree[v];
			}

			++compute.sensor.progressStatus;
		}

		compute.end();
		return degree;
	}

	@Override
	public Graph process(Graph g)
	{
		g.reverse();
		return g;
	}

	@Override
	public void setParameters(PluginParms p)
	{
	}

}
