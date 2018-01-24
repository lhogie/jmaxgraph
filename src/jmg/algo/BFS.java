package jmg.algo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.gen.GridGenerator;
import toools.io.Cout;
import toools.progression.LongProcess;

public class BFS 
{

	public static int[] classic(int[][] adj, int src)
	{
		LongProcess lp = new LongProcess("BFS (classic)", adj.length);
		int[] distances = new int[adj.length];
		Arrays.fill(distances, - 1);
		// IntPriorityQueue q = new IntArrayFIFOQueue();
		int[] q = new int[adj.length];
		int from = 0;
		int to = 1;
		q[0] = src;
		distances[src] = 0;

		while (from != to)
		{
				++lp.progressStatus;

			int v = q[from++];
			int d = distances[v];

			for (int n : adj[v])
			{
				if (distances[n] == - 1)
				{
					distances[n] = d + 1;
					q[to++] = n;
				}
			}
		}

		lp.end();
		return distances;
	}

	public static int[] bsp_seq(int[][] adj, int src)
	{
		LongProcess lp = new LongProcess("BFS (seq BSP)", - 1);
		int[] distances = new int[adj.length];
		Arrays.fill(distances, - 1);
		IntList inbox = new IntArrayList();
		inbox.add(0);
		IntList inboxSizeHistory = new IntArrayList();

		for (int distance = 0; ! inbox.isEmpty(); ++distance)
		{
			// inboxSizeHistory.add(inbox.size());
			++lp.progressStatus;

			// expect as many messages as previous iteration
			IntList outbox = new IntArrayList(inbox.size());
			IntIterator i = inbox.iterator();

			while (i.hasNext())
			{
				int v = i.nextInt();
				distances[v] = distance;

				for (int n : adj[v])
				{
					if (distances[n] == - 1)
					{
						distances[n] = distance;
						outbox.add(n);
					}
				}
			}

			inbox = outbox;
		}

		lp.end();
		return distances;
	}

	public static void main(String[] args)
	{
		Digraph g = new Digraph();
		g.out.adj = GridGenerator.dgrid_outs(10000, 100, true, true, false, false);
		int[] distances = bsp_seq(g.out.adj, 0);
		// FastUtils.printAsMap(distances, " has distance ", System.out);
		System.out.println("*****");
		int[] distances2 = classic(g.out.adj, 0);
		Cout.debug(distances2);
		// FastUtils.printAsMap(distances2, " has distance ", System.out);
	}
	public static class Plugin implements TooolsPlugin<Digraph, int[]>
	{
		private int src = 0;

		@Override
		public int[] process(Digraph g)
		{
			return BFS.classic(g.out.adj, src);
		}

		@Override
		public void setup(PluginConfig p)
		{
			if (p.contains("src"))
				src = p.getInt("src");
		}
	}

}
