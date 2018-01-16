package jmg.chain;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import toools.collections.primitive.IntCursor;
import toools.progression.LongProcess;

public class long_distribution implements TooolsPlugin<long[], Long2IntMap>
{
	public static Long2IntMap getDistribution(long[] m)
	{
		LongProcess pm = new LongProcess("computing distribution", m.length);
		Long2IntMap distribution = new Long2IntAVLTreeMap();

		for (long n : m)
		{
			if (n % 100 == 0)
				pm.progressStatus.addAndGet(100);

			distribution.put(n, distribution.get(n) + 1);
		}

		pm.end();
		return distribution;
	}

	public static StringBuilder getDistributionAsString(Int2IntMap distribution)
	{
		StringBuilder r = new StringBuilder();

		for (IntCursor value : IntCursor.fromFastUtil(distribution.keySet()))
		{
			int nbElement = distribution.get(value.value);

			if (nbElement > 0)
			{
				r.append(nbElement);
				r.append(" elements have value ");
				r.append(value.value);
				r.append('\n');
			}
		}

		return r;
	}

	@Override
	public Long2IntMap process(long[] a)
	{
		return getDistribution(a);
	}

	@Override
	public void setup(PluginConfig p)
	{
	}
}
