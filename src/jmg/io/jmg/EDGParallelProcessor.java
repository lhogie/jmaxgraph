package jmg.io.jmg;

import java.util.Iterator;

import jmg.io.jmg.EDGFileVertexIterator.EDGFileCursor;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public abstract class EDGParallelProcessor
{
	private ArcFile f;
	int nbPreallocatedArrays;

	public EDGParallelProcessor(ArcFile file, int nbPreallocatedArrays, int nbThreads, LongProcess lp)
	{
		this.f = file;
		this.nbPreallocatedArrays = nbPreallocatedArrays;

		new ParallelIntervalProcessing(file.getNbEntries(), nbThreads, lp)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				EDGFileVertexIterator iterator = f.iterator(lowerBound, upperBound,
						nbPreallocatedArrays, 256 * 256 * 256);
				EDGParallelProcessor.this.process(s, iterator);
				iterator.close();
			}
		};
	}

	protected abstract void process(ThreadSpecifics s, Iterator<EDGFileCursor> iterator);
}
