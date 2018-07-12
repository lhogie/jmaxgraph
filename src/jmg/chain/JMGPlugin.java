package jmg.chain;

import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import toools.io.Cout;
import toools.thread.MultiThreadProcessing;

public abstract class JMGPlugin<IN, OUT> implements TooolsPlugin<IN, OUT>
{
	protected int nbThreads = MultiThreadProcessing.NB_THREADS_TO_USE;
	

	@Override
	public void setParameters(PluginParms p)
	{
		if (p.contains("nbThreads"))
		{
			Cout.debug("ldsjlkjfdjfsdjk   " + nbThreads);
			nbThreads = p.getInt("nbThreads");
			Cout.debug("ldsjlkjfdjfsdjk   " + nbThreads);
		}
	}
}
