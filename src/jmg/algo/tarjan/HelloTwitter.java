package jmg.algo.tarjan;

import jmg.Graph;
import jmg.io.jmg.JMGDirectory;

public class HelloTwitter
{
	public static void main(String[] args)
	{
		// the dataset for the graph is there
		JMGDirectory d = new JMGDirectory("/home/lhogie/datasets/twitter/big.jmg");

		// maps a graph on the directory
		Graph g = new Graph(d);

		// makes sure the OUT adj is in RAM
		// if it was not, loads it
		g.out.ensureLoaded(8);

		// runs Tarjan
		int nbSCCs = new Tarjan(g).count();
		System.out.println("nbSCCs: " + nbSCCs);
	}
}
