package jmg.io.adj;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import jmg.JmgUtils;
import toools.io.Cout;
import toools.io.TextNumberReader;
import toools.io.Utilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.util.Conversion;

public class TextADJFastReader extends TextADJReader
{

	@Override
	public Int2ObjectMap<int[]> readFile() throws IOException
	{
		LongProcess reading = new LongProcess("reading arcs from " + from, " arc",
				nbArcsExpected);
		Section[] sections = findSections(from, nbThreads);

		Int2ObjectMap<int[]>[] localAdjs = new Int2ObjectMap[sections.length];

		if (hasNbVertices)
		{
			InputStream in = from.createReadingStream();
			Scanner sc = new Scanner(in);
			nbVerticesExpected = sc.nextInt();
			Cout.info(nbVerticesExpected + " vertices declared on first line");
			sc.close();
		}

		new MultiThreadProcessing(sections.length, reading)
		{
			@Override
			protected void runInParallel(ThreadSpecifics s, List<Thread> threads)
					throws Throwable
			{
				Int2ObjectMap<int[]> _localAdj = localAdjs[s.rank] = new Int2ObjectOpenHashMap<>(
						nbVerticesExpected >= 0 ? nbVerticesExpected / threads.size()
								: 0);

				Section b = sections[s.rank];
				InputStream _is = from.createReadingStream(0);
				Utilities.skip(_is, b.fromPosition);
				TextNumberReader _scanner = new TextNumberReader(_is, bufSize);

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
					if (_src == b.endVertex)
						break;

					int _nbNeighbors = _scanner.nextInt();

					int[] _outNeighbors = _nbNeighbors == 0 ? JmgUtils.emptyArray
							: new int[_nbNeighbors];

					for (int i = 0; i < _nbNeighbors; ++i)
					{
						int neighbor = _scanner.nextInt();
						_outNeighbors[i] = neighbor;
						++_nbEdge;
						++s.progressStatus;
					}

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
