package jmg;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import toools.collection.LazyArray;
import toools.collections.primitive.IntCursor;
import toools.io.Cout;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class JmgUtils
{

	// vertex labels.get(u) will be renamed u
	public static int[][] relabel(int[][] in, IntList labels, int nbThreads)
	{
		int[] labels2 = new int[in.length];
		Arrays.fill(labels2, - 1);

		int[][] out = new int[labels.size()][];

		for (int u = 0; u < out.length; ++u)
		{
			labels2[labels.getInt(u)] = u;
			out[u] = in[labels.getInt(u)];
		}

		JmgUtils.relabel(out, labels2, nbThreads);
		return out;
	}

	public static IntSet findUndeclaredVertices(Int2ObjectMap<int[]> adjTable)
	{
		boolean[] booleanArray = null;
		LazyArray known = null;

		{
			LongProcess marking = new LongProcess(
					"marking " + adjTable.keySet().size()
							+ " declared vertices as 'present'",
					"vertex", adjTable.keySet().size());

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

			for (int v : vertices)
			{
				++marking.sensor.progressStatus;

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
						booleanArray = Arrays.copyOf(booleanArray, v + 1);
					}

					if ( ! booleanArray[v])
					{

						booleanArray[v] = true;
						undeclared.add(v);
					}
				}

				++tracking.sensor.progressStatus;
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

				tracking.sensor.progressStatus++;
			}
		}

		tracking.end("found " + undeclared.size() + " undeclared vertices.");
		return undeclared;
	}

	public static void addUndeclaredVertices(Int2ObjectMap<int[]> adj)
	{
		IntSet undeclaredVertices = JmgUtils.findUndeclaredVertices(adj);
		LongProcess adding = new LongProcess(
				"adding " + undeclaredVertices.size() + " vertices with no neighbors",
				" vertex", undeclaredVertices.size());
		IntIterator i = undeclaredVertices.iterator();

		while (i.hasNext())
		{
			int v = i.nextInt();
			adj.put(v, JmgUtils.emptyArray);
			adding.sensor.progressStatus++;
		}

		adding.end("ADJ-table now has " + adj.size() + " entries.");
	}

	public static int[][] union(int[][] a, int[][] b, boolean prune, int nbThreads)
	{
		int[][] r = new int[a.length][];

		int nbVertex = a.length;
		LongProcess computing = new LongProcess("merging ADJ lists", " adjlist",
				nbVertex);

		new ParallelIntervalProcessing(nbVertex, nbThreads, computing)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					r[u] = JmgUtils.union(a[u], b[u]);

					if (prune)
					{
						a[u] = b[u] = null;
					}

					s.progressStatus++;
				}
			}
		};

		computing.end();
		return r;
	}

	public static String toPythonMap(int[] a)
	{
		StringBuilder r = new StringBuilder();
		r.append('[');

		for (int i = 0; i < a.length; ++i)
		{
			r.append(i);
			r.append(':');
			r.append(a[i]);

			if (i < a.length - 1)
			{
				r.append(", ");
			}
		}

		r.append(']');
		return r.toString();
	}

	public static int sizeOfIntersection(int[] A, int[] B)
	{
		if (A.length == 0 || B.length == 0)
			return 0;

		if (A.length == 1 && B.length == 1)
			return A[0] == B[0] ? 1 : 0;

		int[] small = A.length < B.length ? A : B;
		int[] big = A.length < B.length ? B : A;
		int nbCommonElements = 0;
		int posInBig = 0;

		for (int posInSmall = 0; posInSmall < small.length; ++posInSmall)
		{
			int v = small[posInSmall];
			int pos = IntArrays.binarySearch(big, posInBig, big.length, v);

			if (pos >= 0)
			{
				++nbCommonElements;
				posInBig = pos + 1;
			}
		}

		return nbCommonElements;
	}

	public static void ensureSorted(int[][] adj, int nbThreads)
	{
		LongProcess sorting = new LongProcess("quicksorting all ADJlists in parallel",
				" ADJ-list", adj.length);

		new ParallelIntervalProcessing(adj.length, nbThreads, sorting)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
			{
				for (int v = lowerBound; v < upperBound; ++v)
				{
					IntArrays.quickSort(adj[v]);
					++sorting.sensor.progressStatus;
				}
			}
		};

		sorting.end();
	}

	public static int indexOfFirstCommonElement(int[] A, int[] B, int startOffset)
	{
		int[] small = A.length < B.length ? A : B;
		int[] big = A.length < B.length ? B : A;

		for (int posInSmall = startOffset; posInSmall < small.length; ++posInSmall)
		{
			int v = small[posInSmall];
			int posInBig = IntArrays.binarySearch(big, v);

			if (posInBig >= 0)
			{
				return posInSmall;
			}
		}

		return - 1;
	}

	public static boolean haveCommonNode(int[] a1, int[] a2)
	{
		boolean a1Smallest = a1.length < a2.length;
		int[] small = a1Smallest ? a1 : a2;
		int[] big = a1Smallest ? a2 : a1;

		for (int v : small)
		{
			if (IntArrays.binarySearch(big, v) >= 0)
			{
				return true;
			}
		}

		return false;
	}

	public static int[] union2(int[] A, int[] B)
	{
		IntOpenHashSet s = new IntOpenHashSet(A.length + B.length);

		for (int e : A)
		{
			s.add(e);
		}

		for (int e : B)
		{
			s.add(e);
		}

		int[] r = s.toIntArray();
		IntArrays.quickSort(r);
		return r;
	}

	public static int[] union(int[] A, int[] B)
	{
		int[] R = new int[A.length + B.length - sizeOfIntersection(A, B)];
		int ai = 0, bi = 0, ri = 0;

		try
		{
			int last = - 1;

			while (ai < A.length && bi < B.length)
			{
				int a = A[ai];
				int b = B[bi];

				if (a == b)
				{
					if (last != a)
					{
						R[ri++] = a;
						last = a;
					}

					++ai;
					++bi;
				}
				else if (a < b)
				{
					if (last != a)
					{
						R[ri++] = a;
						last = a;
					}

					++ai;
				}
				else
				{
					if (last != b)
					{
						R[ri++] = b;
						last = b;
					}

					++bi;
				}
			}

			while (ai < A.length)
			{
				R[ri++] = A[ai++];
			}

			while (bi < B.length)
			{
				R[ri++] = B[bi++];
			}

			if (ri != R.length)
				throw new IllegalStateException(ri + " " + R.length);

			return R;
		}
		catch (java.lang.ArrayIndexOutOfBoundsException e)
		{
			Cout.debug(A);
			Cout.debug(B);
			Cout.debug(R);
			Cout.debug(ai, bi, ri);
			throw e;
		}

	}

	public static boolean contains(int[] a, int u)
	{
		return IntArrays.binarySearch(a, u) >= 0;
	}

	public static final int[] emptyArray = new int[0];

	public static <A extends IntCollection> int[][] convert(A[] a)
	{
		int[][] r = new int[a.length][];

		for (int i = 0; i < r.length; ++i)
		{
			r[i] = a[i].toIntArray();

			// free memory
			a[i] = null;
		}

		return r;
	}

	public static int[][] convert(Int2ObjectMap<int[]> m)
	{
		int sz = m.size();
		int[][] r = new int[sz][];

		for (int i = 0; i < sz; ++i)
		{
			r[i] = m.get(i);
		}

		return r;
	}

	public static void printAsMap(int[] a, String separator, PrintStream os)
	{
		for (int v = 0; v < a.length; ++v)
		{
			os.println(v + separator + a[v]);
		}
	}

	public static int[] insert(int[] a, int v, boolean allowsMultipleInstances)
	{
		int pos = IntArrays.binarySearch(a, v);

		if (pos >= 0 && ! allowsMultipleInstances)
			return a;

		if (pos < 0)
		{
			pos = - pos - 1;
		}

		int[] r = new int[a.length + 1];
		System.arraycopy(a, 0, r, 0, pos);
		r[pos] = v;
		System.arraycopy(a, pos, r, pos + 1, a.length - pos);
		return r;
	}

	public static int[] remove(int[] a, int v, boolean removeAll)
	{
		int pos = IntArrays.binarySearch(a, v);

		if (pos >= 0)
		{
			int n = 1;

			if (removeAll)
			{
				int i = pos + 1;

				while (i < a.length && a[i++] == v)
				{
					++n;
				}
			}

			int[] r = new int[a.length - n];
			System.arraycopy(a, 0, r, 0, pos);
			System.arraycopy(a, pos + n, r, pos, a.length - pos - n);
			return r;
		}

		return a;
	}

	public static void main(String[] args)
	{
		int[] w = new int[] { 2, 4, 6, 6, 7, 12, 12 };

		Cout.debug(remove(w, 12, true));
	}

	public static void main2(String[] args)
	{
		Random r = new Random();

		while (true)
		{
			int[] a = MathsUtilities.pickNValues(r, r.nextInt(5) + 1);
			IntArrays.quickSort(a);
			int[] b = MathsUtilities.pickNValues(r, r.nextInt(5) + 1);
			IntArrays.quickSort(b);
			Cout.debug(a);
			Cout.debug(b);
			Cout.debug(union(a, b));
			Cout.debug("****");
		}
	}

	public static long countArcs(int[][] adj, int nbThreads)
	{
		AtomicLong r = new AtomicLong(0);

		new ParallelIntervalProcessing(adj.length, nbThreads, null)
		{
			@Override
			protected void process(ThreadSpecifics s, int _lowerBound, int _upperBound)
			{
				int _n = 0;

				for (int _v = _lowerBound; _v < _upperBound; ++_v)
				{
					int[] N = adj[_v];
					_n += N.length;
				}

				r.addAndGet(_n);
			}
		};

		return r.get();
	}

	public static void relabel(int[][] r, int[] labels, int nbThreads)
	{
		LongProcess relabelling = new LongProcess("relabelling", "ADJ-list", r.length);

		new ParallelIntervalProcessing(r.length, nbThreads, relabelling)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
					throws Throwable
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					int[] adjList = r[u];

					for (int i = 0; i < adjList.length; ++i)
					{
						int neighbor = adjList[i];
						adjList[i] = labels[neighbor];
					}
				}
			}
		};

		relabelling.end();
	}

	public static void not(boolean[] notIsolated)
	{
		for (int i = 0; i < notIsolated.length; ++i)
		{
			notIsolated[i] = ! notIsolated[i];
		}
	}

	public static IntSet toSet(boolean[] b)
	{
		int sz = nbTrues(b);
		IntSet s = new IntOpenHashSet(sz);

		for (int i = 0; i < b.length; ++i)
		{
			if (b[i])
			{
				s.add(i);
			}
		}

		return s;
	}

	private static int nbTrues(boolean[] b)
	{
		int n = 0;

		for (int i = 0; i < b.length; ++i)
		{
			if (b[i])
			{
				++n;
			}
		}

		return n;
	}

	public static boolean[] toBooleanArray(IntList l)
	{
		BooleanArrayList r = new BooleanArrayList();

		for (IntCursor c : IntCursor.fromFastUtil(l))
		{
			r.ensureCapacity(c.value);
			r.set(c.value, true);
		}

		return r.toBooleanArray();
	}

	public static int[] sortVerticesBy(int[] values)
	{
		int[] r = new int[values.length];

		for (int u = 0; u < r.length; ++u)
		{
			r[u] = u;
		}

		IntArrays.parallelQuickSort(r, (u, v) -> Integer.compare(values[u], values[v]));
		return r;
	}

	public static int countTrueCells(boolean[] seen)
	{
		int n = 0;

		for (boolean b : seen)
		{
			if (b)
				++n;
		}

		return n;
	}
	
	public static int countTrueCells_par(boolean[] seen, int nbThreads)
	{
		AtomicInteger a = new AtomicInteger(0);
		
		new ParallelIntervalProcessing(seen.length, nbThreads, null)
		{
			
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
					throws Throwable
			{
				int n = 0;

				for (int i = lowerBound; i < upperBound; ++i)
				{
					if (seen[i])
						++n;
				}
				
				a.addAndGet(n);
			}
		};

		return a.get();
	}
}
