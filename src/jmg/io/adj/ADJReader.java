package jmg.io.adj;

import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import jmg.Digraph;
import jmg.io.DatasetReaderPlugin;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;

public abstract class ADJReader extends DatasetReaderPlugin
{
	public RegularFile from;
	

	@Override
	public Digraph read()
	{
		try
		{
			Int2ObjectMap<int[]> adj = readFile();
			return Digraph.from(adj, addUndeclared, relabel, sort);
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
			merging.progressStatus.addAndGet(a.size());
		}

		merging.end(bigMap.size() + " vertices loaded");
		return bigMap;
	}

}
