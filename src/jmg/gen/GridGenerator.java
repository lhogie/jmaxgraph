package jmg.gen;

import java.io.IOException;
import java.nio.channels.NotYetBoundException;
import java.util.Arrays;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.Utils;
import jmg.io.DotWriter;
import toools.NotYetImplementedException;
import toools.io.Cout;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class GridGenerator implements TooolsPlugin<Void, Digraph>
{

	public int nbColumns;
	public int nbRows;
	public boolean horizontal = true;
	public boolean vertical = true;
	public boolean diags = false;
	public boolean tore = false;

	@Override
	public Digraph process(Void v)
	{
		Digraph g = new Digraph();
		g.out = dgrid_outs(nbRows, nbColumns, horizontal, vertical, diags, tore);
		return g;
	}

	public static int[][] dgrid_outs(int nbRow, int nbColumn, boolean horizontal,
			boolean vertical, boolean diags, boolean tore)
	{
		int nbVertex = nbColumn * nbRow;
		LongProcess lp = new LongProcess("grid-connecting", " vertices", nbVertex);
		int[][] adj = new int[nbVertex][];

		new ParallelIntervalProcessing(nbVertex)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				int[] buf = new int[4];

				for (int v = lowerBound; v < upperBound; ++v)
				{
					if (v % 100 == 0)
						lp.progressStatus.addAndGet(100);

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
/*						if (i == 0)
							buf[n++] = v + nbColumn * (nbRow - 1);

						if (i == nbRow - 1)
							buf[n++] = v - nbColumn * (nbRow - 1);

						if (j == 0)
							buf[n++] = v + nbRow * (nbColumn - 1);

						if (i == nbColumn - 1)
							buf[n++] = v - nbRow * (nbColumn - 1);*/
					}

					adj[v] = Arrays.copyOf(buf, n);
				}
			}
		};

		lp.end();
		Utils.ensureSorted(adj);
		return adj;
	}

	public static void main(String[] args) throws IOException
	{
		int[][] adj = GridGenerator.dgrid_outs(3,  30, true, true, true, false);
		Digraph g = new Digraph();
		g.out= adj;
		g.symmetrize();
		Cout.debug(DotWriter.toString(g.in));
	}

	@Override
	public void setup(PluginConfig p)
	{
		nbColumns = p.getInt("cols");
		nbRows = p.getInt("rows");
		horizontal = p.containsAndRemove("horizontal");
		vertical = p.containsAndRemove("vertical");
		diags = p.containsAndRemove("diags");
		tore = p.containsAndRemove("tore");
	}

}
