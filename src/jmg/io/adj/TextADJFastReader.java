package jmg.io.adj;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import toools.io.Cout;
import toools.io.TextNumberReader;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.util.Conversion;

public class TextADJFastReader extends TextADJReader
{
	@Override
	public Int2ObjectMap<int[]> readFile() throws IOException
	{
		// don't use multi-threading if the file is small
		int nbT = from.getSize() < 1000000 ? 1 : nbThreads;

		LongProcess reading = new LongProcess("reading arcs from " + from, " arc",
				nbArcsExpected);
		Cout.debugSuperVisible("nbVerticesExpected="+nbVerticesExpected);
		Section[] sectionPosititions = findSection(from, nbT);
		Cout.debugSuperVisible(Arrays.toString(sectionPosititions));

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
						nbVerticesExpected / threads.size());
				localAdjs[s.rank] = _localAdj;
				Section b = sectionPosititions[s.rank];
				InputStream _is = from.createReadingStream(0);

				if (_is.skip(b.fromPosition) != b.fromPosition)
					throw new IllegalStateException();

				TextNumberReader _scanner = new TextNumberReader(_is, bufSize);

				if (s.rank == 0 && hasNbVertices)
				{
					// skip the number of vertices in the file
					_scanner.nextLong();
				}

				long _nbBytesReadPreviously = 0;
				long _nbEdge = 0;

				while (_scanner.hasNext())
				{
					long _nbByteReadNow = _scanner.getNbByteRead();
			//		s.progressStatus += _nbByteReadNow - _nbBytesReadPreviously;
					_nbBytesReadPreviously = _nbByteReadNow;

					int _src = Conversion.long2int(_scanner.nextLong());

					// if it reaches the end of the section
					if (_src == b.endVertex)
						break;

					int _nbNeighbors = _scanner.nextInt();

					//int[] _outNeighbors = _nbNeighbors == 0 ? Utils.emptyArray
					//		: new int[_nbNeighbors];

					for (int i = 0; i < _nbNeighbors; ++i)
					{
						int neighbor = _scanner.nextInt();
					//	_outNeighbors[i] = neighbor;
						++_nbEdge;
					}
					
					s.progressStatus += _nbNeighbors;


				//	_localAdj.put(_src, _outNeighbors);
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
