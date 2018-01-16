package jmg.io;

import java.io.IOException;
import java.io.PrintStream;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
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
			try
			{
				PrintStream ps = new PrintStream(to.createWritingStream());
				write(g, ps);
				ps.close();
				return to;
			}
			catch (IOException e)
			{
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void setup(PluginConfig p)
		{
		}
	}

	public static void write(Digraph g, PrintStream out)
	{
		for (int l = 0; l < g.out.length; ++l)
		{
			int v = g.label2vertex[l];

			for (int nl : g.out[l])
			{
				int n = g.label2vertex[nl];
				out.print(v);
				out.print('\t');
				out.print(n);
				out.print('\n');
			}
		}
	}

}
