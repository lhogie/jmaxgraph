package jmg.gen;

import jmg.Digraph;
import jmg.Utils;
import jmg.io.DotWriter;
import toools.io.Cout;

public class CompleteGraph
{
	static public Digraph doit(int nbVertices, int nbThreads)
	{
		Digraph g = new Digraph();
		g.out.adj = new int[nbVertices][];

		for (int u = 0; u < nbVertices; ++u)
		{
			g.out.adj[u] = new int[nbVertices - 1];
		}

		for (int u = 0; u < nbVertices; ++u)
		{
			int i = 0;

			for (int v = 0; v < nbVertices; ++v)
			{
				if (v != u)
				{
					g.out.adj[u][i++] = v;
				}
			}
		}

		Utils.ensureSorted(g.out.adj, nbThreads);
		g.nbVertices = g.out.adj.length;
		return g;
	}

	public static void main(String[] args)
	{
		Digraph g = doit(5, 1);
		Cout.debug(DotWriter.toString(g.out.adj));
	}

}
