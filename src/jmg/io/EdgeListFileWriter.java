package jmg.io;

import java.io.PrintStream;

import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import jmg.Graph;
import toools.io.file.RegularFile;

public class EdgeListFileWriter
{

	public static class Plugin implements TooolsPlugin<Graph, RegularFile>
	{
		public RegularFile to;

		@Override
		public RegularFile process(Graph g)
		{
			PrintStream ps = new PrintStream(to.createWritingStream());
			write(g, ps);
			ps.close();
			return to;
		}

		@Override
		public void setParameters(PluginParms p)
		{
		}
	}

	public static void write(Graph g, PrintStream out)
	{
		for (int l = 0; l < g.out.mem.b.length; ++l)
		{
			int v = g.labelling == null ? l : g.labelling.label2vertex[l];

			for (int nl : g.out.mem.b[l])
			{
				int n = g.labelling == null ? nl : g.labelling.label2vertex[nl];
				out.print(v);
				out.print('\t');
				out.print(n);
				out.print('\n');
			}
		}
	}

}
