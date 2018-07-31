package jmg.algo.tarjan;

import jmg.Graph;
import jmg.io.jmg.JMGDirectory;

public class HelloTwitter
{
	public static void main(String[] args)
	{
		JMGDirectory d = new JMGDirectory("/home/lhogie/datasets/twitter/big.jmg");
		Graph g = new Graph(d);
		int nbSCCs = new Tarjan(g).count();
		System.out.println("nbSCCs: " + nbSCCs);
	}
}
