package jmg;

public class OUTs extends Direction
{
	
	@Override
	public int hashCode()
	{
		int hash = 0;

		if (mem.b != null)
		{
			for (int src = 0; src < mem.b.length; ++src)
			{
				for (int dest : mem.b[src])
				{
					int coupleHash = 31 + src;
					coupleHash = coupleHash * 31 + dest;
					hash += coupleHash;
				}
			}
		}

		return hash;
	}
}
