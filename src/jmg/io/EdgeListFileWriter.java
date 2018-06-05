package jmg.io;

import java.io.PrintStream;

import j4u.chain.PluginConfig;
import j4u.chain.TooolsPlugin;
import jmg.Digraph;
import toools.io.file.RegularFile;

public class EdgeListFileWriter
{

	public static class Plugin implements TooolsPlugin<Digraph, RegularFile>
	{
		public RegularFile to;

		@Override
		public RegularFile process(Digraph g)
		{
			PrintStream ps = new PrintStream(to.createWritingStream());
			write(g, ps);
			ps.close();
			return to;
		}

		@Override
		public void setup(PluginConfig p)
		{
		}
	}

	public static void write(Digraph g, PrintStream out)
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
