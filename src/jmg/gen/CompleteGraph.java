package jmg.gen;

import jmg.Digraph;
import jmg.io.DotWriter;
import toools.io.Cout;

public class CompleteGraph
{
	static public Digraph doit(int nbVertices)
	{
		Digraph g = new Digraph();
		g.out = new int[nbVertices][];
		
		for (int u = 0; u < nbVertices; ++u)
		{
			g.out[u] = new int[nbVertices - 1];
		}

		for (int u = 0; u < nbVertices; ++u)
		{
			int i = 0;
			
			for (int v = 0; v < nbVertices; ++v)
			{
				if (v != u)
				{
					g.out[u][i++] = v;
				}
			}
		}

		return g;
	}
	
	public static void main(String[] args)
	{
		Digraph g = doit(5);
		Cout.debug(DotWriter.toString(g.out));
	}

}
