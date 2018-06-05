package jmg.io.adj;

import java.io.IOException;
import java.io.InputStream;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import j4u.chain.PluginConfig;
import jmg.Utils;
import toools.io.BinaryReader;
import toools.io.Cout;
import toools.progression.LongProcess;
import toools.util.Conversion;

public class ADJ32Reader extends ADJReader
{
	@Override
	public Int2ObjectMap<int[]> readFile() throws IOException
	{
		LongProcess lp = new LongProcess("reading from file", "B", from.getSize());
		Cout.info("using " + nbThreads + " threads");

		long _nbArcs = 0;

		InputStream _is = from.createReadingStream(0);
		BinaryReader _r = new BinaryReader(_is, 65536 * 256);
		int nbVertex = _r.nextInt();
		Cout.info(nbVertex + " vertices in file");
		Int2ObjectMap<int[]> _localAdj = new Int2ObjectOpenHashMap<>(nbVertex);

		long _nbBytesReadPreviously = 0;

		for (int i = 0; i < nbVertex; ++i)
		{
			long _nbByteReadNow = _r.getNbByteRead();
			lp.sensor.progressStatus += _nbByteReadNow - _nbBytesReadPreviously;
			_nbBytesReadPreviously = _nbByteReadNow;
			// System.out.println(i);
			int _src = Conversion.long2int(_r.nextInt());
			int _nbNeighbors = _r.nextInt();

			int[] _outNeighbors = _nbNeighbors == 0 ? Utils.emptyArray
					: new int[_nbNeighbors];

			_localAdj.put(_src, _outNeighbors);

			for (int ni = 0; ni < _nbNeighbors; ++ni)
			{
				_outNeighbors[ni] = _r.nextInt();
				++_nbArcs;
			}
		}

		lp.end(_localAdj.size() + " vertices and "+ _nbArcs + " arcs loaded");
		return _localAdj;
	}

	@Override
	public void setup(PluginConfig parms)
	{

	}

}
