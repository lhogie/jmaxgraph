package jmg.io.jmg;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.plugins.JMGPlugin;

public class JMGWriter
{

	public static class Plugin extends JMGPlugin<Graph, Graph>
	{
		public JMGDirectory to;

		@Override
		public Graph process(Graph g)
		{
			Graph h = new Graph(to);
			h.from(g, nbThreads);
			h.writeToDisk();
			return h;
		}

		@Override
		public void setParameters(PluginParms p)
		{
			if (to == null)
				to = new JMGDirectory(p.get("path"));
		}
	}
}
