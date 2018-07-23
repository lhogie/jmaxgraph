package jmg;

import toools.io.file.Directory;

public class INs extends Direction
{
	public INs(Directory d, int nbThreads)
	{
		super(d, nbThreads);
	}

	@Override
	public int hashCode()
	{
		if ( ! isDefined())
			throw new IllegalStateException();

		int hash = 0;

		for (int dest = 0; dest < mem.b.length; ++dest)
		{
			for (int src : mem.b[dest])
			{
				int coupleHash = 31 + src;
				coupleHash = coupleHash * 31 + dest;
				hash += coupleHash;
			}
		}

		return hash;
	}
}
