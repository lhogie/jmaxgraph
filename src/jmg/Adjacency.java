package jmg;

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

	public abstract IntSet findIsolatedVertices(int expectedNb, int nbThreads);

	public abstract int[] degrees(int nbThreads);
}
