
package jmg.algo;

import java.util.concurrent.ThreadLocalRandom;

import jmg.Graph;
import toools.math.MathsUtilities;

/**
 * Cuts the graph according to the Kernighan Lin algorithm (KL)
 * 
 * @author lhogie
 *
 */
class Partitioning
{
	final Graph g;
	final int[] nbCrossArcs;
	final int[] assignment;
	int nbPartitions = 1;

	public Partitioning(Graph g, int nbThreads)
	{
		this.g = g;
		int nbVertices = g.getNbVertices();

		// by default they are all in partition 0
		assignment = new int[nbVertices];

		// and there are no cross-arcs
		nbCrossArcs = new int[nbVertices];
	}

	// assign random sub-partitions in partition p
	public void randomize(double p, int nbPartitions)
	{
		this.nbPartitions = nbPartitions;

		for (int u = 0; u < assignment.length; ++u)
		{
			if (ThreadLocalRandom.current().nextDouble() < p)
			{
				assignment[u] = ThreadLocalRandom.current().nextInt(nbPartitions);
			}
		}

		for (int u = 0; u < assignment.length; ++u)
		{
			nbCrossArcs[u] = countCrossArcs(u, assignment[u]);
		}
	}
	
	public int[] sizeOfPartitions()
	{
		int[] r = new int[nbPartitions];

		for (int p : assignment)
		{
			++r[p];
		}

		return r;
	}

	public long countCrossArcs(Graph g)
	{
		return MathsUtilities.sum(nbCrossArcs) / 2;
	}

	public int countCrossArcs(int u, int p)
	{
		return countVerticesNotIn(p, g.out.mem.b[u])
				+ countVerticesNotIn(p, g.in.mem.b[u]);
	}

	public int countVerticesNotIn(int p, int... vertices)
	{
		return vertices.length - countVerticesIn(p, vertices);
	}

	public int countVerticesIn(int p, int... vertices)
	{
		int count = 0;

		for (int u : vertices)
		{
			// if the neighbor is in another partition
			// we have a cross-arc
			if (assignment[u] == p)
				++count;
		}

		return count;
	}
}
