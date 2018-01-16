package jmg.chain;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;

public class adj implements TooolsPlugin<Digraph, int[][]>
{
	public String type;

	@Override
	public int[][] process(Digraph g)
	{
		if (type.equals("out"))
			return g.out;
		else if (type.equals("in"))
			return g.in;
		else
			throw new IllegalStateException("unknown ADJ type: " + type);
	}

	@Override
	public void setup(PluginConfig p)
	{
		this.type = p.get("type");
	}

}
