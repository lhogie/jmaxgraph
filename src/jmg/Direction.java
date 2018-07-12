package jmg;

import java.util.Iterator;

public class Direction extends Adjacency
{
	public enum NAME
	{
		in, out
	}

	public MatrixAdj mem = new MatrixAdj();
	public transient ArcFileAdj disk = new ArcFileAdj();
	public Direction opposite;

	public Adjacency getPreferredAdj()
	{
		if (mem.isDefined())
			return mem;

		if (disk.isDefined())
			return disk;

		return null;
	}



	public void ensureLoaded(int nbThreads)
	{
		// if not already loaded
		if ( ! mem.isDefined())
		{
			// it's on disk
			if (disk.isDefined())
			{
				mem.setAllFrom(disk, nbThreads);
			}
			else
			{
				// it's not but maybe it can be computed from the other
				// direction
				if ( ! opposite.isDefined())
					throw new IllegalStateException(
							"unable to load or compute this adjacency since no  opposite adjacency is defined");

				mem.setAllFrom(opposite.opposite(), nbThreads);
			}
		}
	}

	@Override
	public Iterator<VertexCursor> iterator()
	{
		return getPreferredAdj().iterator();
	}

	public boolean isDefined()
	{
		return getPreferredAdj() != null;
	}

	@Override
	public int[] get(int u)
	{
		return getPreferredAdj().get(u);
	}

	@Override
	public int[] degrees(int nbThreads)
	{
		return getPreferredAdj().degrees(nbThreads);
	}

	@Override
	public Iterator<VertexCursor> iterator(int from, int to)
	{
		return getPreferredAdj().iterator(from, to);
	}

	@Override
	public void setAllFrom(Adjacency from, int nbThreads)
	{
		getPreferredAdj().setAllFrom(from, nbThreads);
	}

	@Override
	public int countVertices(int nbThreads)
	{
		return getPreferredAdj().getNbVertices(nbThreads);
	}

}
