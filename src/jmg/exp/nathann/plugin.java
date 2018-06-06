package jmg.exp.nathann;

import jmg.Digraph;
import jmg.chain.JMGPlugin;

public class plugin extends JMGPlugin<Digraph, GlobalCount>
{

	@Override
	public GlobalCount process(Digraph g)
	{
		return new GlobalCount(
				K22AndTransitiveTrianglesCounter.count(g, 0, g.getNbVertices(), nbThreads));
	}
}
