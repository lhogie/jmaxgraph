package jmg.io.jmg;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;

public abstract class DatasetReaderPlugin implements TooolsPlugin<Void, Digraph>
{

	protected int bufSize = 65530 * 256;
	protected int nbThreads = 8;
	protected boolean addUndeclared = true;
	protected boolean relabel = true;
	protected boolean sort = true;

	@Override
	public Digraph process(Void g)
	{
		return read();
	}

	public abstract Digraph read();

	@Override
	public void setup(PluginConfig parms)
	{
		if (parms.contains("bufSize"))
			bufSize = parms.getInt("bufSize");

		if (parms.contains("nbThreads"))
		{
			nbThreads = parms.getInt("nbThreads");
		}

		if (parms.containsAndRemove("noconsolidate"))
		{
			addUndeclared = relabel = sort = false;
		}
	}
}
