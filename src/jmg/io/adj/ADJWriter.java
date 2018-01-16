package jmg.io.adj;

import java.io.IOException;
import java.io.OutputStream;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import toools.io.NBSFile;
import toools.io.file.RegularFile;

public abstract class ADJWriter implements TooolsPlugin<Digraph, RegularFile>
{
	public RegularFile to;
	public boolean writeIndex = true;

	@Override
	public RegularFile process(Digraph g)
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

	public void write(Digraph g, RegularFile to, boolean writeIndex) throws IOException
	{
		OutputStream os = to.createWritingStream(false);
		long[] index = write(g, os);
		os.close();

		if (writeIndex)
		{
			new NBSFile(to.getPath() + ".index").saveValues(index.length,
					i -> index[i], 8);

			if (g.label2vertex != null)
			{
				new NBSFile(to.getPath() + ".label2vertex").saveValues(
						g.label2vertex.length,
						i -> g.label2vertex[i], 4);
			}
		}

	}

	@Override
	public void setup(PluginConfig p)
	{
		if (p.contains("index"))
			writeIndex = p.getBoolean("index");
	}

	protected abstract long[] write(Digraph g, OutputStream os) throws IOException;
}
