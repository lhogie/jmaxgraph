package jmg;

public class OUTs extends Adjacency
{
	@Override
	public int hashCode()
	{
		int h = 0;

		for (int src = 0; src < adj.length; ++src)
		{
			for (int dest : adj[src])
			{
				int coupleHash = 31 + src;
				coupleHash = coupleHash * 31 + dest;
				h += coupleHash;
			}
		}

		return h;
	}
}
