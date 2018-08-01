package jmg.plugins;

import j4u.chain.PluginParms;
import jmg.Direction;
import jmg.Graph;

public class save_edges extends JMGPlugin<Graph, Graph>
{
	public Direction.NAME directionType;

	@Override
	public void setParameters(PluginParms parms)
	{
		this.directionType = Direction.NAME.valueOf(parms.get("type"));
	}

	@Override
	public Graph process(Graph g)
	{
		Direction d = g.getDirection(directionType);
		d.disk.setAllFrom(d.mem, nbThreads);
		return g;
	}

}
