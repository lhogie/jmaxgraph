package jmg.exp.stephane;

import java.io.PrintStream;

import jmg.Graph;
import jmg.JmgUtils;
import jmg.io.jmg.JMGDirectory;
import toools.SystemMonitor;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;

public class HowManyINsBigVerticesHave_seq
{
	public static void main(String[] args)
	{
		SystemMonitor.defaultMonitor.start();
		String path = "$HOME/datasets/twitter/big.jmg";

		JMGDirectory d = new JMGDirectory(path);
		int nbThreads = Runtime.getRuntime().availableProcessors();
		Graph g = new Graph(d, false, nbThreads);
		int[] verticesSortedByInDegree = JmgUtils.sortVerticesBy(g.in.degrees());

		g.in.ensureLoaded(8);

		boolean[] seen = new boolean[g.in.mem.b.length];
		PrintStream out = new RegularFile("HowManyINsBigVerticesHave_seq.csv")
				.createPrintStream();
		out.println("#bigGuys	#followers");
		LongProcess lp = new LongProcess("counting", " vertex",
				verticesSortedByInDegree.length);
		int nbSeen = 0;

		for (int i = 0; i < verticesSortedByInDegree.length; ++i)
		{
			int v = verticesSortedByInDegree[verticesSortedByInDegree.length - 1 - i];
			nbSeen += seen(seen, g.in.mem.b[v], nbThreads);
			out.print(i);
			out.print('\t');
			out.print(nbSeen);
			out.println();

			if (i % 100 == 0)
				out.flush();

			lp.sensor.progressStatus++;
		}

		out.close();
		lp.end();
	}

	private static int seen(boolean[] seen, int[] s, int nbThreads)
	{
		int r = 0;

		int len = s.length;

		for (int i = 0; i < len; ++i)
		{
			if ( ! seen[s[i]])
				++r;

			seen[s[i]] = true;
		}

		return r;
	}
}
