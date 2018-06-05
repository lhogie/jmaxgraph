package jmg.io.jmg;

import j4u.chain.PluginConfig;
import jmg.Digraph;
import jmg.io.DatasetReaderPlugin;

public class JMGReader
{
	public static class Plugin extends DatasetReaderPlugin
	{
		public JMGDirectory from;
		public boolean useLabels;

		@Override
		public Digraph read()
		{
			return from.mapGraph(nbThreads, useLabels);
		}

		@Override
		public void setup(PluginConfig parms)
		{
			super.setup(parms);

			if (parms.contains("useLabels"))
			{
				useLabels = parms.getBoolean("useLabels");
			}
		}
	}


}
