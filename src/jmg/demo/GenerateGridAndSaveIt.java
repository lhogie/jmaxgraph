package jmg.demo;

import java.io.IOException;

import jmg.Graph;
import jmg.gen.GridGenerator;
import jmg.io.jmg.JMGDirectory;

public class GenerateGridAndSaveIt
{
	public static void main(String[] args) throws IOException
	{
		Graph g = new Graph();
		g.out.mem.b = GridGenerator.dgrid_outs(100, 100, true, true, false, false, 2);
		JMGDirectory d = new JMGDirectory("$HOME/tmp/grid100x100.jmg");
		g.write(d);
	}
}
