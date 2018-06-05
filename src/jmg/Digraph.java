package jmg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Properties;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import jmg.algo.ReverseGraph;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;
import toools.io.file.RegularFile;

public class Digraph implements Serializable
{
	public Properties properties = new Properties();
	public int nbVertices;
	public final Direction out;
	public final Direction in;
	public boolean isMultiGraph = false;
	public JMGDirectory jmgDirectory;
	public Labelling labelling;

	public Digraph()
	{
		out = new OUTs();
		in = new INs();
		out.opposite = in;
		in.opposite = out;
	}

	public Direction getDirection(Direction.NAME d)
	{
		if (d == Direction.NAME.IN)
		{
			return in;
		}
		else if (d == Direction.NAME.OUT)
		{
			return out;
		}

		throw new IllegalStateException("unknown ADJ type: " + d);
	}

	public Direction getDefinedDirection()
	{
		if (out.disk.isDefined())
		{
			return out;
		}
		else if (in.disk.isDefined())
		{
			return in;
		}
		else
		{
			return null;
		}
	}

	public void addArc_slow(int u, int v)
	{
		if (out.mem != null)
		{
			out.mem.b[u] = Utils.insert(out.mem.b[u], v, isMultiGraph);
		}

		if (in.mem != null)
		{
			in.mem.b[v] = Utils.insert(in.mem.b[v], u, isMultiGraph);
		}
	}

	public void addArcs_slow(int u, int... V)
	{
		if (out.mem.b != null)
		{
			out.mem.b[u] = Utils.union(out.mem.b[u], V);
		}

		if (in.mem.b != null)
		{
			for (int v : V)
			{
				Utils.insert(in.mem.b[v], u, isMultiGraph);
			}
		}
	}

	public void removeArc_slow(int u, int v)
	{
		if (out.mem.b != null)
		{
			out.mem.b[u] = Utils.remove(out.mem.b[u], v, ! isMultiGraph);
		}

		if (in.mem.b != null)
		{
			in.mem.b[v] = Utils.remove(in.mem.b[v], u, ! isMultiGraph);
		}
	}

	public int getNbVertices()
	{
		return nbVertices;
	}

	public void symmetrize(int nbThreads)
	{
		in.ensureDefined(nbThreads);
		out.ensureDefined(nbThreads);
		int[][] undirectedAdj = Utils.union(out.mem.b, in.mem.b, true, nbThreads);
		out.mem.b = in.mem.b = undirectedAdj;
	}

	public boolean arcExists(int u, int v)
	{
		if (out.mem.b != null)
		{
			return Utils.contains(out.mem.b[u], v);
		}
		else if (in.mem.b != null)
		{
			return Utils.contains(in.mem.b[v], u);
		}

		throw new IllegalStateException("no ADJ");
	}

	public void reverse()
	{
		if (in.mem.b == null && out.mem.b == null)
		{
			throw new IllegalStateException("no ADJ defined");
		}
		else if (in.mem.b == null)
		{
			in.mem.b = ReverseGraph.computeInverseADJ(out.mem.b, true);
			out.mem.b = null;
		}
		else if (out.mem.b == null)
		{
			out.mem.b = ReverseGraph.computeInverseADJ(in.mem.b, true);
			in.mem.b = null;
		}
	}

	public long countArcs(int nbThreads)
	{
		if (out.mem.isDefined())
		{
			return out.mem.countArcs(nbThreads);
		}
		else if (in.mem.isDefined())
		{
			return in.mem.countArcs(nbThreads);
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
			in.disk.file = jmgDirectory.inFile;
		}

		if (jmgDirectory.outFile.exists())
		{
			out.disk.file = jmgDirectory.outFile;
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

		if (out.mem != null)
		{
			Cout.debugSuperVisible("dlsjlj");
			d.getOutFile().writeADJ(out.mem.b);
		}

		if (in.mem != null)
		{
			d.getInFile().writeADJ(in.mem.b);
		}
	}

	public void writeProperties(RegularFile f) throws IOException
	{
		properties.put("nbVertices", "" + getNbVertices());
		OutputStream pos = f.createWritingStream();
		properties.store(pos, "JMG property file");
		pos.close();
	}

	public boolean noADJLoaded()
	{
		return out.mem.b == null && in.mem.b == null;
	}

	public void ensureADJLoaded(int nbThreads)
	{
		// if no ADJ is loaded
		if (out.mem.b == null && in.mem.b == null)
		{
			// try to load OUTs or INs
			for (Direction d : new Direction[] { out, in })
			{
				if (d.disk.file.exists())
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
