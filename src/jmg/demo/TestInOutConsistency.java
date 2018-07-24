package jmg.demo;

import java.util.Random;

import jmg.Graph;
import jmg.JmgUtils;
import jmg.io.jmg.ArcFile;
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
		Graph g = new Graph(d);
		Random r = new Random();
		int n = g.getNbVertices();

		for (int i = 0;; ++i)
		{
			if (r.nextBoolean())
			{
				test(r, n, g.in.disk.getArcFile(), g.out.disk.getArcFile(), true);
			}
			else
			{
				test(r, n, g.out.disk.getArcFile(), g.in.disk.getArcFile(), true);

			}
		}
	}

	public static void test(Random r, int n, ArcFile f1, ArcFile f2,
			boolean stopAtFirstError)
	{
		int dest = r.nextInt(n);

		int[] ins = f1.readEntry(dest).adj;

		if (ins.length > 0)
		{
			int src = ins[r.nextInt(ins.length)];
			Cout.result("Testing if (" + src + " => " + dest + " in file " + f1
					+ " exists in " + f2);

			boolean ok = JmgUtils.contains(f2.readEntry(src).adj, dest);
			Cout.result(ok);

			if ( ! ok && stopAtFirstError)
				throw new IllegalStateException();
		}
	}
}
