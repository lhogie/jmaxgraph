package jmg.exp.nathann;

import jmg.Graph;
import jmg.chain.JMGPlugin;

public class K22AndTransitiveTrianglesCounterPlugin extends JMGPlugin<Graph, LocalCount>
{
	@Override
	public LocalCount process(Graph g)
	{
		return K22AndTransitiveTrianglesCounter.writeCountFile(g, 0, g.getNbVertices() - 1, nbThreads);
	}

}