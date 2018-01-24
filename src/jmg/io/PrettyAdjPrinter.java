package jmg.io;

import java.util.Arrays;

public class PrettyAdjPrinter
{

	public static String f(int[][] adj)
	{
		StringBuilder b = new StringBuilder();

		for (int u = 0; u < adj.length; ++u)
		{
			b.append(u + " => " + Arrays.toString(adj[u]) + "\n");
		}

		return b.toString();
	}
}
