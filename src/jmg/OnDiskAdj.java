package jmg;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import jmg.io.jmg.ArcFile;
import jmg.io.jmg.ArcFileVertexIterator.ArcFileCursor;

public class OnDiskAdj extends Adjacency
{
	public ArcFile file;

	@Override
	public int[] get(int u)
	{
		return file.readEntry(u).adj;
	}

	public void save(int[][] adj) throws IOException
	{
		file.writeADJ(adj);
	}

	public Set<ArcFileCursor> search(Predicate<ArcFileCursor> p)
	{
		ObjectSet<ArcFileCursor> r = new ObjectOpenHashSet<>();

		for (ArcFileCursor u : file)
		{
			if (p.test(u))
			{
				r.add(u);
			}
		}

		return r;
	}

	@Override
	public int[] degrees(int nbThreads)
	{
		return file.getDegrees(nbThreads);
	}

	@Override
	public boolean isDefined()
	{
		return file.exists();
	}

	@Override
	public IntSet findIsolatedVertices(int expectedNb, int nbThreads)
	{
		int nbEntries = file.getNbEntries();
		boolean[] found = new boolean[nbEntries];
		int[] degree = new int[nbEntries];

		for (ArcFileCursor c : file)
		{
			degree[c.vertex] = c.adj.length;

			for (int v : c.adj)
			{
				found[v] = true;
			}
		}

		IntSet r = new IntOpenHashSet();

		for (int u = 0; u < found.length; ++u)
		{
			if ( ! found[u] && degree[u] == 0)
			{
				r.add(u);
			}
		}

		return r;
	}

	@Override
	public Iterator<VertexCursor> iterator()
	{
		return (Iterator<VertexCursor>) (Object) file.iterator();
	}
}
