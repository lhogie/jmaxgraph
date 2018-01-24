package jmg.demo;

import java.io.IOException;

import jmg.Digraph;
import jmg.io.jmg.JMGDirectory;
import toools.SystemMonitor;
import toools.io.Cout;

public class GenerateOppositeADJ
{
	public static void main(String[] args) throws IOException
	{
		Cout.debug("start");
		SystemMonitor.defaultMonitor.start();

		JMGDirectory d = new JMGDirectory(args[0]);
		Digraph g = d.readDirectory(8, false);

		if (g.out.file.exists() && ! g.in.file.exists())
		{
			Cout.info("Generating IN adj");
			g.in.adj = g.out.file.readAndComputeOppositeADJ(8);
			g.in.save();
		}
		else if (g.in.file.exists() && ! g.out.file.exists())
		{
			Cout.info("Generating OUT adj");
			g.out.adj = g.in.file.readAndComputeOppositeADJ(8);
			g.out.save();
		}
		else
		{
			Cout.info("Doing nothing");
		}

	}
}
