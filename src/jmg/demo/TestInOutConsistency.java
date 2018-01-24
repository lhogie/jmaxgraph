package jmg.demo;

import java.util.Random;

import jmg.Digraph;
import jmg.Utils;
import jmg.io.jmg.JMGDirectory;
import toools.SystemMonitor;
import toools.io.Cout;

public class TestInOutConsistency
{
	public static void main(String[] args)
	{
		Cout.debug("start");
		SystemMonitor.defaultMonitor.start();

		JMGDirectory d = new JMGDirectory(args[0]);
		Digraph g = d.readDirectory(8, false);
		Cout.debug(g);
		Random r = new Random();

		for (int i = 0; ; ++i)
		{
			int u = r.nextInt(g.getNbVertex());

			int[] ins = g.in.file.readEntry(u);
			
			if (ins.length > 0)
			{
				int v = ins[r.nextInt(ins.length)];
				Cout.debug(Utils.contains(g.out.file.readEntry(v), u));
				--i;
			}

		}
	}
}
