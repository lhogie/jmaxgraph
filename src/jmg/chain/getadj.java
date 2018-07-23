package jmg.chain;

import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import jmg.Direction;
import jmg.Graph;

public class getadj implements TooolsPlugin<Graph, Direction>
{
	public String type;

	@Override
	public Direction process(Graph g)
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
	public void setParameters(PluginParms p)
	{
		this.type = p.get("type");
	}

}
