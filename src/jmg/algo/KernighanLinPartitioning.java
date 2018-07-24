package jmg.algo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import jmg.Graph;
import jmg.MatrixAdj;
import jmg.gen.DirectedGNP;
import toools.io.Cout;
import toools.thread.MultiThreadProcessing;

public class KernighanLinPartitioning extends Partitioning
{
	private final IntList improvement = new IntArrayList();

	public KernighanLinPartitioning(Graph g, int nbThreads)
	{
		super(g, nbThreads);
		g.out.ensureLoaded(nbThreads);
		g.in.ensureLoaded(nbThreads);
	}

	public void reassign(Graph g, int nbThreads)
	{
		AtomicInteger nbSwap = new AtomicInteger(0);

		new MultiThreadProcessing(nbThreads, null)
		{
			@Override
			protected void runInParallel(ThreadSpecifics s, List<Thread> threads)
					throws Throwable
			{
				for (int r = 0; r < assignment.length / nbThreads; ++r)
				{
					int u = ThreadLocalRandom.current().nextInt(assignment.length);
					int v = ThreadLocalRandom.current().nextInt(assignment.length);

					int uIfInPv = countCrossArcs(u, assignment[v]);
					int vIfInPu = countCrossArcs(v, assignment[u]);

					// worth moving
					if (uIfInPv + vIfInPu < nbCrossArcs[u] + nbCrossArcs[v])
					{
						int tmp = assignment[u];
						assignment[u] = assignment[v];
						assignment[v] = tmp;
						nbSwap.incrementAndGet();

						nbCrossArcs[u] = uIfInPv;
						nbCrossArcs[v] = vIfInPu;
					}
				}
			}
		};

		improvement.add(nbSwap.get());
	}

	public void compute(int nbPartitions, int nbThreads, BooleanSupplier stop)
	{
		randomize(1, nbPartitions);

		while ( ! stop.getAsBoolean())
		{
			reassign(g, nbThreads);
		}
	}

	public static void main(String[] args)
	{
		Graph g = new Graph();
		g.out.mem = new MatrixAdj(DirectedGNP.out(100000, 0.01, new Random(), true, 4),
				null, 1);

		KernighanLinPartitioning p = new KernighanLinPartitioning(g, 2);
		p.compute(4, 4, new BooleanSupplier()
		{
			int min = Integer.MAX_VALUE;

			@Override
			public boolean getAsBoolean()
			{
				if (p.improvement.size() < 1)
					return false;

				int last = p.improvement.getInt(p.improvement.size() - 1);
				Cout.result(last);

				if (last < min)
				{
					min = last;
					return false;
				}
				else
					return true;
			}
		});
	}
}
