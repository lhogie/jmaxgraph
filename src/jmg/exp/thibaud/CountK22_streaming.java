package jmg.exp.thibaud;

import java.io.IOException;
import java.util.Iterator;

import java4unix.pluginchain.PluginConfig;
import jmg.Digraph;
import jmg.Utils;
import jmg.chain.JMGPlugin;
import jmg.io.jmg.EDGFileVertexIterator.EDGFileCursor;
import jmg.io.jmg.JMGDirectory;
import toools.io.IORuntimeException;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class CountK22_streaming extends JMGPlugin<Digraph, CountK22v2_Result>
{

	@Override
	public CountK22v2_Result process(Digraph g)
	{
		return count(g);
	}

	@Override
	public void setup(PluginConfig p)
	{
	}

	public CountK22v2_Result count(Digraph g)
	{
		if (g.dataset == null)
		{
			JMGDirectory d = new JMGDirectory("$HOME/tmp/flsjklkj");

			if (d.exists())
				d.deleteRecursively();

			g.out.ensureDefined(nbThreads);
			g.in.ensureDefined(nbThreads);

			try
			{
				g.write(d);
			}
			catch (IOException e)
			{
				throw new IORuntimeException(e);
			}

			g.setDataset(d);
		}

		g.in.ensureDefined(nbThreads);

		CountK22v2_Result globalResult = new CountK22v2_Result();

		LongProcess progressMonitor = new LongProcess("thibaud tracking K2,2",
				g.getNbVertex());

		progressMonitor.temporaryResult = globalResult;

		new ParallelIntervalProcessing(g.getNbVertex(), nbThreads, progressMonitor)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				long _sum_fractionalNbK22pot = 0;
				long _sum_twoTimesfractionalNbK22 = 0;
				Iterator<EDGFileCursor> vertexIterator = g.out.file.iterator(lowerBound,
						upperBound, 1000, 256 * 256 * 256);

				while (vertexIterator.hasNext())
				{
					globalResult.nbVertices.incrementAndGet();
					EDGFileCursor u = vertexIterator.next();

					for (int v : u.adj)
					{
						for (int w : u.adj)
						{
							if (v < w)
							{
								int nbCN = Utils.countElementsInCommon_dichotomic(
										g.in.adj[v], g.in.adj[w]);

								int _twotimesfractionalNbK22 = (nbCN - 1);
								int dv = g.in.adj[v].length;
								int dw = g.in.adj[w].length;

								if (Utils.contains(g.in.adj[w], v))
								{
									--dv;
								}

								if (Utils.contains(g.in.adj[v], w))
								{
									--dw;
								}

								int _fractionalNbK22pot = dv + dw - 2;

								_sum_twoTimesfractionalNbK22 += _twotimesfractionalNbK22;
								_sum_fractionalNbK22pot += _fractionalNbK22pot;
							}
						}
					}

					++s.progressStatus;

					if (false)// progressMonitor.progressStatus % 1000 < 1)
					{
						synchronized (globalResult)
						{
							globalResult.nbK22pot += _sum_fractionalNbK22pot;
							_sum_fractionalNbK22pot = 0;
						}
					}
				}

				synchronized (globalResult)
				{
					globalResult.nbK22 += _sum_twoTimesfractionalNbK22;
					globalResult.nbK22 /= 2;
					globalResult.nbK22pot += _sum_fractionalNbK22pot;
				}
			}
		};

		progressMonitor.end();
		return globalResult;
	}
}
