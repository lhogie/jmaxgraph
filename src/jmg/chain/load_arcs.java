package jmg.chain;

import it.unimi.dsi.fastutil.ints.IntSet;
import j4u.chain.PluginParms;
import jmg.Direction;
import jmg.Graph;
import jmg.JmgUtils;
import toools.io.Cout;

public class load_arcs extends JMGPlugin<Graph, Graph>
{
	// no sampling by default
	public double p = 1;
	public long seed = System.currentTimeMillis();
	public Direction.NAME directionType;

	@Override
	public void setParameters(PluginParms parms)
	{
		super.setParameters(parms);
		this.directionType = Direction.NAME.valueOf(parms.get("type"));

		if (parms.contains("p"))
		{
			p = parms.getDouble("p");
		}

		if (parms.contains("seed"))
		{
			seed = parms.getLong("seed");
		}
	}

	@Override
	public Graph process(Graph g)
	{
		Direction d = g.getDirection(directionType);

		if ( ! d.disk.isDefined())
			throw new IllegalStateException("adj not defined");

		d.mem.fill(d.disk, p, seed, nbThreads);
		IntSet isolatedVertices = JmgUtils
				.toSet(d.mem.findIsolatedVertices(0, nbThreads));
		Cout.info("removing " + isolatedVertices.size() + " isolated vertices");
		d.mem.removeVertices(isolatedVertices, nbThreads);
		return g;
	}

}
