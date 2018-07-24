package jmg.demo;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import jmg.Graph;
import jmg.MatrixAdj;
import jmg.gen.ErdosRenyiFromNetworkit;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class GraphOverview
{

	@SuppressWarnings("serial")
	public static class AtomicInt extends AtomicInteger
	{

		public AtomicInt()
		{
			super();
		}

		public AtomicInt(int value)
		{
			super(value);
		}

		public int getAndMin(int value)
		{
			int local;
			do
			{
				local = get();
			}
			while (local > value && ! compareAndSet(local, value));
			return local;
		}

		public int getAndMax(int value)
		{
			int local;
			do
			{
				local = get();
			}
			while (local < value && ! compareAndSet(local, value));
			return local;
		}

	}

	public static void main(String args[]) throws Exception
	{
		Runtime runtime = Runtime.getRuntime();

		int nbThreads = runtime.availableProcessors(), nbVertex = 1 << 20;

		// JMGDirectory directory = new JMGDirectory("$HOME/tmp/bench.jmg");

		int nodeArray[][];

		Graph graph = null;
		// boolean exist = directory.outFile.exists();

		// if (!exist) {
		MatrixAdj inMemory = new MatrixAdj(
				ErdosRenyiFromNetworkit.out(nbVertex, 10.0 / nbVertex), null, nbThreads);
		graph = new Graph();
		graph.out.mem = inMemory;

		nodeArray = inMemory.b;
		/*
		 * } else { nodeArray = directory.outFile.readADJ(1, 0, nbThreads); }
		 */

		int nodes = nodeArray.length;

		AtomicInt edges = new AtomicInt(), min = new AtomicInt(Integer.MAX_VALUE),
				max = new AtomicInt(Integer.MIN_VALUE), isolate = new AtomicInt(),
				loops = new AtomicInt();

		LongProcess longProcess = new LongProcess("Overview", "vertices", nodes);

		new ParallelIntervalProcessing(nodes, nbThreads, longProcess)
		{

			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
					throws Throwable
			{
				for (int localBound = lowerBound; localBound < upperBound; localBound++)
				{
					int neibArray[] = nodeArray[localBound],
							nodeDegree = neibArray.length;
					if (nodeDegree == 0)
						isolate.getAndIncrement();
					edges.getAndAdd(nodeDegree);
					max.getAndMax(nodeDegree);
					min.getAndMin(nodeDegree);
					if (Arrays.binarySearch(neibArray, localBound) >= 0)
						loops.getAndIncrement();
					s.progressStatus++;
				}
			}

		};

		longProcess.end();

		int iedges = edges.get();
		System.out.printf("nodes, edges\t\t%d, %d\n" + "directed?\t\tTrue\n"
				+ "weighted?\t\tFalse\n" + "isolated nodes\t\t%d\n" + "self-loops\t\t%d\n"
				+ "density\t\t\t%f\n" + "min/max/avg degree\t%d, %d, %f\n", nodes, iedges,
				isolate.get(), loops.get(), iedges / (nodes * (nodes - 1.0)), min.get(),
				max.get(), iedges / (double) nodes);

		/*
		 * if (!exist) { graph.nbVertices = nodes; graph.write(directory); }
		 */
	}

}
