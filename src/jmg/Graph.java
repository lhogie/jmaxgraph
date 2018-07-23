package jmg;

import java.io.Serializable;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import jmg.algo.ReverseGraph;
import jmg.io.jmg.JMGDirectory;
import toools.io.file.Directory;
import toools.util.Conversion;

public class Graph implements Serializable
{
	public final Direction out;
	public final Direction in;
	public final JMGDirectory jmgDirectory;
	public Labelling labelling;

	public Graph()
	{
		this(null);
	}

	public Graph(JMGDirectory d)
	{
		this(d, false, Runtime.getRuntime().availableProcessors());
	}

	public Graph(JMGDirectory d, boolean useLabels, int nbThreads)
	{
		this.jmgDirectory = d;
		out = new OUTs(d == null ? null : new Directory(d, "out"), nbThreads);
		in = new INs(d == null ? null : new Directory(d, "in"), nbThreads);
		out.opposite = in;
		in.opposite = out;

		if (d != null)
		{
			d.ensureExists();

			if (useLabels && d.label2vertexFile.exists())
			{
				labelling.label2vertex = Conversion
						.toIntArray(d.label2vertexFile.readValues(nbThreads));

				assert new IntOpenHashSet(labelling.label2vertex)
						.size() == labelling.label2vertex.length : new IntOpenHashSet(
								labelling.label2vertex).size() + " != "
								+ labelling.label2vertex.length;

				labelling.vertex2label = new Vertex2LabelMap(labelling.label2vertex);
			}
		}
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
		if (out.mem.isDefined())
		{
			out.mem.b[u] = JmgUtils.insert(out.mem.b[u], v, true);
		}

		if (in.mem.isDefined())
		{
			in.mem.b[v] = JmgUtils.insert(in.mem.b[v], u, true);
		}
	}

	public void addArcs_slow(int u, int... V)
	{
		if (out.mem.isDefined())
		{
			out.mem.b[u] = JmgUtils.union(out.mem.b[u], V);
		}

		if (in.mem.isDefined())
		{
			for (int v : V)
			{
				JmgUtils.insert(in.mem.b[v], u, true);
			}
		}
	}

	public void removeArc_slow(int u, int v)
	{
		if (out.mem.isDefined())
		{
			out.mem.b[u] = JmgUtils.remove(out.mem.b[u], v, ! true);
		}

		if (in.mem.isDefined())
		{
			in.mem.b[v] = JmgUtils.remove(in.mem.b[v], u, ! true);
		}
	}

	public int getNbVertices()
	{
		for (Adjacency adj : new Adjacency[] { out.mem, in.mem, out.disk, in.disk })
		{
			if (adj.isDefined())
			{
				return adj.getNbVertices();
			}
		}

		throw new IllegalStateException("no adj available");
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

	public long getNbArcs()
	{
		for (Adjacency adj : new Adjacency[] { out.mem, in.mem, out.disk, in.disk })
		{
			if (adj.isDefined())
			{
				return adj.getNbArcs();
			}
		}

		throw new IllegalStateException("no adj loaded");
	}

	@Override
	public String toString()
	{
		return "graph: " + getNbVertices() + " vertices";
	}

	public void writeToDisk()
	{
		jmgDirectory.ensureExists();
		assert new IntOpenHashSet(labelling.label2vertex)
				.size() == labelling.label2vertex.length : new IntOpenHashSet(
						labelling.label2vertex).size() + " != "
						+ labelling.label2vertex.length;

		if (labelling != null)
		{
			jmgDirectory.getLabel2VertexFile().saveValues(labelling.label2vertex.length,
					l -> labelling.label2vertex[l], 4);
		}

		if (out.mem.isDefined())
		{
			out.disk.setAllFrom(out.mem, 1);
		}

		if (in.mem.isDefined())
		{
			in.disk.setAllFrom(in.mem, 1);
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

	public void from(Graph g, int nbThreads)
	{
		if (g.in.mem.isDefined())
			in.mem.setAllFrom(g.in.mem, nbThreads);

		if (g.in.disk.isDefined())
			in.disk.setAllFrom(g.in.disk, nbThreads);

		if (g.out.mem.isDefined())
			out.mem.setAllFrom(g.out.mem, nbThreads);

		if (g.in.disk.isDefined())
			out.disk.setAllFrom(g.out.disk, nbThreads);
	}

}
