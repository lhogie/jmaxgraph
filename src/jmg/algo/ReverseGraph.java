package jmg.algo;

import java.util.concurrent.atomic.AtomicLong;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.Utils;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class ReverseGraph implements TooolsPlugin<Digraph, Digraph>
{
	public static int[][] computeInverseADJ(int[][] adj, boolean pruneSrc)
	{
		int[] invDegree = computeReverseDegrees(adj);
		int[][] r = new int[invDegree.length][];
		int nbVertex = adj.length;
		int[] pos = new int[nbVertex];
		LongProcess computing = new LongProcess("computing inverse adjacencies",
				nbVertex);

		for (int v = 0; v < nbVertex; ++v)
		{
			for (int neighbor : adj[v])
			{
				int[] invAdjList = r[neighbor];

				if (invAdjList == null)
					invAdjList = r[neighbor] = new int[invDegree[neighbor]];

				invAdjList[pos[neighbor]++] = v;
			}

			if (pruneSrc)
			{
				adj[v] = null;
			}

			if (v % 100 == 0)
				computing.progressStatus.addAndGet(100);
		}

		// vertices that had no out-neighbors
		for (int u = 0; u < r.length; ++u)
		{
			if (r[u] == null)
			{
				r[u] = Utils.emptyArray;
			}
		}

		computing.end();
		return r;
	}

	public static int[][] computeInverseADJ_par(int[][] adj, boolean pruneSrc)
	{
		int[] degrees = computeReverseDegrees(adj);
		int[][] r = allocates(degrees);

		int nbVertex = adj.length;

		LongProcess initIndex = new LongProcess("initializing indices", nbVertex);
		int[] pos = new int[nbVertex];
		initIndex.progressStatus.set(0);

		initIndex.end();
		LongProcess computing = new LongProcess("computing inverse adjacencies",
				nbVertex);

		new ParallelIntervalProcessing(nbVertex)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
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

					if (u % 100 == 0)
						computing.progressStatus.addAndGet(100);
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
		new ParallelIntervalProcessing(nbVertex)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
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

	public static int[][] allocates(int[] degree)
	{
		LongProcess allocating = new LongProcess("preallocating out-adj", " array",
				degree.length);
		int[][] r = new int[degree.length][];

		new ParallelIntervalProcessing(degree.length)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					r[u] = new int[degree[u]];

					if (u % 100 == 0)
						allocating.progressStatus.addAndGet(100);
				}
			}
		};

		allocating.end();
		return r;
	}

	public static int[] computeReverseDegrees(int[][] adj)
	{
		LongProcess compute = new LongProcess("computeReverseDegrees", adj.length);
		int nbVertices = adj.length;
		int[] degree = new int[nbVertices];

		for (int u = 0; u < nbVertices; ++u)
		{
			for (int v : adj[u])
			{
				++degree[v];
			}

			if (u % 100 == 0)
				compute.progressStatus.addAndGet(100);
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
