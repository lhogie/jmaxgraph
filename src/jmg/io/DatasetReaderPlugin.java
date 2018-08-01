package jmg.io;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.plugins.JMGPlugin;

public abstract class DatasetReaderPlugin extends JMGPlugin<Void, Graph>
{

	public int bufSize = 65530 * 256;
	public boolean addUndeclared = true;
	public boolean relabel = true;
	public boolean sort = true;

	@Override
	public Graph process(Void g)
	{
		return read();
	}

	public abstract Graph read();


	@Override
	public void setParameters(PluginParms parms)
	{
		super.setParameters(parms);
		
		if (parms.contains("bufSize"))
			bufSize = parms.getInt("bufSize");

		if (parms.containsAndRemove("noconsolidate"))
		{
			addUndeclared = relabel = sort = false;
		}
	}
}
