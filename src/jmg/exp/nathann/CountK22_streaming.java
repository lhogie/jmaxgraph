package jmg.exp.nathann;

import java.io.IOException;
import java.util.Iterator;

import jmg.Digraph;
import jmg.chain.JMGPlugin;
import jmg.io.jmg.EDGFileVertexIterator.EDGFileCursor;
import jmg.io.jmg.EDGParallelProcessor;
import jmg.io.jmg.JMGDirectory;
import toools.io.IORuntimeException;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;

public class CountK22_streaming extends JMGPlugin<Digraph, CountK22_Result>
{

	@Override
	public CountK22_Result process(Digraph g)
	{
		return count(g);
	}

	public CountK22_Result count(Digraph g)
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

		g.out.ensureDefined(nbThreads);
		CountK22_Result r = new CountK22_Result();

		int nbVertices = g.getNbVertex();
		long[][] inDegreeInduced = new long[nbVertices][];
		boolean[][] xINs = new boolean[nbVertices][];

		for (int ra = 0; ra < nbThreads; ++ra)
		{
			inDegreeInduced[ra] = new long[nbVertices];
			xINs[ra] = new boolean[nbVertices];
		}

		LongProcess lp = new LongProcess("Nathann tracking K2,2", g.getNbVertex());
		lp.temporaryResult = r;

		if (g.in.file == null)
			throw new IllegalStateException();

		new EDGParallelProcessor(g.in.file, 0, nbThreads, lp)
		{
			@Override
			protected void process(ThreadSpecifics s, Iterator<EDGFileCursor> iterator)
			{
				long _twiceNbK22 = 0;
				long _sumNbPotK22 = 0;
				long[] _inDegreeInduced = inDegreeInduced[s.rank];
				boolean[] _inNeighbors = xINs[s.rank];
				int[][] outAdjTable = g.out.adj;

				while (iterator.hasNext())
				{
					++s.progressStatus;
					EDGFileCursor _x = iterator.next();
					int[] xPreds = _x.adj;

					if (xPreds.length < 2)
						continue;

					long _nbInternalArcs = 0;

					for (int u : xPreds)
					{
						_inNeighbors[u] = true;
					}

					for (int _u : xPreds)
					{
						for (int _v : outAdjTable[_u])
						{
							if (_inNeighbors[_v])
							{
								++_nbInternalArcs;
							}

							++_inDegreeInduced[_v];
						}
					}

					_inDegreeInduced[_x.vertex] = 0;

					long _sumOfDegrees = 0;
					long _n;

					for (int u : xPreds)
					{
						for (int _v : outAdjTable[u])
						{
							_n = _inDegreeInduced[_v];

							if (_n > 1)
							{
								_twiceNbK22 += (_n * (_n - 1)) >> 1;
							}

							_inDegreeInduced[_v] = 0;
						}

						_sumOfDegrees += outAdjTable[u].length;
						_inNeighbors[u] = false;
					}

					_sumNbPotK22 += (_sumOfDegrees - xPreds.length) * (xPreds.length - 1)
							- _nbInternalArcs;

					if (s.progressStatus % 10000 < 1)
						synchronized (r)
						{
							r.twiceNK22 += _twiceNbK22;
							r.nk22 = r.twiceNK22 / 2;
							r.nbK22pot += _sumNbPotK22;

							_twiceNbK22 = 0;
							_sumNbPotK22 = 0;
						}
				}

				synchronized (r)
				{
					r.twiceNK22 += _twiceNbK22;
					r.nk22 = r.twiceNK22 / 2;
					r.nbK22pot += _sumNbPotK22;
				}
			}
		};

		lp.end();
		return r;
	}

	public static void main(String[] args)
	{
		JMGDirectory d = new JMGDirectory("$HOME/datasets/grid10kx10k.jmg");
		Digraph g = d.mapGraph(5, false);
		System.out.println(new CountK22_streaming().count(g));
	}
}
