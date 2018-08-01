package jmg.algo.tarjan;

import java.io.IOException;

import jmg.Graph;
import jmg.io.jmg.JMGDirectory;

public class DemoNefFromEclipse
{
	public static void main(String[] args) throws IOException
	{
		Graph g = new Graph(new JMGDirectory("$HOME/datasets/0.0005.jmg/"));
		System.out.println(g.getNbVertices());
	}

}
