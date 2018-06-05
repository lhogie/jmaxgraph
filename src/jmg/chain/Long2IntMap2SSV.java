package jmg.chain;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import j4u.chain.PluginConfig;
import j4u.chain.TooolsPlugin;

public class Long2IntMap2SSV implements TooolsPlugin<Long2IntMap, String>
{

	@Override
	public String process(Long2IntMap in)
	{
		StringBuilder b = new StringBuilder();

		for (long k : in.keySet())
		{
			b.append(k);
			b.append('\t');
			b.append(in.get(k));
			b.append('\n');
		}

		return b.toString();
	}

	@Override
	public void setup(PluginConfig p)
	{
		// TODO Auto-generated method stub

	}

}
