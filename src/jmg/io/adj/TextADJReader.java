package jmg.io.adj;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java4unix.pluginchain.PluginConfig;
import jmg.Utils;
import toools.io.Cout;
import toools.io.TextNumberReader;
import toools.io.Utilities;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.util.Conversion;

public class TextADJReader extends ADJReader
{
	public int nbVerticesExpected;
	public boolean hasNbVertices;
	public boolean hasDegree;

	@Override
	public Int2ObjectMap<int[]> readFile() throws IOException
	{nbThreads=1;
		LongProcess reading = new LongProcess("reading ADJ from " + from, "B",
				from.getSize());
		Cout.info("using " + nbThreads + " threads");
		Section[] sectionPosititions = findSection(from, nbThreads);
		Int2ObjectMap<int[]>[] localAdjs = new Int2ObjectMap[nbThreads];

		if (hasNbVertices)
		{
			InputStream in = from.createReadingStream();
			Scanner sc = new Scanner(in);
			nbVerticesExpected = sc.nextInt();
			Cout.info(nbVerticesExpected + " vertices declared on first line");
			sc.close();
		}

		new MultiThreadProcessing(nbThreads)
		{
			@Override
			protected void runInParallel(int _rank, List<Thread> threads) throws Throwable
			{
				Int2ObjectMap<int[]> _localAdj = new Int2ObjectOpenHashMap<>(
						nbVerticesExpected / NB_THREADS_TO_USE);
				localAdjs[_rank] = _localAdj;
				Section b = sectionPosititions[_rank];
				InputStream _is = from.createReadingStream(0);

				if (_is.skip(b.from) != b.from)
					throw new IllegalStateException();

				TextNumberReader _scanner = new TextNumberReader(_is, bufSize);

				if (_rank == 0 && hasNbVertices)
				{
					// skip the number of vertices in the file
					_scanner.nextLong();
				}

				long _nbBytesReadPreviously = 0;
				long _nbEdge = 0;

				while (_scanner.hasNext())
				{
					long _nbByteReadNow = _scanner.getNbByteRead();
					reading.progressStatus
							.addAndGet(_nbByteReadNow - _nbBytesReadPreviously);
					_nbBytesReadPreviously = _nbByteReadNow;

					int _src = Conversion.long2int(_scanner.nextLong());

					// if it reaches the end of the section
					if (_src == b.to)
						break;

					int _nbNeighbors = _scanner.nextInt();

					if (true)// hasDegree)
					{
						int[] _outNeighbors = _nbNeighbors == 0 ? Utils.emptyArray
								: new int[_nbNeighbors];

						for (int i = 0; i < _nbNeighbors; ++i)
						{
							int neighbor = _scanner.nextInt();
							_outNeighbors[i] = neighbor;
							++_nbEdge;
						}

						_localAdj.put(_src, _outNeighbors);
					}
					else
					{
						IntList _outNeighbors = new IntArrayList();

						for (int i = 0; i < _nbNeighbors; ++i)
						{
							int neighbor = _scanner.nextInt();
							_outNeighbors.add(neighbor);
							++_nbEdge;
						}

						_localAdj.put(_src, _outNeighbors.toIntArray());
					}
				}

				Cout.progress("thread " + _rank + " has read " + _localAdj.size()
						+ " vertices and " + _nbEdge + " edges");
			}
		};

		reading.end();
		Int2ObjectMap<int[]> bigMap = merge(localAdjs);
		return bigMap;
	}

	static class Section
	{
		long from;
		long to = - 1;
	}

	Section[] findSection(RegularFile f, int nbSection) throws IOException
	{
		final long sectionLength = f.getSize() / nbSection;
		Section[] sectionBounds = new Section[nbSection];

		for (int i = 0; i < nbSection; ++i)
		{
			sectionBounds[i] = new Section();
			sectionBounds[i].from = i * sectionLength;
		}

		for (int sectionIndex = 0; sectionIndex < nbSection - 1; ++sectionIndex)
		{
			InputStream is = f.createReadingStream();
			Section section = sectionBounds[sectionIndex];

			if (is.skip(section.from + sectionLength) != section.from + sectionLength)
				throw new IllegalStateException();

			int skip = Utilities.skipUntilEndOfLine(is);

			// if we're not building the last section bounds
			if (sectionIndex < nbSection - 1)
			{
				section.to = new Scanner(is).nextLong();
				sectionBounds[sectionIndex + 1].from = section.from + sectionLength
						+ skip;
			}

			is.close();
		}

		return sectionBounds;
	}

	@Override
	public void setup(PluginConfig parms)
	{
		super.setup(parms);

		hasNbVertices = parms.getBoolean("hasNbVertices");
		// hasDegree = parms.getBoolean("hasDegree");

		if (parms.contains("n"))
			nbVerticesExpected = parms.getInt("n");
	}

}
