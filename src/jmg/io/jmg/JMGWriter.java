package jmg.io.jmg;

import java.io.IOException;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;

public class JMGWriter
{

	public static class Plugin implements TooolsPlugin<Digraph, Digraph>
	{
		public JMGDirectory to;

		@Override
		public Digraph process(Digraph g)
		{
			try
			{
				g.write(to);
				return g;
			}
			catch (IOException e)
			{
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void setup(PluginConfig p)
		{
		}
	}


}
