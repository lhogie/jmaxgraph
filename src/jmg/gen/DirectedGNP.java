package jmg.gen;

import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.Utils;
import toools.progression.LongProcess;

public class DirectedGNP
{

	public static Digraph out(int nbVertex, double p, Random prng)
	{
		LongProcess lp = new LongProcess("generating GNP graph", nbVertex);
		IntSet[] v_hash = new IntSet[nbVertex];

		for (int v = 0; v < nbVertex; ++v)
		{
			v_hash[v] = new IntOpenHashSet();
		}

		for (int u = 0; u < nbVertex; ++u)
		{
			for (int v = 0; v < nbVertex; ++v)
			{
				if (u != v)
				{
					if (prng.nextDouble() < p)
					{
						v_hash[u].add(v);
					}
				}
			}

			lp.progressStatus.incrementAndGet();
		}

		int[][] v_array = new int[nbVertex][];

		for (int u = 0; u < nbVertex; ++u)
		{
			v_array[u] = v_hash[u].toIntArray();
		}

		lp.end();
		Utils.ensureSorted(v_array);
		Digraph g = new Digraph();
		g.out = v_array;
		return g;
	}

	public static class Plugin implements TooolsPlugin<Void, Digraph>
	{
		public double p = 0.5;
		public int nbVertex = 1000;
		public Random r = new Random();

		@Override
		public Digraph process(Void v)
		{
			Digraph g = out(nbVertex, p, r);
			return g;
		}

		@Override
		public void setup(PluginConfig p)
		{
			nbVertex = p.getInt("n");
			this.p = p.getDouble("p");
			
			long seed = p.contains("seed") ? p.getInt("seed") :  System.currentTimeMillis();
			this.r = new Random(seed);

		}
	}

}
