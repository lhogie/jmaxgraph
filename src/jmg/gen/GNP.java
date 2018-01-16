package jmg.gen;

import java.util.Random;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;

public abstract class GNP
{

	public abstract Digraph out(int nbVertex, double p, Random prng);

	public  class Plugin implements TooolsPlugin<Void, Digraph>
	{
		public double p;
		public int nbVertex;
		public Random r;

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
			long seed = p.contains("seed") ? p.getInt("seed")
					: System.currentTimeMillis();
			this.r = new Random(seed);
		}
	}

}
