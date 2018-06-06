package jmg;

import java.io.Serializable;
import java.util.Iterator;

import jmg.algo.ReverseGraph;

public class Direction implements Serializable, Iterable<VertexCursor>
{
	public enum NAME {IN, OUT}
	
	public InMemoryAdj mem = new InMemoryAdj();
	public transient OnDiskAdj disk = new OnDiskAdj();
	public Direction opposite;

	
	public void load(double edgeProbability, long seed, int nbThreads)
	{
		mem.b = disk.file.readADJ(edgeProbability, seed, nbThreads);
	}

	
	public void load(int nbThreads)
	{
		mem.b = disk.file.readADJ(1, 0, nbThreads);
	}
	
	public void computeFromOppositeDirection()
	{
		if (mem.b != null)
		{
			mem.b = null;
			System.gc();
		}

		mem.b = ReverseGraph.computeInverseADJ(opposite.mem.b, false);
	}

	public void ensureDefined(int nbThreads)
	{
		if ( ! mem.isDefined())
		{
			// it's on disk
			if (disk.file != null && disk.file.exists())
			{
				load(nbThreads);
			}
			else
			{
				// it's not but maybe it can be computed from the other
				// direction

				if ( ! opposite.mem.isDefined() && ! opposite.disk.isDefined())
					throw new IllegalStateException(
							"unable to load or compute this adjacency since no file or opposite adjacency is available");

				opposite.ensureDefined(nbThreads);
				computeFromOppositeDirection();
			}
		}
	}

	@Override
	public Iterator<VertexCursor> iterator()
	{
		if (mem.isDefined())
		{
			return mem.iterator();
		}
		else if (disk.isDefined())
		{
			return disk.iterator();
		}
		else
		{
			throw new IllegalStateException("no adj to iterate on");
		}
	}

}
