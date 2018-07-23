package jmg;

import toools.io.file.Directory;

public class OUTs extends Direction
{
	public OUTs(Directory d, int nbThreads)
	{
		super(d, nbThreads);
	}

	@Override
	public int hashCode()
	{
		if ( ! isDefined())
			throw new IllegalStateException();

		int hash = 0;

		for (int src = 0; src < mem.b.length; ++src)
		{
			for (int dest : mem.b[src])
			{
				int coupleHash = 31 + src;
				coupleHash = coupleHash * 31 + dest;
				hash += coupleHash;
			}
		}

		return hash;
	}
}
