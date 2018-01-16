package jmg.chain;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.gen.CompleteGraph;

public class complete_graph implements TooolsPlugin<Void, Digraph>
{
	int nbVertices;

	@Override
	public Digraph process(Void in)
	{
		return CompleteGraph.doit(nbVertices);
	}

	@Override
	public void setup(PluginConfig p)
	{
		this.nbVertices = p.getInt("n");
	}

}
