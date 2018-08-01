package jmg.exp.nathann;

import jmg.Graph;
import jmg.plugins.JMGPlugin;

public class plugin extends JMGPlugin<Graph, GlobalCount>
{

	@Override
	public GlobalCount process(Graph g)
	{
		return new GlobalCount(
				K22AndTransitiveTrianglesCounter.count(g, 0, g.getNbVertices(), nbThreads));
	}
}
