package jmg.exp;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.Utils;
import jmg.algo.Degrees;
import toools.io.Cout;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class CountK2_2_Thibaud implements TooolsPlugin<Digraph, CountK2_2_Thilbaud_Result>
{
	@Override
	public CountK2_2_Thilbaud_Result process(Digraph g)
	{
		g.ensureBothDirections();

		CountK2_2_Thilbaud_Result r = new CountK2_2_Thilbaud_Result();
		int maxDegree = Degrees.maxDegree(g.out);
		Cout.info("max degree=" + maxDegree);
		r.distri = new int[maxDegree + 1];

		LongProcess l = new LongProcess("tracking K2,2", g.getNbVertex());

		l.temporaryResult = r;

		new ParallelIntervalProcessing(g.getNbVertex())
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				long _sum_fourTimesNbK22pot = 0;
				long _sum_nK22 = 0;
				int[] _distri = new int[maxDegree + 1];
				long lastK22saved = 0;

				for (int u = lowerBound; u < upperBound; ++u)
				{
					IntSet alreadyDone = new IntOpenHashSet();

					for (int v : g.out[u])
					{
						for (int w : g.in[v])
						{
							if (u < w && ! alreadyDone.contains(w))
							{
								alreadyDone.add(w);
								int nbCN = Utils.countElementsInCommon_dichotomic(
										g.out[u], g.out[w]);

								++_distri[nbCN];

								int _nK22 = nbCN * (nbCN - 1) / 2;
								int du = g.out[u].length;
								int dw = g.out[w].length;

								if (g.exists(u, w))
								{
									--du;
								}

								if (g.exists(w, u))
								{
									--dw;
								}

								int _fourTimeNbK22pot = nbCN * (du + dw - 2);

								_sum_nK22 += _nK22;
								_sum_fourTimesNbK22pot += _fourTimeNbK22pot;
							}
						}
					}

					l.progressStatus.incrementAndGet();

					if (u % 1000 == 0 && lastK22saved != _sum_nK22)
					{
						synchronized (r)
						{
							r.fourTimesNbK22pot += _sum_fourTimesNbK22pot;
							_sum_fourTimesNbK22pot = 0;
							r.nK22 += _sum_nK22;
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
					r.fourTimesNbK22pot += _sum_fourTimesNbK22pot;
					r.nK22 += _sum_nK22;

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

	@Override
	public void setup(PluginConfig p)
	{
	}
}
