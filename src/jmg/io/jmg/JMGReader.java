package jmg.io.jmg;

import java4unix.pluginchain.PluginConfig;
import jmg.Digraph;

public class JMGReader
{
	public static class Plugin extends DatasetReaderPlugin
	{
		public JMGDirectory from;
		public boolean useLabels;

		@Override
		public Digraph read()
		{
			return from.readDirectory(nbThreads, useLabels);
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
