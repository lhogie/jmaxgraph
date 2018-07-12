package jmg.chain;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.gen.CompleteGraph;

public class complete_graph extends JMGPlugin<Void, Graph>
{
	int nbVertices;

	@Override
	public Graph process(Void in)
	{
		return CompleteGraph.doit(nbVertices, nbThreads);
	}

	@Override
	public void setParameters(PluginParms p)
	{
		this.nbVertices = p.getInt("n");
	}

}
