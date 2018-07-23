package jmg;

import java.util.Iterator;

import jmg.algo.ReverseGraph;
import jmg.io.jmg.ArcFile;
import toools.io.file.Directory;

public class Direction extends Adjacency
{
	public enum NAME
	{
		in, out
	}

	public MatrixAdj mem;
	public transient ArcFileAdj disk;
	public Direction opposite;

	public Direction(Directory d, int nbThreads)
	{
		super(d, nbThreads);
		this.mem = new MatrixAdj(null, d, nbThreads);
		this.disk = d == null ? null : new ArcFileAdj(new ArcFile(d, "arcs"), nbThreads);
	}

	@Override
	public String toString()
	{
		return "use RAM: " + (mem != null) + ", disk file: " + disk;
	}

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

				mem.setAllFrom(opposite.opposite(nbThreads), nbThreads);
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
	public int[] degrees()
	{
		if (isDefined())
		{
			return getPreferredAdj().degrees();
		}
		else if (opposite.isDefined())
		{
			return ReverseGraph.computeReverseDegrees(getNbVertices(), opposite);
		}

		throw new IllegalStateException(
				"no possibility to retrieve the number of vertices");
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
		if (isDefined())
		{
			return getPreferredAdj().getNbVertices();
		}
		else if (opposite.isDefined())
		{
			return opposite.getPreferredAdj().getNbVertices();
		}

		throw new IllegalStateException(
				"no possibility to retrieve the number of vertices");
	}

}
