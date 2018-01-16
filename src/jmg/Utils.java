package jmg;

import java.io.PrintStream;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import toools.io.Cout;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class Utils
{
	public static int nbThreads = Runtime.getRuntime().availableProcessors() * 2;

	public static void union(int[][] a, int[][] b, boolean prune)
	{
		int nbVertex = a.length;
		LongProcess computing = new LongProcess("merging ADJ lists", nbVertex);

		new ParallelIntervalProcessing(nbVertex)
		{

			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				for (int u = lowerBound; u < upperBound; ++u)
				{
					a[u] = Utils.union2(a[u], b[u]);

					if (prune)
					{
						b[u] = null;
					}

					if (u % 100 == 0)
						computing.progressStatus.addAndGet(100);
				}
			}
		};

		computing.end();
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
/*
	public static int pick(int[] weights, Random prng)
	{
		return pick(partialSums(weights), prng);
	}
*/
	public static long[] partialSums(int[] weights)
	{
		LongProcess lp = new LongProcess("computing partial sums", weights.length);
		long[] partialSums = new long[weights.length];
		long currentSum = 0;

		for (int i = 0; i < weights.length; ++i)
		{
			if (weights[i] < 0)
				throw new IllegalArgumentException("weight should be positive");

			long trySum = currentSum + weights[i];

			if (trySum < currentSum)
				throw new IllegalArgumentException(
						"long overflow while adding " + weights[i] + " to " + currentSum);

			partialSums[i] = currentSum = trySum;

			if (i % 1000 == 0)
				lp.progressStatus.addAndGet(1000);
		}

		lp.end();
		return partialSums;
	}

	public static int pick(long[] partialSums, Random prng)
	{
		double r = prng.nextDouble() * partialSums[partialSums.length - 1];
		return binarySearch(partialSums, r);
	}

	private static int binarySearch(long[] partialSums, double d)
	{
		int min = 0, max = partialSums.length - 1;

		while (max - min > 1)
		{
			int middle = (max + min) / 2;

			if (d <= partialSums[middle])
			{
				max = middle;
			}
			else if (d > partialSums[middle])
			{
				min = middle;
			}
		}

		if (max == min)
			return min;

		if (d < partialSums[min])
			return min;

		return max;
	}

	public static void main(String[] args)
	{
		long[] w = new long[] { 2, 4, 6, 6, 7, 12 };

		Random r = new Random();
		int[] distribution = new int[6];
int n = 12000;

		for (int i = 0; i < n; ++i)
		{
			++distribution[pick(w, r)];
		}


		
		Cout.debug(distribution);
	}

	public static int countElementsInCommon_dichotomic(int[] A, int[] B)
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

	public static void ensureSorted(int[][] adj)
	{
		LongProcess sorting = new LongProcess("quicksorting all ADJlists in parallel",
				" ADJ-list", adj.length);

		new ParallelIntervalProcessing(adj.length)
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
			{
				for (int v = lowerBound; v < upperBound; ++v)
				{
					IntArrays.quickSort(adj[v]);
					sorting.progressStatus.incrementAndGet();
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
		int[] R = new int[A.length + B.length - countElementsInCommon_dichotomic(A, B)];
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

}