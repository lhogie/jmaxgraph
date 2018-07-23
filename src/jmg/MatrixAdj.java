package jmg;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import jmg.algo.Degrees;
import toools.io.file.Directory;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class MatrixAdj extends Adjacency
{
	public int[][] b;

	public MatrixAdj(int[][] initalAdj, Directory d, int nbThreads)
	{
		super(d, nbThreads);
		this.b = initalAdj;
	}

	@Override
	public boolean isDefined()
	{
		return b != null;
	}

	public void ensureSorted(int nbThreads)
	{
		JmgUtils.ensureSorted(b, nbThreads);
	}

	public void from(Int2ObjectMap<int[]> adj, boolean addUndeclared, boolean sort,
			Labelling labelling, int nbThreads)
	{
		if (addUndeclared)
		{
			JmgUtils.addUndeclaredVertices(adj);
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

	@Override
	public int[] computeDegrees(int nbThreads)
	{
		return Degrees.computeDegrees(b, nbThreads);
	}

	public boolean isIsolated(int u)
	{
		return b[u].length == 0 && ! isReferencedInLists(u);
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

	public void removeVertices(IntSet vertexSet, int nbThreads)
	{
		IntList labels = recomputeLabelsByExcluding(vertexSet);
		this.b = JmgUtils.relabel(b, labels, nbThreads);
	}

	private IntList recomputeLabelsByExcluding(IntSet excludedVertices)
	{
		int[] verticesToRemove = excludedVertices.toIntArray();

		for (int u : verticesToRemove)
			if (b[u].length > 0)
				throw new IllegalStateException(
						"vertex " + u + " is connected to other vertices");

		IntArrays.quickSort(verticesToRemove);

		IntList labels = new IntArrayList(b.length - excludedVertices.size());
		int i = 0;

		for (int u = 0; u < b.length; ++u)
		{
			// if u needs to stay
			if (i < verticesToRemove.length && u == verticesToRemove[i])
			{
				++i;
			}
			else
			{
				labels.add(u);
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
		return JmgUtils.countArcs(b, nbThreads);
	}

	@Override
	public int countVertices(int nbThreads)
	{
		return b.length;
	}

	@Override
	public Iterator<VertexCursor> iterator(int from, int to)
	{
		return new Iterator<VertexCursor>()
		{
			int i = from;
			final VertexCursor c = new VertexCursor();

			@Override
			public boolean hasNext()
			{
				return i < to;
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

	@Override
	public void setAllFrom(Adjacency adj, int nbThreads)
	{
		int nbVertices = adj.getNbVertices();
		b = new int[nbVertices][];
		LongProcess lp = new LongProcess("building matrix ADJ", " vertex", nbVertices);

		new ParallelAdjProcessing(adj, nbThreads, lp)
		{
			@Override
			public void processSubAdj(ThreadSpecifics s, Iterator<VertexCursor> i)
			{
				while (i.hasNext())
				{
					VertexCursor c = i.next();
					b[c.vertex] = c.adj;
					s.progressStatus++;
				}
			}
		};

		lp.end();
	}

	public void fill(Adjacency src, double p, long seed, int nbThreads)
	{
		LongProcess lp = new LongProcess("from()", " vertex", src.getNbVertices());

		int nbVertex = src.countVertices(nbThreads);
		b = new int[nbVertex][];

		if (p == 0)
		{
			Arrays.fill(b, IntArrays.EMPTY_ARRAY);
		}
		else
		{
			new ParallelAdjProcessing(src, nbThreads, lp)
			{
				@Override
				public void processSubAdj(ThreadSpecifics s,
						Iterator<VertexCursor> iterator)
				{
					ThreadLocalRandom prng = ThreadLocalRandom.current();

					while (iterator.hasNext())
					{
						VertexCursor c = iterator.next();

						// if no sampling
						if (p == 1)
						{
							b[c.vertex] = c.adj;
						}
						else
						{
							int nbRetained = 0;

							for (int v : c.adj)
							{
								if (prng.nextDouble() < p)
								{
									c.adj[nbRetained++] = v;
								}
							}

							// if none is retained
							if (nbRetained == 0)
							{
								b[c.vertex] = IntArrays.EMPTY_ARRAY;
							}
							else
							{
								b[c.vertex] = IntArrays.copy(c.adj, 0, nbRetained);
							}
						}

						s.progressStatus++;
					}
				}
			};
		}

		lp.end();
	}

}
