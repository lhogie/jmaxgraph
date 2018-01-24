package jmg.io.jmg;

import java.util.Iterator;

import jmg.io.jmg.EDGFileVertexIterator.EDGFileCursor;
import toools.thread.ParallelIntervalProcessing;

public abstract class EDGParallelProcessor
{
	private EDGFile f;
	int nbPreallocatedArrays;

	public EDGParallelProcessor(EDGFile file, int nbPreallocatedArrays, int nbThreads)
	{
		this.f = file;
		this.nbPreallocatedArrays = nbPreallocatedArrays;

		new ParallelIntervalProcessing(file.getNbEntries(), nbThreads)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				Iterator<EDGFileCursor> iterator = f.iterator(lowerBound, upperBound,
						nbPreallocatedArrays);
				EDGParallelProcessor.this.process( rank, iterator);
			}
		};
	}

	protected abstract void process(int rank, Iterator<EDGFileCursor> iterator);
}
