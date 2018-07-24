package jmg;

import java.util.Iterator;

import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public abstract class ParallelAdjProcessing
{
	public ParallelAdjProcessing(Adjacency adj, int nbThreads, LongProcess lp)
	{
		new ParallelIntervalProcessing(adj.getNbVertices(), nbThreads, lp)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
					throws Throwable
			{
				processSubAdj(s, adj.iterator(lowerBound, upperBound));
			}
		};
	}

	public abstract void processSubAdj(ThreadSpecifics s,
			Iterator<VertexCursor> iterator);
}
