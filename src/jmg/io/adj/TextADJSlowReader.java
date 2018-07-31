package jmg.io.adj;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import toools.io.Cout;
import toools.io.Utilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.util.Conversion;

public class TextADJSlowReader extends TextADJReader
{
	@Override
	public Int2ObjectMap<int[]> readFile() throws IOException
	{
		// don't use multi-threading if the file is small
		int nbT = from.getSize() < 1000000 ? 1 : nbThreads;

		LongProcess reading = new LongProcess("reading arcs from " + from, " arc",
				nbArcsExpected);
		Cout.info("using " + nbT + " threads");
		Section[] sectionPosititions = findSections(from, nbT);
		Int2ObjectMap<int[]>[] localAdjs = new Int2ObjectMap[nbT];

		if (hasNbVertices)
		{
			InputStream in = from.createReadingStream();
			Scanner sc = new Scanner(in);
			nbVerticesExpected = sc.nextInt();
			Cout.info(nbVerticesExpected + " vertices declared on first line");
			sc.close();
		}

		new MultiThreadProcessing(nbT, reading)
		{
			@Override
			protected void runInParallel(ThreadSpecifics s, List<Thread> threads)
					throws Throwable
			{
				Int2ObjectMap<int[]> _localAdj = new Int2ObjectOpenHashMap<>(
						nbVerticesExpected / NB_THREADS_TO_USE);
				localAdjs[s.rank] = _localAdj;
				InputStream _is = from.createReadingStream(65536 * 256);

				Section section = sectionPosititions[s.rank];
				Utilities.skip(_is, section.fromPosition);

				Scanner _scanner = new Scanner(_is);

				if (s.rank == 0 && hasNbVertices)
				{
					// skip the number of vertices in the file
					_scanner.nextLong();
				}

				long _nbEdge = 0;

				while (_scanner.hasNext())
				{

					int _src = Conversion.long2int(_scanner.nextLong());

					// if it reaches the end of the section
					if (_src == section.endVertex)
						break;

					int _nbNeighbors = _scanner.nextInt();

					int[] _outNeighbors = _nbNeighbors == 0 ? IntArrays.EMPTY_ARRAY
							: new int[_nbNeighbors];

					for (int i = 0; i < _nbNeighbors; ++i)
					{
						int neighbor = _scanner.nextInt();
						_outNeighbors[i] = neighbor;
						++_nbEdge;
					}

					s.progressStatus += _nbNeighbors;

					_localAdj.put(_src, _outNeighbors);
				}

				Cout.progress("thread " + s.rank + " has read " + _localAdj.size()
						+ " vertices and " + _nbEdge + " edges");
			}
		};

		reading.end();
		Int2ObjectMap<int[]> bigMap = merge(localAdjs);
		return bigMap;
	}
}
