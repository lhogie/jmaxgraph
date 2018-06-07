package jmg;

import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.IntSet;
import toools.math.MathsUtilities;

public abstract class Adjacency implements Iterable<VertexCursor>
{
	// may have been initialized
	public int nbArcs = - 1;

	public abstract int[] get(int u);

	public abstract boolean isDefined();

	public int maxDegree(int nbThreads)
	{
		return MathsUtilities.max(degrees(nbThreads));
	}

	public double avgDegree(int nbThreads)
	{
		return MathsUtilities.avg(degrees(nbThreads));
	}

	public abstract IntSet findIsolatedVertices(int expectedNb, int nbThreads);

	public abstract int[] degrees(int nbThreads);

	public Map<String, Object> makeReport(int nbThreads)
	{
		Map<String, Object> m = new HashMap<>();
		m.put("nbArcs= ", countArcs(nbThreads));
		m.put("max degree= ", maxDegree(nbThreads));
		m.put("avg degree= ", avgDegree(nbThreads));
		return m;
	}

	public long countArcs(int nbThreads)
	{
		long n = 0;
		
		for (VertexCursor c : this)
		{
			n+=c.adj.length;
		}
		
		return n;
	}
}
