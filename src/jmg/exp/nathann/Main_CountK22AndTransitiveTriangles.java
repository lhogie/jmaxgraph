package jmg.exp.nathann;

import jmg.Digraph;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;

public class Main_CountK22AndTransitiveTriangles
{
	public static void main(String[] args)
	{
		JMGDirectory dataset = new JMGDirectory(args[0]);
		Digraph g = dataset.mapGraph(1, false);

		CountingK22AndTransitiveTrianglesProblem problem = new CountingK22AndTransitiveTrianglesProblem(
				g, true, - 1);

		int nbJobs = Integer.valueOf(args[1]);

		GlobalCount r = problem
				.map(args[2], nbJobs);
		Cout.result(r);
		//r.save();

	}
}
