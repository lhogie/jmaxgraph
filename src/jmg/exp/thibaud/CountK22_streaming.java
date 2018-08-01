package jmg.exp.thibaud;

import java.util.Iterator;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.JmgUtils;
import jmg.VertexCursor;
import jmg.io.jmg.JMGDirectory;
import jmg.plugins.JMGPlugin;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class CountK22_streaming extends JMGPlugin<Graph, CountK22v2_Result>
{

	@Override
	public CountK22v2_Result process(Graph g)
	{
		return count(g);
	}

	@Override
	public void setParameters(PluginParms p)
	{
	}

	public CountK22v2_Result count(Graph g)
	{
		if (g.jmgDirectory == null)
		{
			JMGDirectory d = new JMGDirectory("$HOME/tmp/flsjklkj");
			Graph h = new Graph(d, false, 1);
			g.out.mem.fill(g.out, 1, 0, nbThreads);
			g.in.mem.fill(g.in, 1, 0, nbThreads);

			if (d.exists())
				d.deleteRecursively();

			h.writeToDisk();
			g = h;
		}

		g.in.ensureLoaded(nbThreads);

		CountK22v2_Result globalResult = new CountK22v2_Result();

		LongProcess progressMonitor = new LongProcess("thibaud tracking K2,2", " vertex",
				g.getNbVertices());

		progressMonitor.temporaryResult = globalResult;
		int[][] hIns = g.in.mem.b;
		final Graph h = g;

		new ParallelIntervalProcessing(g.getNbVertices(), nbThreads, progressMonitor)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				long _sum_fractionalNbK22pot = 0;
				long _sum_twoTimesfractionalNbK22 = 0;
				Iterator<VertexCursor> vertexIterator = h.out.disk.iterator(lowerBound,
						upperBound, 1000, 256 * 256 * 256);

				while (vertexIterator.hasNext())
				{
					globalResult.nbVertices.incrementAndGet();
					VertexCursor u = vertexIterator.next();

					for (int v : u.adj)
					{
						for (int w : u.adj)
						{
							if (v < w)
							{
								int nbCN = JmgUtils.sizeOfIntersection(hIns[v], hIns[w]);

								int _twotimesfractionalNbK22 = (nbCN - 1);
								int dv = hIns[v].length;
								int dw = hIns[w].length;

								if (JmgUtils.contains(hIns[w], v))
								{
									--dv;
								}

								if (JmgUtils.contains(hIns[v], w))
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
