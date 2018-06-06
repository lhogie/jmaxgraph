package jmg.chain;

import java.io.IOException;

import j4u.chain.PluginConfig;
import jmg.Digraph;
import jmg.Direction;

public class save_edges extends JMGPlugin<Digraph, Digraph>
{
	public Direction.NAME directionType;

	@Override
	public void setup(PluginConfig parms)
	{
		this.directionType = Direction.NAME.valueOf(parms.get("type"));
	}

	@Override
	public Digraph process(Digraph g)
	{
		Direction d = g.getDirection(directionType);

		try
		{
			d.disk.save(d.mem.b);
		}
		catch (IOException e)
		{
			throw new IllegalStateException();
		}

		return g;
	}

}
