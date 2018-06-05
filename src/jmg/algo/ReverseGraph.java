package jmg.algo;

import java.util.concurrent.atomic.AtomicLong;

import j4u.chain.PluginConfig;
import j4u.chain.TooolsPlugin;
import jmg.Digraph;
import jmg.Utils;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class ReverseGraph implements TooolsPlugin<Digraph, Digraph>
{
	static class TimeWasted
	{
		long allocating, reusing;

		public String toString()
		{
			return "time wasted allocating= " + allocating + " reusing=" + reusing;
		}
	}

	public static int[][] computeInverseADJ(int[][] adj, boolean freeMemOnTheFly)
	{
		int[] invDegree = computeReverseDegrees(adj);
		int[][] r = new int[invDegree.length][];
		int nbVertex = adj.length;
		int[] pos = new int[nbVertex];
		LongProcess computing = new LongProcess(
				"computing inverse adjacencies, freeing memory=" + freeMemOnTheFly,
				" vertex", nbVertex);

		TimeWasted timeWastedInAllocations = new TimeWasted();
		computing.temporaryResult = timeWastedInAllocations;

		for (int v = 0; v < nbVertex; ++v)
		{
			for (int neighbor : adj[v])
			{
				int[] invAdjList = r[neighbor];

				if (invAdjList == null)
				{
					long a = System.currentTimeMillis();
					invAdjList = r[neighbor] = new int[invDegree[neighbor]];
					timeWastedInAllocations.allocating += System.currentTimeMillis() - a;
				}

				invAdjList[pos[neighbor]++] = v;
			}

			if (freeMemOnTheFly)
			{
				adj[v] = null;
			}

			++computing.sensor.progressStatus;
		}

		// vertices that had no out-neighbors
		for (int u = 0; u < r.length; ++u)
		{
			if (r[u] == null)
			{
				r[u] = Utils.emptyArray;
			}
		}

		computing.end("timeWastedInAllocations=" + timeWastedInAllocations);
		return r;
	}

	public static int[][] computeInverseADJ_par(int[][] adj, boolean pruneSrc,
			int nbThreads)
	{
		int[] degrees = computeReverseDegrees(adj);
		int[][] r = allocates(degrees, nbThreads);

		int nbVertex = adj.length;

		int[] pos = new int[nbVertex];

		LongProcess computing = new LongProcess("computing inverse adjacencies",
				" vertex", nbVertex);

		new ParallelIntervalProcessing(nbVertex, nbThreads, computing)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					for (int v : adj[u])
					{
						int[] vAdj = r[v];
						vAdj[pos[v]++] = u;
					}

					if (pruneSrc)
					{
						adj[u] = null;
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
				r[u] = Utils.emptyArray;
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

	public static int[] computeReverseDegrees(int[][] adj)
	{
		LongProcess compute = new LongProcess("computeReverseDegrees", " vertex", adj.length);
		int nbVertices = adj.length;
		int[] degree = new int[nbVertices];

		for (int u = 0; u < nbVertices; ++u)
		{
			for (int v : adj[u])
			{
				++degree[v];
			}

			++compute.sensor.progressStatus;
		}

		compute.end();
		return degree;
	}

	@Override
	public Digraph process(Digraph g)
	{
		g.reverse();
		return g;
	}

	@Override
	public void setup(PluginConfig p)
	{
	}

}
