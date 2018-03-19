package jmg;

public class INs extends Adjacency
{
	@Override
	public int hashCode()
	{
		int hash = 0;

		if (adj != null)
		{
			for (int dest = 0; dest < adj.length; ++dest)
			{
				for (int src : adj[dest])
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
