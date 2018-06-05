package jmg;

public class INs extends Direction
{
	@Override
	public int hashCode()
	{
		int hash = 0;

		if (mem.b != null)
		{
			for (int dest = 0; dest < mem.b.length; ++dest)
			{
				for (int src : mem.b[dest])
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
