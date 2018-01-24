package jmg.demo;

import java.io.IOException;

import jmg.Digraph;
import jmg.gen.GridGenerator;
import jmg.io.jmg.JMGDirectory;

public class GenerateGridAndSaveIt
{
	public static void main(String[] args) throws IOException
	{
		Digraph g = new Digraph();
		g.out.adj = GridGenerator.dgrid_outs(100, 100, true, true, false, false);
		g.nbVertices = g.out.adj.length;
		JMGDirectory d = new JMGDirectory("$HOME/tmp/grid100x100.jmg");
		g.write(d);
	}
}
