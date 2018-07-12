package jmg.io.adj;

import java.io.IOException;
import java.io.OutputStream;

import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import jmg.Graph;
import toools.io.file.RegularFile;
import toools.io.file.nbs.NBSFile;

public abstract class ADJWriter implements TooolsPlugin<Graph, RegularFile>
{
	public RegularFile to;
	public boolean writeIndex = true;

	@Override
	public RegularFile process(Graph g)
	{
		try
		{
			write(g, to, writeIndex);
			return to;
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public void write(Graph g, RegularFile to, boolean writeIndex) throws IOException
	{
		OutputStream os = to.createWritingStream(false);
		long[] index = write(g, os);
		os.close();

		if (writeIndex)
		{
			new NBSFile(to.getPath() + ".index").saveValues(index.length, i -> index[i],
					8);

			if (g.labelling != null)
			{
				new NBSFile(to.getPath() + ".label2vertex")
						.saveValues(g.labelling.label2vertex);
			}
		}
	}

	@Override
	public void setParameters(PluginParms p)
	{
		if (p.contains("index"))
			writeIndex = p.getBoolean("index");
	}

	protected abstract long[] write(Graph g, OutputStream os) throws IOException;
}
