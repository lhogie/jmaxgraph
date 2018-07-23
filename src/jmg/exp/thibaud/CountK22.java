package jmg.exp.thibaud;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.JmgUtils;
import jmg.chain.JMGPlugin;
import toools.io.Cout;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class CountK22 extends JMGPlugin<Graph, CountK22_Result>
{

	@Override
	public CountK22_Result process(Graph g)
	{
		return count(g);
	}

	@Override
	public void setParameters(PluginParms p)
	{
	}

	public CountK22_Result count(Graph g)
	{
		g.out.ensureLoaded(nbThreads);
		g.in.ensureLoaded(nbThreads);

		CountK22_Result r = new CountK22_Result();
		int maxDegree = g.out.mem.maxDegree();
		Cout.info("max degree=" + maxDegree);
		r.distri = new int[maxDegree + 1];

		LongProcess l = new LongProcess("tracking K2,2", " vertex", g.getNbVertices());

		l.temporaryResult = r;

		new ParallelIntervalProcessing(g.getNbVertices(), nbThreads, l)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				long _sum_fourTimesNbK22pot = 0;
				long _sum_nK22 = 0;
				int[] _distri = new int[maxDegree + 1];
				long lastK22saved = 0;

				for (int u = lowerBound; u < upperBound; ++u)
				{
					IntSet alreadyDone = new IntOpenHashSet();

					for (int v : g.out.mem.b[u])
					{
						for (int w : g.in.mem.b[v])
						{
							if (u < w && ! alreadyDone.contains(w))
							{
								alreadyDone.add(w);
								int nbCN = JmgUtils.sizeOfIntersection(
										g.out.mem.b[u], g.out.mem.b[w]);

								++_distri[nbCN];

								int _nK22 = nbCN * (nbCN - 1) / 2;
								int du = g.out.mem.b[u].length;
								int dw = g.out.mem.b[w].length;

								if (g.arcExists(u, w))
								{
									--du;
								}

								if (g.arcExists(w, u))
								{
									--dw;
								}

								int _fourTimeNbK22pot = nbCN * (du + dw - 2);

								_sum_nK22 += _nK22;
								_sum_fourTimesNbK22pot += _fourTimeNbK22pot;
							}
						}
					}

					++s.progressStatus;

					if (u % 1000 == 0 && lastK22saved != _sum_nK22)
					{
						synchronized (r)
						{
							r.nbK22pot += _sum_fourTimesNbK22pot;
							_sum_fourTimesNbK22pot = 0;
							r.nbK22 += _sum_nK22;
							lastK22saved = _sum_nK22;
							_sum_nK22 = 0;

							for (int i = 0; i < maxDegree; ++i)
							{
								r.distri[i] += _distri[i];
								_distri[i] = 0;
							}
						}
					}
				}

				synchronized (r)
				{
					r.nbK22pot += _sum_fourTimesNbK22pot;
					r.nbK22 += _sum_nK22;

					for (int i = 0; i < maxDegree; ++i)
					{
						r.distri[i] += _distri[i];
					}
				}
			}
		};

		l.end();
		return r;
	}

}
