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
		Digraph g = d.mapGraph(8, false);

		if (g.out.disk.file.exists() && ! g.in.disk.file.exists())
		{
			Cout.info("Generating IN adj");
			g.in.mem.b = g.out.disk.file.readAndComputeOppositeADJ(8);
			g.in.disk.save(g.in.mem.b);
		}
		else if (g.in.disk.file.exists() && ! g.out.disk.file.exists())
		{
			Cout.info("Generating OUT adj");
			g.out.mem.b = g.in.disk.file.readAndComputeOppositeADJ(8);
			g.out.disk.save(g.out.mem.b);
		}
		else
		{
			Cout.info("Doing nothing");
		}
	}

}
