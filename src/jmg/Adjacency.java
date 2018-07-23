package jmg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import jmg.algo.ReverseGraph;
import toools.io.file.Directory;
import toools.io.serialization.Serializer;
import toools.io.serialization.TextSerializer;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;

public abstract class Adjacency implements AdjacencyPrimitives
{
	public final Cache<int[]> degreesCache;
	public final Cache<Long> nbArcsCache;
	public final Cache<Integer> nbVerticesCache;
	public final Cache<Integer> maxDegreeCache;
	public final Cache<Double> avgDegreeCache;

	public Adjacency(Directory d, int nbThreads)
	{
		degreesCache = new Cache<int[]>(null, "degrees", d,
				Serializer.getDefaultSerializer(), () -> computeDegrees(nbThreads));

		nbArcsCache = new Cache<Long>( - 1L, "nb_arcs", d, new TextSerializer.Int64(),
				() -> countArcs(nbThreads));

		nbVerticesCache = new Cache<Integer>( - 1, "nb_vertices", d,
				new TextSerializer.Int32(), () -> countVertices(nbThreads));

		maxDegreeCache = new Cache<Integer>( - 1, "max_degree", d,
				new TextSerializer.Int32(), () -> MathsUtilities.max(degrees()));

		avgDegreeCache = new Cache<Double>( - 1d, "avg_degree", d,
				new TextSerializer.Float64(), () -> MathsUtilities.avg(degrees()));
	}

	public MatrixAdj opposite(int nbThreads)
	{
		return new MatrixAdj(ReverseGraph.opposite(this, true), null, nbThreads);
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
		return iterator(0, getNbVertices());
	}

	public int getNbVertices()
	{
		return nbVerticesCache.get();
	}

	public int maxDegree()
	{
		return maxDegreeCache.get();
	}

	public double avgDegree()
	{
		return avgDegreeCache.get();
	}

	public double stdDevDegree()
	{
		return MathsUtilities.stdDev(degrees());
	}

	public final boolean[] findIsolatedVertices(int expectedNb, int nbThreads)
	{
		int nbVertices = getNbVertices();

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

	public int[] degrees()
	{
		return degreesCache.get();
	}

	public int[] computeDegrees(int nbThreads)
	{
		int[] r = new int[getNbVertices()];
		LongProcess lp = new LongProcess("compute degrees2", " vertex", getNbVertices());

		new ParallelAdjProcessing(this, nbThreads, null)
		{

			@Override
			public void processSubAdj(ThreadSpecifics s, Iterator<VertexCursor> iterator)
			{
				while (iterator.hasNext())
				{
					VertexCursor c = iterator.next();
					r[c.vertex] = c.adj.length;
					s.progressStatus++;
				}
			}
		};

		lp.end();

		return r;
	}

	public Map<String, Object> makeReport(int nbThreads)
	{
		Map<String, Object> m = new HashMap<>();
		m.put("nbArcs", getNbArcs());
		m.put("max degree", maxDegree());
		m.put("avg degree", avgDegree());
		m.put("std dev degree", stdDevDegree());
		return m;
	}

	public long getNbArcs()
	{
		return nbArcsCache.get();
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
