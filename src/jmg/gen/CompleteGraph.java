package jmg.gen;

import jmg.Graph;
import jmg.JmgUtils;
import jmg.io.DotWriter;
import toools.io.Cout;

public class CompleteGraph
{
	static public Graph doit(int nbVertices, int nbThreads)
	{
		Graph g = new Graph();
		g.out.mem.b = new int[nbVertices][];

		for (int u = 0; u < nbVertices; ++u)
		{
			g.out.mem.b[u] = new int[nbVertices - 1];
		}

		for (int u = 0; u < nbVertices; ++u)
		{
			int i = 0;

			for (int v = 0; v < nbVertices; ++v)
			{
				if (v != u)
				{
					g.out.mem.b[u][i++] = v;
				}
			}
		}

		JmgUtils.ensureSorted(g.out.mem.b, nbThreads);
		return g;
	}

	public static void main(String[] args)
	{
		Graph g = doit(5, 1);
		Cout.debug(DotWriter.toString(g.out.mem.b));
	}

}
