package jmg.gen;

import java.io.IOException;
import java.util.Arrays;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.JmgUtils;
import jmg.io.DotWriter;
import jmg.plugins.JMGPlugin;
import toools.exceptions.NotYetImplementedException;
import toools.io.Cout;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class GridGenerator extends JMGPlugin<Void, Graph>
{

	public int nbColumns;
	public int nbRows;
	public boolean horizontal = true;
	public boolean vertical = true;
	public boolean diags = false;
	public boolean tore = false;

	@Override
	public Graph process(Void v)
	{
		Graph g = new Graph();
		g.out.mem.b = dgrid_outs(nbRows, nbColumns, horizontal, vertical, diags, tore,
				nbThreads);
		return g;
	}

	public static int[][] dgrid_outs(int nbRow, int nbColumn, boolean horizontal,
			boolean vertical, boolean diags, boolean tore, int nbThreads)
	{
		int nbVertex = nbColumn * nbRow;
		LongProcess lp = new LongProcess("grid-connecting", " vertices", nbVertex);
		int[][] adj = new int[nbVertex][];

		new ParallelIntervalProcessing(nbVertex, nbThreads, lp)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				Cout.debug(s.rank, lowerBound, upperBound);
				int[] buf = new int[4];

				for (int v = lowerBound; v < upperBound; ++v)
				{
					++s.progressStatus;

					int i = v / nbColumn;
					int j = v % nbColumn;
					int n = 0;

					if (horizontal)
					{
						if (j < nbColumn - 1)
						{
							buf[n++] = v + 1;
						}
					}

					if (vertical)
					{
						if (i < nbRow - 1)
						{
							buf[n++] = v + nbColumn;
						}
					}

					if (diags)
					{
						if (j < nbColumn - 1 && i < nbRow - 1)
						{
							buf[n++] = v + nbColumn + 1;
						}
					}

					if (tore)
					{
						throw new NotYetImplementedException();
						/*
						 * if (i == 0) buf[n++] = v + nbColumn * (nbRow - 1);
						 * 
						 * if (i == nbRow - 1) buf[n++] = v - nbColumn * (nbRow
						 * - 1);
						 * 
						 * if (j == 0) buf[n++] = v + nbRow * (nbColumn - 1);
						 * 
						 * if (i == nbColumn - 1) buf[n++] = v - nbRow *
						 * (nbColumn - 1);
						 */
					}

					adj[v] = Arrays.copyOf(buf, n);
				}
			}
		};

		lp.end();
		JmgUtils.ensureSorted(adj, nbThreads);
		return adj;
	}

	public static void main(String[] args) throws IOException
	{
		int[][] adj = GridGenerator.dgrid_outs(3, 30, true, true, true, false, 1);
		Graph g = new Graph();
		g.out.mem.b = adj;
		g.symmetrize(8);
		Cout.debug(DotWriter.toString(g.in.mem.b));
	}

	@Override
	public void setParameters(PluginParms p)
	{
		nbColumns = p.getInt("cols");
		nbRows = p.getInt("rows");
		horizontal = p.containsAndRemove("horizontal");
		vertical = p.containsAndRemove("vertical");
		diags = p.containsAndRemove("diags");
		tore = p.containsAndRemove("tore");
	}

}
