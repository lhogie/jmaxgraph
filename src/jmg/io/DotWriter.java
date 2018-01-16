package jmg.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import toools.io.file.RegularFile;

public class DotWriter implements TooolsPlugin<Digraph, RegularFile>
{
	public RegularFile to;

	@Override
	public RegularFile process(Digraph g)
	{
		try
		{
			to.setContent(toString().getBytes());
			return to;
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public static String toString(int[][] adj)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("digraph {");
		int nbVertex = adj.length;

		for (int v = 0; v < nbVertex; ++v)
		{
			for (int w : adj[v])
			{
				pw.print('\t');
				pw.print(v);
				pw.print(" -> ");
				pw.print(w);
				pw.println(";");
			}
		}

		pw.println("}");
		return sw.getBuffer().toString();
	}

	@Override
	public void setup(PluginConfig p)
	{
	}

}
