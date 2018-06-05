package jmg.chain;

import j4u.chain.PluginConfig;
import jmg.Digraph;
import jmg.gen.CompleteGraph;

public class complete_graph extends JMGPlugin<Void, Digraph>
{
	int nbVertices;

	@Override
	public Digraph process(Void in)
	{
		return CompleteGraph.doit(nbVertices, nbThreads);
	}

	@Override
	public void setup(PluginConfig p)
	{
		this.nbVertices = p.getInt("n");
	}

}
