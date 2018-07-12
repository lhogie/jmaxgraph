package jmg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import jmg.algo.ReverseGraph;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;

public abstract class Adjacency implements AdjacencyPrimitives
{
	public Cache<int[]> degreesCache = new Cache<>(null);
	public Cache<Long> nbArcsCache = new Cache<>( - 1L);
	public Cache<Integer> nbVerticesCache = new Cache<>( - 1);
	public Cache<Integer> maxDegreeCache = new Cache<>( - 1);
	public Cache<Double> avgDegreeCache = new Cache<>( - 1d);

	public MatrixAdj opposite()
	{
		return new MatrixAdj(ReverseGraph.opposite(this, true));
	}

	public IntSet search(Predicate<VertexCursor> p, int nbExpected)
	{
		IntSet r = new IntOpenHashSet(nbExpected);

		for (VertexCursor u : this)
		{
			if (p.test(u))
			{
				r.add(u.vertex);
			}
		}

		return r;
	}

	@Override
	public Iterator<VertexCursor> iterator()
	{
		return iterator(0, getNbVertices(1));
	}

	public int getNbVertices(int nbThreads)
	{
		return nbVerticesCache.get(() -> countVertices(nbThreads));
	}

	public int maxDegree(int nbThreads)
	{
		return maxDegreeCache.get(() -> MathsUtilities.max(degrees(nbThreads)));
	}

	public double avgDegree(int nbThreads)
	{
		return avgDegreeCache.get(() -> MathsUtilities.avg(degrees(nbThreads)));
	}

	public double stdDevDegree(int nbThreads)
	{
		return MathsUtilities.stdDev(degrees(nbThreads));
	}

	public final boolean[] findIsolatedVertices(int expectedNb, int nbThreads)
	{
		int nbVertices = getNbVertices(nbThreads);

		boolean[] isolated = new boolean[nbVertices];
		Arrays.fill(isolated, true);
		LongProcess lp = new LongProcess("tracking isolated vertices", " vertex",
				nbVertices);

		for (VertexCursor c : this)
		{
			if (c.adj.length > 0)
			{
				isolated[c.vertex] = false;

				for (int v : c.adj)
				{
					isolated[v] = false;
				}
			}

			lp.sensor.progressStatus++;
		}

		lp.end();
		return isolated;
	}

	public int[] degrees(int nbThreads)
	{
		return degreesCache.get(() -> computeDegrees(nbThreads));
	}

	public int[] computeDegrees(int nbThreads)
	{
		int[] r = new int[getNbVertices(nbThreads)];

		new ParallelAdjProcessing(this, nbThreads, null)
		{

			@Override
			public void f(ThreadSpecifics s, Iterator<VertexCursor> iterator)
			{
				while (iterator.hasNext())
				{
					VertexCursor c = iterator.next();
					r[c.vertex] = c.adj.length;
				}
			}
		};

		return r;
	}

	public Map<String, Object> makeReport(int nbThreads)
	{
		Map<String, Object> m = new HashMap<>();
		m.put("nbArcs", getNbArcs(nbThreads));
		m.put("max degree", maxDegree(nbThreads));
		m.put("avg degree", avgDegree(nbThreads));
		m.put("std dev degree", stdDevDegree(nbThreads));
		return m;
	}

	public long getNbArcs(int nbThreads)
	{
		return nbArcsCache.get(() -> countArcs(nbThreads));
	}

	protected long countArcs(int nbThreads)
	{
		long n = 0;

		for (VertexCursor c : this)
		{
			n += c.adj.length;
		}

		return n;
	}

}
