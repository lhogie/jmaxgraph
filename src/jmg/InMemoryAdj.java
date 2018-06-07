package jmg;

import java.util.Iterator;
import java.util.function.IntPredicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import jmg.algo.Degrees;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class InMemoryAdj extends Adjacency
{
	public int[][] b;

	// may be null
	public int[] degrees;

	public InMemoryAdj()
	{

	}

	public InMemoryAdj(int[][] b)
	{
		this.b = b;
	}

	@Override
	public boolean isDefined()
	{
		return b != null;
	}

	public void ensureSorted(int nbThreads)
	{
		Utils.ensureSorted(b, nbThreads);
	}

	public void from(Int2ObjectMap<int[]> adj, boolean addUndeclared, boolean sort,
			Labelling labelling, int nbThreads)
	{
		if (addUndeclared)
		{
			Utils.addUndeclaredVertices(adj);
		}

		if (labelling != null)
		{
			labelling.label2vertex = adj.keySet().toIntArray();
			labelling.vertex2label = new Vertex2LabelMap(labelling.label2vertex);
		}

		load(adj, labelling, nbThreads);

		if (sort)
		{
			ensureSorted(nbThreads);
		}
	}

	private void load(Int2ObjectMap<int[]> m, Labelling labelling, int nbThreads)
	{
		int nbVertex = m.size();
		b = new int[nbVertex][];
		LongProcess relabelSrc = new LongProcess(
				"relabelling " + nbVertex + " src vertices", " vertex", b.length);

		for (int u = 0; u < nbVertex; ++u)
		{
			b[u] = m.get(labelling == null ? u : labelling.label2vertex[u]);
			++relabelSrc.sensor.progressStatus;
		}

		relabelSrc.end();

		if (labelling != null)
		{
			LongProcess relabelADJ = new LongProcess("relabelling", " list", b.length);

			new ParallelIntervalProcessing(b.length, nbThreads, relabelADJ)
			{

				@Override
				protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
				{
					for (int v = lowerBound; v < upperBound; ++v)
					{
						int[] _list = b[v];
						int _sz = _list.length;

						for (int _neighborIndex = 0; _neighborIndex < _sz; ++_neighborIndex)
						{
							int neighbor = _list[_neighborIndex];
							assert labelling.vertex2label
									.containsKey(neighbor) : neighbor;
							int neighborLabel = labelling.vertex2label.get(neighbor);
							_list[_neighborIndex] = neighborLabel;
						}

						++relabelADJ.sensor.progressStatus;
					}
				}
			};

			relabelADJ.end();
		}
	}

	public IntSet search(IntPredicate p)
	{
		IntSet r = new IntOpenHashSet();

		for (int u = 0; u < b.length; ++u)
		{
			if (p.test(u))
			{
				r.add(u);
			}
		}

		return r;
	}

	@Override
	public int[] degrees(int nbThreads)
	{
		if (b == null)
			throw new JMGException("no ADJ defined");

		return Degrees.computeDegrees(b, nbThreads);
	}

	public boolean isIsolated(int u)
	{
		return b[u].length > 0 && isReferencedInLists(u);
	}

	public boolean isReferencedInLists(int u)
	{
		class A
		{
			boolean value = false;
		}

		A found = new A();

		new ParallelIntervalProcessing(b.length, MultiThreadProcessing.NB_THREADS_TO_USE,
				null)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
					throws Throwable
			{
				for (int v = lowerBound; ! found.value && v < upperBound; ++v)
				{
					if (IntArrays.binarySearch(b[v], u) >= 0)
					{
						found.value = true;
						return;
					}
				}
			}
		};

		return found.value;
	}

	@Override
	public IntSet findIsolatedVertices(int expectedNb, int nbThreads)
	{
		class A
		{
			boolean[] found = new boolean[b.length];
		}

		A found = new A();

		new ParallelIntervalProcessing(b.length, nbThreads, null)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
					throws Throwable
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					for (int v : b[u])
					{
						found.found[v] = true;
					}
				}
			}
		};

		IntSet r = new IntOpenHashSet();

		for (int u = 0; u < found.found.length; ++u)
		{
			if ( ! found.found[u] && b[u].length == 0)
			{
				r.add(u);
			}
		}

		return r;
	}

	public void removeVertices(IntSet vertexSet, int nbThreads)
	{
		IntList labels = findLabels(vertexSet);
		// Cout.debug("labels: ", labels);
		this.b = Utils.relabel(b, labels, nbThreads);
	}

	private IntList findLabels(IntSet vertexSet)
	{
		int[] verticesToRemove = vertexSet.toIntArray();

		for (int u : verticesToRemove)
			if (b[u].length > 0)
				throw new IllegalStateException(
						"vertex " + u + " is connected to other vertices");

		IntArrays.quickSort(verticesToRemove);

		IntList labels = new IntArrayList();
		int i = 0;

		for (int u = 0; u < b.length; ++u)
		{
			// if u needs to stay
			if (i < verticesToRemove.length && u != verticesToRemove[i])
			{
				labels.add(u);
			}
			else
			{
				++i;
			}
		}

		return labels;
	}

	@Override
	public int[] get(int u)
	{
		return b[u];
	}

	@Override
	public long countArcs(int nbThreads)
	{
		if (nbArcs != - 1)
			return nbArcs;

		if (degrees != null)
			return MathsUtilities.sum(degrees);

		return Utils.countArcs(b, nbThreads);
	}

	@Override
	public Iterator<VertexCursor> iterator()
	{
		return new Iterator<VertexCursor>()
		{
			int i = 0;
			final VertexCursor c = new VertexCursor();

			@Override
			public boolean hasNext()
			{
				return i < b.length;
			}

			@Override
			public VertexCursor next()
			{
				c.vertex = i;
				c.adj = b[i];
				++i;
				return c;
			}
		};
	}

}
