package jmg.chain;

import java.util.Arrays;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import toools.io.Cout;

public class info implements TooolsPlugin<Digraph, Digraph>
{
	boolean outs, ins;

	@Override
	public Digraph process(Digraph g)
	{
		Cout.info("nbVertex=" + g.getNbVertex() + ", nbEdges=" + g.countArcs());

		if (outs)
		{
			Cout.info("out-ADJ:");
			int nbVertices = g.getNbVertex();

			for (int u = 0; u < nbVertices; ++u)
			{
				Cout.info(u + " => " + Arrays.toString(g.out[u]));
			}
		}

		if (ins)
		{
			Cout.info("in-ADJ:");
			int nbVertices = g.getNbVertex();

			for (int u = 0; u < nbVertices; ++u)
			{
				Cout.info(u + " => " + Arrays.toString(g.in[u]));
			}
		}

		return g;
	}

	@Override
	public void setup(PluginConfig p)
	{
		outs = p.containsAndRemove("out");
		ins = p.containsAndRemove("in");
	}

}
