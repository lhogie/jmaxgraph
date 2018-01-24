package jmg.exp.nathann;

import java.io.IOException;
import java.util.Iterator;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.io.jmg.EDGFileVertexIterator.EDGFileCursor;
import jmg.io.jmg.EDGParallelProcessor;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;
import toools.io.IORuntimeException;
import toools.progression.LongProcess;

public class CountK22 implements TooolsPlugin<Digraph, CountK22_Result>
{

	@Override
	public CountK22_Result process(Digraph g)
	{
		return count(g);
	}

	@Override
	public void setup(PluginConfig p)
	{
	}

	public static CountK22_Result count(Digraph g)
	{
		if (g.dataset == null)
		{
			JMGDirectory d = new JMGDirectory("$HOME/tmp/flsjklkj");

			if (d.exists())
				d.deleteRecursively();

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

		g.out.ensureDefined();
		CountK22_Result r = new CountK22_Result();
		g.in.file.loadIndex(8);
		LongProcess lp = new LongProcess("Nathann tracking K2,2", g.getNbVertex());
		lp.temporaryResult = r;

		new EDGParallelProcessor(g.in.file, 1000, 16)
		{
			@Override
			protected void process(int rank, Iterator<EDGFileCursor> iterator)
			{
				Cout.debug("starting thread of rank " + rank);

				int twiceNbK22 = 0;
				int sumNbPotK22 = 0;
				Cout.info("allocating arrays");
				int[] array = new int[g.getNbVertex()];
				boolean[] xINs = new boolean[g.getNbVertex()];
				lp.progressStatus = 0;
				Cout.info("done");

				while (iterator.hasNext())
				{
					++lp.progressStatus;
					EDGFileCursor x = iterator.next();

					if (x.adj.length < 2)
						continue;

					int nbInternalArcs = 0;

					for (int u : x.adj)
					{
						xINs[u] = true;
					}

					for (int u : x.adj)
					{
						for (int v : g.out.adj[u])
						{
							if (xINs[u])
							{
								++nbInternalArcs;
							}

							++array[v];
						}
					}

					array[x.vertex] = 0;

					int sumOfDegrees = 0;

					for (int u : x.adj)
					{
						for (int v : g.out.adj[u])
						{
							int n = array[v];

							if (n > 1)
							{
								twiceNbK22 += (n * (n - 1)) >> 1;
							}

							array[v] = 0;
						}

						sumOfDegrees += g.out.adj[u].length;
						xINs[u] = false;
					}

					sumNbPotK22 += (sumOfDegrees - x.adj.length) * (x.adj.length - 1)
							- nbInternalArcs;

					if (lp.progressStatus % 10000 < 1)
						synchronized (r)
						{
							r.twiceNK22 += twiceNbK22;
							r.nk22 = r.twiceNK22 / 2;
							twiceNbK22 = 0;
							r.nbK22pot += sumNbPotK22;
							sumNbPotK22 = 0;
						}
				}

				synchronized (r)
				{
					r.twiceNK22 += twiceNbK22;
					r.nk22 = r.twiceNK22 / 2;
					twiceNbK22 = 0;
					r.nbK22pot += sumNbPotK22;
					sumNbPotK22 = 0;
				}
			}
		};

		lp.end();
		return r;
	}
}
