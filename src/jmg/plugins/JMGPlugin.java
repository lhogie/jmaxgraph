package jmg.plugins;

import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import toools.thread.MultiThreadProcessing;

public abstract class JMGPlugin<IN, OUT> implements TooolsPlugin<IN, OUT>
{
	public int nbThreads = MultiThreadProcessing.NB_THREADS_TO_USE;

	@Override
	public void setParameters(PluginParms p)
	{
		if (p.contains("nbThreads"))
		{
			nbThreads = p.getInt("nbThreads");
		}
	}
}
