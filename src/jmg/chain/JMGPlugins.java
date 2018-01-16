package jmg.chain;

import java4unix.pluginchain.DefaultPlugins;
import java4unix.pluginchain.NBSReader;
import java4unix.pluginchain.TooolsPlugin;
import jmg.algo.BFS;
import jmg.algo.Degrees;
import jmg.algo.ReverseGraph;
import jmg.gen.DirectedGNP;
import jmg.gen.GridGenerator;
import jmg.gen.DirectedGNP;
import jmg.io.DotWriter;
import jmg.io.EdgeListFileReader;
import jmg.io.EdgeListFileWriter;
import jmg.io.JMGReader;
import jmg.io.JMGWriter;
import jmg.io.adj.ADJ32Reader;
import jmg.io.adj.ADJ32Writer;
import jmg.io.adj.TextADJReader;
import jmg.io.adj.TextADJWriter;
import toools.io.NBSFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;

public class JMGPlugins extends DefaultPlugins
{
	public JMGPlugins()
	{
		importPackages.add(JMGPlugins.class.getPackage());
	}

	@Override
	public TooolsPlugin<?, ?> create(String name, boolean bootstrap)
	{
		if (name.endsWith(".jmg"))
		{
			if (bootstrap)
			{
				JMGReader.Plugin w = new JMGReader.Plugin();
				w.from = new Directory(name);
				return w;
			}
			else
			{
				JMGWriter.Plugin w = new JMGWriter.Plugin();
				w.to = new Directory(name);
				return w;
			}
		}
		else if (name.endsWith(".adj"))
		{
			if (bootstrap)
			{
				TextADJReader w = new TextADJReader();
				w.from = new RegularFile(name);
				return w;
			}
			else
			{
				TextADJWriter w = new TextADJWriter();
				w.to = new RegularFile(name);
				return w;
			}
		}
		else if (name.endsWith(".adj32"))
		{
			if (bootstrap)
			{
				ADJ32Reader w = new ADJ32Reader();
				w.from = new RegularFile(name);
				return w;
			}
			else
			{
				ADJ32Writer w = new ADJ32Writer();
				w.to = new RegularFile(name);
				return w;
			}
		}
		else if (name.endsWith(".edgelist"))
		{
			if (bootstrap)
			{
				EdgeListFileReader.Plugin w = new EdgeListFileReader.Plugin();
				w.from = new RegularFile(name);
				return w;
			}
			else
			{
				EdgeListFileWriter.Plugin w = new EdgeListFileWriter.Plugin();
				w.to = new RegularFile(name);
				return w;
			}
		}
		else if (name.endsWith(".dot"))
		{
			DotWriter w = new DotWriter();
			w.to = new RegularFile(name);
			return w;
		}
		else if (name.endsWith(".nbs"))
		{
			if (bootstrap)
			{
				NBSReader w = new NBSReader();
				w.f = new NBSFile(name);
				return w;
			}
		}
		else if (name.equals("sgnp"))
		{
			return new DirectedGNP.Plugin();
		}
		else if (name.equals("info"))
		{
			return new info();
		}
		else if (name.equals("dgnp"))
		{
			return new DirectedGNP.Plugin();
		}
		else if (name.equals("grid"))
		{
			return new GridGenerator();
		}
		else if (name.equals("reverse"))
		{
			return new ReverseGraph();
		}
		else if (name.equals("bfs"))
		{
			return new BFS.Plugin();
		}
		else if (name.equals("degrees"))
		{
			return new Degrees.Plugin();
		}

		return super.create(name, bootstrap);
	}
}
