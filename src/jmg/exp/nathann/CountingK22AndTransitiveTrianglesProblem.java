package jmg.exp.nathann;

import jmg.Digraph;
import jmr.FSMapReduce;
import jmr.Job;
import jmr.Problem;

public class CountingK22AndTransitiveTrianglesProblem extends Problem<CountK22WorkerOutput, GlobalCount>
{
	private final Digraph g;
	private final boolean countK22;
	private final int nbThreads;

	public CountingK22AndTransitiveTrianglesProblem(Digraph g, boolean countK22,
			int nbThreads)
	{
		this.g = g;
		this.countK22 = countK22;
		this.nbThreads = nbThreads;
	}

	@Override
	public int size()
	{
		return g.getNbVertices();
	}

	@Override
	public GlobalCount createResult(FSMapReduce mr, int nbThreads)
	{
		return new GlobalCount(g, mr, nbThreads);
	}

	@Override
	public Job createJob(int start, int end)
	{
		K22Job r = new K22Job();
		r.startVertex = start;
		r.endVertex = end;
		r.countK22 = countK22;
		r.jmgDirectoryPath = g.jmgDirectory.getPath();
		r.nbThreads = nbThreads;
		return r;
	}

}
