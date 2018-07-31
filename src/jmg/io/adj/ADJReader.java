package jmg.io.adj;

import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import j4u.chain.PluginParms;
import jmg.Graph;
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
	public void setParameters(PluginParms parms)
	{
		super.setParameters(parms);
		nbVerticesExpected = parms.contains("nbVertices") ? parms.getInt("nbVertices")
				: - 1;
		nbArcsExpected = parms.contains("nbArcs") ? parms.getLong("nbArcs") : - 1;

		if (parms.contains("from"))
			from = new RegularFile(parms.get("from"));

		if (parms.containsAndRemove("noconsolidate"))
		{
			addUndeclared = relabel = sort = false;
		}
	}

	@Override
	public Graph read()
	{
		try
		{
			Int2ObjectMap<int[]> adj = readFile();
			Graph g = new Graph();
			g.labelling = relabel ? new Labelling() : null;
			g.out.mem.from(adj, addUndeclared, sort, g.labelling, nbThreads);
			return g;
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public Int2ObjectMap<int[]> readFile(RegularFile f) throws IOException
	{
		from = f;
		return readFile();
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
