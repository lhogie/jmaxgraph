package jmg.exp.nathann;

import jmg.Digraph;
import jmg.io.jmg.JMGDirectory;
import jmr.GlobalOutput;
import jmr.Problem;
import toools.io.Cout;

public class Main_CountCyclicTriangles
{
	public static void main(String[] args)
	{
		JMGDirectory dataset = new JMGDirectory(args[0]);
		Digraph g = dataset.mapGraph(1, false);

		Problem problem = new CountingCyclicTrianglesProblem(g, - 1);

		Cout.debug(g.getNbVertices());

		GlobalOutput r = problem.map("countTrianglesOnUndirected", 50);
		System.out.println(r);
	}
}
