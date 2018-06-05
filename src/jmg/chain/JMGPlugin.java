package jmg.chain;

import j4u.chain.PluginConfig;
import j4u.chain.TooolsPlugin;
import toools.thread.MultiThreadProcessing;

public abstract class JMGPlugin<IN, OUT> implements TooolsPlugin<IN, OUT>
{
	protected int nbThreads = MultiThreadProcessing.NB_THREADS_TO_USE;
	

	@Override
	public void setup(PluginConfig p)
	{
		if (p.contains("nbThreads"))
		{
			nbThreads = p.getInt("nbThreads");
		}
	}
}
