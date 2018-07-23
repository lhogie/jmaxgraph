package jmg.demo;

import java.io.IOException;

import jmg.Graph;
import jmg.gen.GridGenerator;
import jmg.io.jmg.JMGDirectory;

public class GenerateGridAndSaveIt
{
	public static void main(String[] args) throws IOException
	{
		JMGDirectory d = new JMGDirectory("$HOME/tmp/grid100x100.jmg");
		Graph g = new Graph(d);
		g.out.mem.b = GridGenerator.dgrid_outs(100, 100, true, true, false, false, 2);
		g.writeToDisk();
	}
}
