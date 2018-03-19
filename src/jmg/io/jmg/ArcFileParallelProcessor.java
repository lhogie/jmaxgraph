package jmg.io.jmg;

import java.util.Iterator;

import jmg.io.jmg.ArcFileVertexIterator.ArcFileCursor;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public abstract class ArcFileParallelProcessor
{
	private ArcFile f;
	int nbPreallocatedArrays;

	public ArcFileParallelProcessor(ArcFile file, int startVertex, int endVertex,
			int nbPreallocatedArrays, int nbThreads, LongProcess lp)
	{
		this.f = file;
		this.nbPreallocatedArrays = nbPreallocatedArrays;
		int nbEntries = endVertex - startVertex;

		new ParallelIntervalProcessing(nbEntries, nbThreads, lp)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				lowerBound += startVertex;
				upperBound += startVertex;
				ArcFileVertexIterator iterator = f.iterator(lowerBound, upperBound,
						nbPreallocatedArrays, 256 * 256 * 256);
				ArcFileParallelProcessor.this.process(s, iterator);
				iterator.close();
			}
		};
	}

	protected abstract void process(ThreadSpecifics s, Iterator<ArcFileCursor> iterator);
}
