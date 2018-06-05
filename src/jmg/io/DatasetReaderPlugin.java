package jmg.io;

import j4u.chain.PluginConfig;
import jmg.Digraph;
import jmg.chain.JMGPlugin;

public abstract class DatasetReaderPlugin extends JMGPlugin<Void, Digraph>
{

	protected int bufSize = 65530 * 256;
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
		super.setup(parms);
		

		
		if (parms.contains("bufSize"))
			bufSize = parms.getInt("bufSize");

		if (parms.containsAndRemove("noconsolidate"))
		{
			addUndeclared = relabel = sort = false;
		}
	}
}
