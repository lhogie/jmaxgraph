package jmg.io.jmg;

import java.io.IOException;

import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import jmg.Graph;

public class JMGWriter
{

	public static class Plugin implements TooolsPlugin<Graph, Graph>
	{
		public JMGDirectory to;

		@Override
		public Graph process(Graph g)
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
		public void setParameters(PluginParms p)
		{
		}
	}


}
