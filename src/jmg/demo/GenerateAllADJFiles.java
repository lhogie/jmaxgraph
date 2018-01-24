package jmg.demo;

import java.io.IOException;

import jmg.Digraph;
import jmg.io.jmg.JMGDirectory;
import toools.SystemMonitor;
import toools.io.Cout;

public class GenerateAllADJFiles
{
	public static void main(String[] args) throws IOException
	{
		Cout.debug("start");
		SystemMonitor.defaultMonitor.start();

		JMGDirectory d = new JMGDirectory(args[0]);
		Digraph g = d.readDirectory(8, false);
		g.ensureADJLoaded();

		if (g.out.adj == null)
		{
			g.out.computeFromOppositeDirection();
			g.out.save();
		}
		else if (g.in.adj == null)
		{
			g.in.computeFromOppositeDirection();
			g.in.save();
		}

	}
}
