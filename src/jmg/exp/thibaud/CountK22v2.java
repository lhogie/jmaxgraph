package jmg.exp.thibaud;

import java.util.Iterator;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.Utils;
import jmg.algo.Degrees;
import jmg.io.jmg.EDGFileVertexIterator.EDGFileCursor;
import toools.io.Cout;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class CountK22v2 implements TooolsPlugin<Digraph, CountK22v2_Result>
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

	public static CountK22v2_Result count(Digraph g)
	{
		g.out.ensureDefined();
		g.in.ensureDefined();

		CountK22v2_Result r = new CountK22v2_Result();
		int maxDegree = Degrees.maxDegree(g.out.adj);
		Cout.info("max degree=" + maxDegree);

		LongProcess l = new LongProcess("tracking K2,2", g.getNbVertex());

		l.temporaryResult = r;

		new ParallelIntervalProcessing(g.getNbVertex())
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				long _sum_fractionalNbK22pot = 0;
				long _sum_twotimesfractionalNbK22 = 0;
				Iterator<EDGFileCursor> vertexIterator = g.out.file.iterator(lowerBound,
						upperBound, 100);

				while (vertexIterator.hasNext())
				{
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

								_sum_twotimesfractionalNbK22 += _twotimesfractionalNbK22;
								_sum_fractionalNbK22pot += _fractionalNbK22pot;
							}
						}
					}

					++l.progressStatus;

					if (u.vertex % 1000 == 0)
					{
						synchronized (r)
						{
							r.nK22 = _sum_twotimesfractionalNbK22 / 2;
							r.nbK22pot = _sum_fractionalNbK22pot;
							_sum_fractionalNbK22pot = 0;
							_sum_twotimesfractionalNbK22 = 0;
						}
					}
				}

				synchronized (r)
				{
					r.nK22 = _sum_twotimesfractionalNbK22 / 2;
					r.nbK22pot = _sum_fractionalNbK22pot;
				}
			}
		};

		l.end();
		return r;
	}
}
