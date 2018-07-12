package jmg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import jmg.algo.ReverseGraph;
import jmg.io.jmg.JMGDirectory;
import toools.io.file.RegularFile;

public class Graph implements Serializable
{
	public Properties properties = new Properties();
	public final Direction out;
	public final Direction in;
	public boolean isMultiGraph = false;
	public JMGDirectory jmgDirectory;
	public Labelling labelling;
	public final Cache<Integer> nbVerticesCache = new Cache<>( - 1);

	public Graph()
	{
		out = new OUTs();
		in = new INs();
		out.opposite = in;
		in.opposite = out;
	}

	public Direction getDirection(Direction.NAME d)
	{
		if (d == Direction.NAME.in)
		{
			return in;
		}
		else if (d == Direction.NAME.out)
		{
			return out;
		}

		throw new IllegalStateException("unknown ADJ type: " + d);
	}

	public void addArc_slow(int u, int v)
	{
		if (out.mem != null)
		{
			out.mem.b[u] = JmgUtils.insert(out.mem.b[u], v, isMultiGraph);
		}

		if (in.mem != null)
		{
			in.mem.b[v] = JmgUtils.insert(in.mem.b[v], u, isMultiGraph);
		}
	}

	public void addArcs_slow(int u, int... V)
	{
		if (out.mem.b != null)
		{
			out.mem.b[u] = JmgUtils.union(out.mem.b[u], V);
		}

		if (in.mem.b != null)
		{
			for (int v : V)
			{
				JmgUtils.insert(in.mem.b[v], u, isMultiGraph);
			}
		}
	}

	public void removeArc_slow(int u, int v)
	{
		if (out.mem.b != null)
		{
			out.mem.b[u] = JmgUtils.remove(out.mem.b[u], v, ! isMultiGraph);
		}

		if (in.mem.b != null)
		{
			in.mem.b[v] = JmgUtils.remove(in.mem.b[v], u, ! isMultiGraph);
		}
	}

	public int getNbVertices()
	{
		return getNbVertices(1);
	}

	public int getNbVertices(int nbThreads)
	{
		Supplier<Integer> s = new Supplier<Integer>()
		{

			@Override
			public Integer get()
			{
				for (Adjacency adj : new Adjacency[] { out.mem, in.mem, out.disk,
						in.disk })
				{
					if (adj.isDefined())
					{
						return adj.getNbVertices(nbThreads);
					}
				}

				throw new IllegalStateException("no adj loaded");
			}
		};

		return nbVerticesCache.get(s);
	}

	public void symmetrize(int nbThreads)
	{
		in.ensureLoaded(nbThreads);
		out.ensureLoaded(nbThreads);
		out.mem.b = in.mem.b = JmgUtils.union(out.mem.b, in.mem.b, true, nbThreads);
	}

	public boolean arcExists(int u, int v)
	{
		if (out.mem.b != null)
		{
			return JmgUtils.contains(out.mem.b[u], v);
		}
		else if (in.mem.b != null)
		{
			return JmgUtils.contains(in.mem.b[v], u);
		}

		throw new IllegalStateException("no ADJ");
	}

	public void reverse()
	{
		if ( ! in.isDefined() && ! out.isDefined())
		{
			throw new IllegalStateException("no ADJ defined");
		}
		else if ( ! in.isDefined())
		{
			in.mem.b = ReverseGraph.opposite(out, true);
			out.mem.b = null;
		}
		else if ( ! out.isDefined())
		{
			out.mem.b = ReverseGraph.opposite(in, true);
			in.mem.b = null;
		}
	}

	public long getNbArcs(int nbThreads)
	{
		for (Adjacency adj : new Adjacency[] { out.mem, in.mem, out.disk, in.disk })
		{
			if (adj.isDefined())
			{
				return adj.getNbArcs(nbThreads);
			}
		}

		throw new IllegalStateException("no adj loaded");
	}

	@Override
	public String toString()
	{
		return "graph: " + getNbVertices() + " vertices";
	}

	public void setDataset(JMGDirectory jmgDirectory)
	{
		this.jmgDirectory = jmgDirectory;

		if (jmgDirectory.inFile.exists())
		{
			in.disk.setFile(jmgDirectory.inFile);
		}

		if (jmgDirectory.outFile.exists())
		{
			out.disk.setFile(jmgDirectory.outFile);
		}
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

		if (out.mem.isDefined())
		{
			d.getOutFile().writeADJ(out.mem);
		}

		if (in.mem.isDefined())
		{
			d.getInFile().writeADJ(in.mem);
		}
	}

	public void writeProperties(RegularFile f) throws IOException
	{
		properties.put("nbVertices", "" + getNbVertices(1));
		properties.put("nbArcs", "" + getNbArcs(1));
		OutputStream pos = f.createWritingStream();
		properties.store(pos, "JMG property file");
		pos.close();
	}

	public boolean noADJLoaded()
	{
		return out.mem.b == null && in.mem.b == null;
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
