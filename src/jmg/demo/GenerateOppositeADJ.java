package jmg.demo;

import java.io.IOException;

import jmg.Graph;
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
		Graph g = d.mapGraph(8, false);

		if (g.out.disk.isDefined() && ! g.in.disk.isDefined())
		{
			Cout.info("Generating IN adj");
			g.in.disk.setAllFrom(g.out.disk.opposite(), 8);
		}
		else if (g.in.disk.isDefined() && ! g.out.disk.isDefined())
		{
			Cout.info("Generating OUT adj");
			g.out.disk.setAllFrom(g.in.disk.opposite(), 8);
		}
		else
		{
			Cout.info("Doing nothing");
		}
	}

}
