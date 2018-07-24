package jmg.exp.stephane;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

import jmg.Graph;
import jmg.JmgUtils;
import jmg.io.jmg.JMGDirectory;
import toools.SystemMonitor;
import toools.io.Cout;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class HowManyINsBigVerticesHave
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
		PrintStream out = new RegularFile("HowManyINsBigVerticesHave.csv")
				.createPrintStream();
		out.println("#bigGuys	#followers");
		LongProcess lp = new LongProcess("counting", " vertex",
				verticesSortedByInDegree.length);
		int howMany = 0;

		for (int i = 0; i < verticesSortedByInDegree.length; ++i)
		{
			int v = verticesSortedByInDegree[verticesSortedByInDegree.length - 1 - i];
			int nbMore = seen(seen, g.in.mem.b[v], nbThreads);
			howMany += nbMore;
			synchronized (out)
			{
				out.println(i + " " + howMany);

				if (i % 100 == 0)
					out.flush();
			}
			Cout.progress(i);
			lp.sensor.progressStatus++;
		}

		out.close();
		lp.end();
	}

	private static int seen(boolean[] seen, int[] s, int nbThreads)
	{
		AtomicInteger r = new AtomicInteger();

		new ParallelIntervalProcessing(s.length, nbThreads, null)
		{
			@Override
			protected void process(ThreadSpecifics sp, int lowerBound, int upperBound)
					throws Throwable
			{
				int _r = 0;

				for (int i = lowerBound; i < upperBound; ++i)
				{
					if ( ! seen[s[i]])
						++_r;

					seen[s[i]] = true;
				}

				r.addAndGet(_r);
			}
		};

		return r.get();
	}
}
