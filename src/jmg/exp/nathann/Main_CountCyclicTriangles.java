package jmg.exp.nathann;

import jmg.Graph;
import jmg.io.jmg.JMGDirectory;
import jmr.GlobalOutput;
import jmr.Problem;
import toools.io.Cout;
import toools.io.file.Directory;

public class Main_CountCyclicTriangles
{
	public static void main(String[] args)
	{
		JMGDirectory dataset = new JMGDirectory(args[0]);
		Graph g = dataset.mapGraph(1, false);

		Problem problem = new CountingCyclicTrianglesProblem(g, - 1);

		Cout.debug(g.getNbVertices());

		GlobalOutput r = problem.map(new Directory(dataset, "countTrianglesOnUndirected"),
				50, true, true);
		System.out.println(r);
	}
}
