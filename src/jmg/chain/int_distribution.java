package jmg.chain;

import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import j4u.chain.PluginConfig;
import j4u.chain.TooolsPlugin;
import toools.collections.primitive.IntCursor;
import toools.progression.LongProcess;

public class int_distribution implements TooolsPlugin<int[], Int2IntMap>
{
	public static Int2IntMap getDistribution(int[] m)
	{
		LongProcess pm = new LongProcess("computing distribution", " element", m.length);
		Int2IntMap distribution = new Int2IntAVLTreeMap();

		for (int n : m)
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

		for (IntCursor k : IntCursor.fromFastUtil(distribution.keySet()))
		{
			int nbElement = distribution.get(k.value);

			if (nbElement > 0)
			{
				r.append(nbElement);
				r.append(" elements have value ");
				r.append(k.value);
				r.append('\n');
			}
		}

		return r;
	}

	@Override
	public Int2IntMap process(int[] a)
	{
		return getDistribution(a);
	}

	@Override
	public void setup(PluginConfig p)
	{
	}
}
