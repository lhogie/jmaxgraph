package jmg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Properties;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import jmg.algo.ReverseGraph;
import jmg.io.jmg.JMGDirectory;
import toools.io.file.RegularFile;

public class Digraph implements Serializable
{
	public Properties properties = new Properties();
	public int nbVertices;
	public final OUTs out;
	public final INs in;
	public boolean isMultiGraph = false;
	public JMGDirectory dataset;
	public Labelling labelling;

	public Digraph()
	{
		out = new OUTs();
		in = new INs();
		out.opposite = in;
		in.opposite = out;
	}

	public void setOuts(int u, int... N)
	{
		if (u != out.adj.length)
			throw new IllegalArgumentException();

		int[][] newOut = new int[out.adj.length + 1][];
		System.arraycopy(out, 0, newOut, 0, out.adj.length);
		newOut[u] = N;
		this.out.adj = newOut;
	}

	public void addArc(int u, int v)
	{
		if (out.adj != null)
		{
			out.adj[u] = Utils.insert(out.adj[u], v, isMultiGraph);
		}

		if (in.adj != null)
		{
			in.adj[v] = Utils.insert(in.adj[v], u, isMultiGraph);
		}
	}

	public void addArcs(int u, int... V)
	{
		if (out.adj != null)
		{
			out.adj[u] = Utils.union(out.adj[u], V);
		}

		if (in.adj != null)
		{
			for (int v : V)
			{
				Utils.insert(in.adj[v], u, isMultiGraph);
			}
		}
	}

	public void removeArc(int u, int v)
	{
		if (out.adj != null)
		{
			out.adj[u] = Utils.remove(out.adj[u], v, ! isMultiGraph);
		}

		if (in.adj != null)
		{
			in.adj[v] = Utils.remove(in.adj[v], u, ! isMultiGraph);
		}
	}

	public int getNbVertex()
	{
		return nbVertices;
	}

	public void symmetrize(int nbThreads)
	{
		in.ensureDefined(nbThreads);
		out.ensureDefined(nbThreads);
		Utils.union(out.adj, in.adj, true, nbThreads);
	}

	public boolean arcExists(int u, int v)
	{
		if (out.adj != null)
		{
			return Utils.contains(out.adj[u], v);
		}
		else if (in.adj != null)
		{
			return Utils.contains(in.adj[v], u);
		}

		throw new IllegalStateException("no ADJ");
	}

	public void reverse()
	{
		if (in.adj == null && out.adj == null)
		{
			throw new IllegalStateException("no ADJ defined");
		}
		else if (in.adj == null)
		{
			in.adj = ReverseGraph.computeInverseADJ(out.adj, true);
			out.adj = null;
		}
		else if (out.adj == null)
		{
			out.adj = ReverseGraph.computeInverseADJ(in.adj, true);
			in.adj = null;
		}
	}

	public long countArcs(int nbThreads)
	{
		if (out.adj != null)
		{
			return Utils.countArcs(out.adj, nbThreads);
		}
		else if (in.adj != null)
		{
			return Utils.countArcs(in.adj, nbThreads);
		}
		else
		{
			return 0;
		}
	}

	@Override
	public String toString()
	{
		return "graph: " + getNbVertex() + " vertices";
	}

	public void setDataset(JMGDirectory newDataset)
	{
		this.dataset = newDataset;
		in.file = dataset.inFile;
		out.file = dataset.outFile;
	}

	public void write(JMGDirectory d) throws IOException
	{
		d.mkdirs();
		writeProperties(d.getPropertyFile());

		assert new IntOpenHashSet(labelling.label2vertex)
				.size() == labelling.label2vertex.length : new IntOpenHashSet(
						labelling.label2vertex).size() + " != "
						+ labelling.label2vertex.length;

		if (labelling != null)
		{
			d.getLabel2VertexFile().saveValues(labelling.label2vertex.length,
					l -> labelling.label2vertex[l], 4);
		}

		if (out.adj != null)
		{
			d.getOutFile().writeADJ(out.adj);
		}

		if (in.adj != null)
		{
			d.getInFile().writeADJ(in.adj);
		}
	}

	public void writeProperties(RegularFile f) throws IOException
	{
		properties.put("nbVertices", "" + getNbVertex());
		OutputStream pos = f.createWritingStream();
		properties.store(pos, "JMG property file");
		pos.close();
	}

	public void ensureADJLoaded(int nbThreads)
	{
		// if no ADJ is loaded
		if (out.adj == null && in.adj == null)
		{
			// try to load OUTs or INs
			for (Adj d : new Adj[] { out, in })
			{
				if (d.file.exists())
				{
					d.load(nbThreads);
					return;
				}
			}

			throw new JMGException("no ADJ can't be loaded");
		}
	}

	@Override
	public int hashCode()
	{
		int h = 0;
		h = 31 * h + out.hashCode();
		h = 31 * h + in.hashCode();
		return h;
	}

}
