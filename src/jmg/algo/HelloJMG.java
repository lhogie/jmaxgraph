package jmg.algo;

import java.util.Arrays;
import java.util.Random;

import jmg.Graph;
import jmg.MatrixAdj;
import jmg.gen.DirectedGNP;
import toools.io.Cout;

public class HelloJMG
{
	public static int[] compute(Graph g)
	{
		int[] r = new int[g.getNbVertices()];

		return r;
	}

	public static void main(String[] args)
	{
		Graph g = new Graph();
		
		// gets the OUT adjacencies for all vertices
		MatrixAdj outs = g.out.mem;
		
		outs.b = DirectedGNP.out(100, 0.1, new Random(), false, 1);

		int nbVertices = outs.b.length;

		for (int u = 0; u < nbVertices; ++u)
		{
			//Cout.debug(outs.b[u]);
			System.out.println(u + ": " + Arrays.toString(outs.b[u]));
		}

		int[] partitions = compute(g);

	}
}
