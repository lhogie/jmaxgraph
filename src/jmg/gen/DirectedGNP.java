package jmg.gen;

import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java4unix.pluginchain.PluginConfig;
import jmg.Digraph;
import jmg.Utils;
import jmg.chain.JMGPlugin;
import toools.progression.LongProcess;

public class DirectedGNP
{

	public static int[][] out(int nbVertex, double p, Random prng, int nbThreads)
	{
		LongProcess lp = new LongProcess("generating GNP graph", " adj-list", nbVertex);
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

			++lp.sensor.progressStatus;
		}

		int[][] v_array = new int[nbVertex][];

		for (int u = 0; u < nbVertex; ++u)
		{
			v_array[u] = v_hash[u].toIntArray();
		}

		lp.end();
		Utils.ensureSorted(v_array, nbThreads);
		return v_array;
	}

	public static class Plugin extends JMGPlugin<Void, Digraph>
	{
		public double p = 0.5;
		public int nbVertex = 1000;
		public Random r = new Random();

		@Override
		public Digraph process(Void v)
		{
			Digraph g = new Digraph();
			g.out.adj = out(nbVertex, p, r, nbThreads);
			g.nbVertices = g.out.adj.length;
			g.properties.put("edge probability", ""+p);
			return g;
		}

		@Override
		public void setup(PluginConfig p)
		{
			nbVertex = p.getInt("n");
			this.p = p.getDouble("p");

			long seed = p.contains("seed") ? p.getInt("seed")
					: System.currentTimeMillis();
			this.r = new Random(seed);

		}
	}

}
