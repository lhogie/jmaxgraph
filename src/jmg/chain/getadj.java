package jmg.chain;

import j4u.chain.PluginConfig;
import j4u.chain.TooolsPlugin;
import jmg.Digraph;
import jmg.Direction;

public class getadj implements TooolsPlugin<Digraph, Direction>
{
	public String type;

	@Override
	public Direction process(Digraph g)
	{
		if (type.equals("out"))
		{
			return g.out;
		}
		else if (type.equals("in"))
		{
			return g.in;
		}

		throw new IllegalStateException("unknown ADJ type: " + type);
	}

	@Override
	public void setup(PluginConfig p)
	{
		this.type = p.get("type");
	}

}
