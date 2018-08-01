package jmg.gen;

import java.io.IOException;

import jmg.Graph;
import jmg.MatrixAdj;
import jmg.io.jmg.JMGDirectory;

public class Test
{
	public static void main(String[] args) throws IOException
	{
		int nbVertex = 1000;
		double p = 0.3;
		Graph g = new Graph(new JMGDirectory("$HOME/random_graph.jmg"));
		g.out.mem = new MatrixAdj(DirectedGNP.out(nbVertex, p, 0, true, 1), null, 1);
		g.writeToDisk();
	}
}
