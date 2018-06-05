package jmg.chain;

import j4u.chain.DefaultPlugins;
import j4u.chain.NBSReader;
import j4u.chain.TooolsPlugin;
import jmg.algo.BFS;
import jmg.algo.Degrees;
import jmg.algo.ReverseGraph;
import jmg.gen.DirectedGNP;
import jmg.gen.GridGenerator;
import jmg.io.DotWriter;
import jmg.io.EdgeListFileReader;
import jmg.io.EdgeListFileWriter;
import jmg.io.adj.ADJ32Reader;
import jmg.io.adj.ADJ32Writer;
import jmg.io.adj.TextADJFastReader;
import jmg.io.adj.TextADJWriter;
import jmg.io.jmg.JMGDirectory;
import jmg.io.jmg.JMGReader;
import jmg.io.jmg.JMGWriter;
import toools.io.file.RegularFile;
import toools.io.file.nbs.NBSFile;

public class JMGPlugins extends DefaultPlugins
{
	public JMGPlugins()
	{
		importPackages.add(JMGPlugins.class.getPackage());
	}

	@Override
	public TooolsPlugin<?, ?> create(String name, boolean bootstrap)
	{
		if (name.endsWith(".jmg") || name.endsWith(".jmg/"))
		{
			if (bootstrap)
			{
				JMGReader.Plugin w = new JMGReader.Plugin();
				w.from = new JMGDirectory(name);
				return w;
			}
			else
			{
				JMGWriter.Plugin w = new JMGWriter.Plugin();
				w.to = new JMGDirectory(name);
				return w;
			}
		}
		else if (name.endsWith(".ram.adj"))
		{
			if (bootstrap)
			{
				TextADJFastReader w = new TextADJFastReader();
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
		else if (name.endsWith(".ram.adj32"))
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
		else if (name.equals("loadadj"))
		{
			return new load_edges();
		}
		else if (name.equals("saveadj"))
		{
			return new save_edges();
		}
		else if (name.equals("sample"))
		{
			return new load_edges();
		}
		else if (name.equals("gnp"))
		{
			return new DirectedGNP.Plugin();
		}
		else if (name.equals("info"))
		{
			return new info();
		}
		else if (name.equals("grid"))
		{
			return new GridGenerator();
		}
		else if (name.equals("reverse"))
		{
			return new ReverseGraph();
		}
		else if (name.equals("getadj"))
		{
			return new getadj();
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
