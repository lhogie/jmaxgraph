package jmg.io;

import java.io.PrintWriter;
import java.io.StringWriter;

import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import jmg.Graph;
import toools.io.file.RegularFile;

public class DotWriter implements TooolsPlugin<Graph, RegularFile>
{
	public RegularFile to;

	@Override
	public RegularFile process(Graph g)
	{

		to.setContent(toString().getBytes());
		return to;

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
	public void setParameters(PluginParms p)
	{
	}

}
