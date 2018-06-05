package jmg.io.adj;

import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import j4u.chain.PluginConfig;
import jmg.Digraph;
import jmg.Labelling;
import jmg.io.DatasetReaderPlugin;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;

public abstract class ADJReader extends DatasetReaderPlugin
{
	public RegularFile from;
	public int nbVerticesExpected = - 1;
	public long nbArcsExpected = - 1;

	@Override
	public void setup(PluginConfig parms)
	{
		super.setup(parms);
		nbVerticesExpected = parms.getInt("nbVertices");
		nbArcsExpected = parms.getLong("nbArcs");

		if (parms.contains("from"))
			from = new RegularFile(parms.get("from"));

		if (parms.containsAndRemove("noconsolidate"))
		{
			addUndeclared = relabel = sort = false;
		}
	}

	@Override
	public Digraph read()
	{
		try
		{
			Int2ObjectMap<int[]> adj = readFile();
			Digraph g = new Digraph();
			g.labelling = new Labelling();
			g.out.mem.from(adj, addUndeclared, sort, g.labelling, nbThreads);
			return g;
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	protected abstract Int2ObjectMap<int[]> readFile() throws IOException;

	public static <E> Int2ObjectMap<E> merge(Int2ObjectMap<E>[] adj)
	{
		if (adj.length == 1)
			return adj[0];

		int totalSize = 0;

		for (Int2ObjectMap<E> a : adj)
		{
			totalSize += a.size();
		}

		LongProcess merging = new LongProcess("merging threads output", " vertices",
				totalSize);
		Int2ObjectMap<E> bigMap = new Int2ObjectOpenHashMap<>(totalSize);

		for (Int2ObjectMap<E> a : adj)
		{
			bigMap.putAll(a);
			merging.sensor.progressStatus += a.size();
		}

		merging.end(bigMap.size() + " vertices loaded");
		return bigMap;
	}

}
