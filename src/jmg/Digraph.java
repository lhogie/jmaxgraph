package jmg;

import java.util.Arrays;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import jmg.algo.Adj;
import jmg.algo.ReverseGraph;
import jmg.io.JMGDataset;
import toools.collection.LazyArray;
import toools.io.Cout;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class Digraph
{
	public int[][] out;
	public int[][] in;
	public boolean isMultiGraph = true;
	public Vertex2LabelMap vertex2label;
	public int[] label2vertex;
	public JMGDataset origin;

	public void addArc(int u, int v)
	{
		if (out != null)
		{
			out[u] = Utils.insert(out[u], v, isMultiGraph);
		}

		if (in != null)
		{
			in[v] = Utils.insert(in[v], u, isMultiGraph);
		}
	}

	public void addArcs(int u, int... V)
	{
		if (out != null)
		{
			out[u] = Utils.union(out[u], V);
		}

		if (in != null)
		{
			for (int v : V)
			{
				Utils.insert(in[v], u, isMultiGraph);
			}
		}
	}

	public void removeArc(int u, int v)
	{
		if (out != null)
		{
			out[u] = Utils.remove(out[u], v, ! isMultiGraph);
		}

		if (in != null)
		{
			in[v] = Utils.remove(in[v], u, ! isMultiGraph);
		}
	}

	public static void main(String[] args)
	{
		Digraph g = new Digraph();
		g.out = new int[1][];
		g.out[0] = new int[0];
		Random r = new Random();

		LongProcess p = new LongProcess("perfomance", 1000000);

		for (int i = 0; i < 1000000; ++i)
		{
			g.addArc(0, r.nextInt());
			p.progressStatus.incrementAndGet();
		}

		p.end();
	}

	public int[][] getRefAdj()
	{
		if (out == null && in == null)
		{
			return null;
		}
		else if (out == null)
		{
			return in;
		}
		else
		{
			return out;
		}
	}

	public int getNbVertex()
	{
		return getRefAdj().length;
	}

	public void undirectionalize()
	{
		ensureBothDirections();
		Utils.union(out, in, true);
	}

	public boolean exists(int u, int v)
	{
		if (out != null)
		{
			return Utils.contains(out[u], v);
		}
		else if (in != null)
		{
			return Utils.contains(in[v], u);
		}
		else
			throw new IllegalStateException("no ADJ");
	}

	public void ensureSorted()
	{
		Utils.ensureSorted(out);
	}

	public void ensureBothDirections()
	{
		if (in == null && out == null)
		{
			throw new IllegalStateException("neither ins or outs and defined");
		}
		else if (in == null)
		{
			in = ReverseGraph.computeInverseADJ(out, false);
		}
		else if (out == null)
		{
			out = ReverseGraph.computeInverseADJ(in, false);
		}
	}

	public void reverse()
	{
		if (in == null && out == null)
		{
			throw new IllegalStateException("no ADJ defined");
		}
		else if (in == null)
		{
			in = ReverseGraph.computeInverseADJ(out, true);
			out = null;
		}
		else if (out == null)
		{
			out = ReverseGraph.computeInverseADJ(in, true);
			in = null;
		}
	}

	public int[] computeDegrees()
	{
		LongProcess computeDegrees = new LongProcess("computeDegrees", out.length);
		int[] d = new int[out.length];

		for (int v = 0; v < out.length; ++v)
		{
			d[v] = out[v].length;
			computeDegrees.progressStatus.incrementAndGet();
		}

		computeDegrees.end();
		return d;
	}

	public long countArcs()
	{
		if (out != null)
		{
			return Adj.countArcs(out);
		}
		else if (in != null)
		{
			return Adj.countArcs(in);
		}
		else
		{
			return 0;
		}
	}

	public static Digraph from(Int2ObjectMap<int[]> adj, boolean addUndeclared,
			boolean relabel, boolean sort)
	{
		if (addUndeclared)
		{
			addUndeclaredVertices(adj);
		}

		Digraph g = new Digraph();

		if (relabel)
		{
			g.label2vertex = adj.keySet().toIntArray();
			g.vertex2label = new Vertex2LabelMap(g.label2vertex);
		}

		g.out = toOut(adj, g);

		if (sort)
		{
			g.ensureSorted();
		}

		return g;
	}

	public static void addUndeclaredVertices(Int2ObjectMap<int[]> adj)
	{
		IntSet undeclaredVertices = findUndeclaredVertices(adj);
		LongProcess adding = new LongProcess(
				"adding " + undeclaredVertices.size() + " vertices with no neighbors",
				" vertex", undeclaredVertices.size());
		IntIterator i = undeclaredVertices.iterator();

		while (i.hasNext())
		{
			int v = i.nextInt();
			adj.put(v, Utils.emptyArray);
			adding.progressStatus.incrementAndGet();
		}

		adding.end("ADJ-table now has " + adj.size() + " entries.");
	}

	public static IntSet findUndeclaredVertices(Int2ObjectMap<int[]> adjTable)
	{
		boolean[] booleanArray = null;
		LazyArray known = null;

		{
			LongProcess marking = new LongProcess(
					"marking " + adjTable.keySet().size()
							+ " declared vertices as 'present'",
					adjTable.keySet().size());

			int[] vertices = adjTable.keySet().toIntArray();

			try
			{
				int maxV = MathsUtilities.max(vertices);
				booleanArray = new boolean[maxV + 1];
				Cout.info("using boolean array for known vertex set");
			}
			catch (Throwable e)
			{
				known = new LazyArray();
				Cout.info(
						"using lazy int array for known vertex set: not enough RAM for allocating a large boolean array");
			}

			int n = 0;

			for (int v : vertices)
			{
				if (n++ % 100 == 0)
					marking.progressStatus.addAndGet(100);

				if (booleanArray != null)
					booleanArray[v] = true;
				else
					known.put(v, 1);
			}

			marking.end();
		}

		ObjectCollection<int[]> adjLists = adjTable.values();
		LongProcess tracking = new LongProcess(
				"tracking non-'present' vertices in ADJ-lists", " list", adjLists.size());
		IntSet undeclared = new IntOpenHashSet(adjTable.size());

		if (booleanArray != null)
		{
			for (int[] adjList : adjLists)
			{
				for (int v : adjList)
				{
					if (v >= booleanArray.length)
					{
						Cout.debug("((( reallocating");
						long a = System.currentTimeMillis();
						booleanArray = Arrays.copyOf(booleanArray, v + 1);
						Cout.debug("((( wasted " + (System.currentTimeMillis() - a));
					}

					if ( ! booleanArray[v])
					{

						booleanArray[v] = true;
						undeclared.add(v);
					}
				}

				tracking.progressStatus.incrementAndGet();
			}
		}
		else
		{
			for (int[] adjList : adjLists)
			{
				for (int v : adjList)
				{
					if (known.get(v) != 1)
					{
						known.put(v, 1);
						undeclared.add(v);
					}
				}

				tracking.progressStatus.incrementAndGet();
			}
		}

		tracking.end("found " + undeclared.size() + " undeclared vertices.");
		return undeclared;
	}

	public static int[][] toOut(Int2ObjectMap<int[]> m, Digraph g)
	{
		int nbVertex = m.size();
		int[][] adj = new int[nbVertex][];
		LongProcess relabelSrc = new LongProcess(
				"relabelling " + nbVertex + " src vertices", " vertex", adj.length);

		for (int u = 0; u < nbVertex; ++u)
		{
			adj[u] = m.get(g.label2vertex == null ? u : g.label2vertex[u]);

			if (u % 1000 == 0)
				relabelSrc.progressStatus.addAndGet(100);
		}

		relabelSrc.end();

		if (g.label2vertex != null)
		{
			LongProcess relabelADJ = new LongProcess("relabelling", " list", adj.length);

			new ParallelIntervalProcessing(adj.length)
			{

				@Override
				protected void process(int rank, int lowerBound, int upperBound)
				{
					for (int v = lowerBound; v < upperBound; ++v)
					{
						int[] _list = adj[v];
						int _sz = _list.length;

						for (int _neighborIndex = 0; _neighborIndex < _sz; ++_neighborIndex)
						{
							int neighbor = _list[_neighborIndex];
							assert g.vertex2label.containsKey(neighbor) : neighbor;
							int neighborLabel = g.vertex2label.get(neighbor);
							_list[_neighborIndex] = neighborLabel;
						}

						relabelADJ.progressStatus.incrementAndGet();
					}
				}
			};

			relabelADJ.end();
		}

		return adj;
	}

	@Override
	public String toString()
	{
		return "graph: " + getNbVertex() + " vertices";
	}
}
