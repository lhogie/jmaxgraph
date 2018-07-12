package jmg.io.jmg;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.io.DatasetReaderPlugin;

public class JMGReader
{
	public static class Plugin extends DatasetReaderPlugin
	{
		public JMGDirectory from;
		public boolean useLabels;

		@Override
		public Graph read()
		{
			return from.mapGraph(nbThreads, useLabels);
		}

		@Override
		public void setParameters(PluginParms parms)
		{
			super.setParameters(parms);

			if (parms.contains("useLabels"))
			{
				useLabels = parms.getBoolean("useLabels");
			}
		}
	}


}
