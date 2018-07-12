package jmg.exp.nathann;

import jmg.Graph;
import jmr.FSMapReduce;
import jmr.Job;
import jmr.Problem;

public class CountingCyclicTrianglesProblem extends Problem
{
	private final Graph g;
	private final int nbThreads;

	public CountingCyclicTrianglesProblem(Graph g, int nbThreads)
	{
		this.g = g;
		this.nbThreads = nbThreads;
	}

	@Override
	public int size()
	{
		return g.getNbVertices();
	}

	@Override
	public GlobalCount createResult(FSMapReduce mr, int i)
	{
		return new GlobalCount(g, mr, - 1);
	}

	@Override
	public Job createJob(int start, int end)
	{
		CountCyclicJob r = new CountCyclicJob();
		r.startVertex = start;
		r.endVertex = end;
		r.jmgDirectoryPath = g.jmgDirectory.getPath();
		r.nbThreads = nbThreads;
		return r;
	}
}
