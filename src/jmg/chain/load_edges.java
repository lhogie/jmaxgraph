package jmg.chain;

import it.unimi.dsi.fastutil.ints.IntSet;
import j4u.chain.PluginConfig;
import jmg.Digraph;
import jmg.Direction;

public class load_edges extends JMGPlugin<Digraph, Digraph>
{
	// no sampling by default
	public double p = 1;
	public long seed = System.currentTimeMillis();
	public Direction.NAME directionType;

	@Override
	public void setup(PluginConfig parms)
	{
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
	public Digraph process(Digraph g)
	{
		Direction d = g.getDirection(directionType);
		d.mem.b = d.disk.file.readADJ(p, seed, nbThreads);
		IntSet isolatedVertices = d.mem.findIsolatedVertices(0, nbThreads);
		d.mem.removeVertices(isolatedVertices, nbThreads);
		g.nbVertices = d.mem.b.length;
		return g;
	}

}
