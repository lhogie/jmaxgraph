package jmg.plugins;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import toools.collections.primitive.IntCursor;
import toools.progression.LongProcess;

public class long_distribution implements TooolsPlugin<long[], Long2IntMap>
{
	public static Long2IntMap getDistribution(long[] m)
	{
		LongProcess pm = new LongProcess("computing distribution", " element", m.length);
		Long2IntMap distribution = new Long2IntAVLTreeMap();

		for (long n : m)
		{
			++pm.sensor.progressStatus;

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
	public void setParameters(PluginParms p)
	{
	}
}
