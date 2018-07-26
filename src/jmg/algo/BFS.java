package jmg.algo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import j4u.chain.PluginParms;
import j4u.chain.TooolsPlugin;
import jmg.Direction;
import jmg.Graph;
import jmg.gen.GridGenerator;
import toools.progression.LongProcess;

public class BFS
{
	public static class BFSResult
	{
		int[] visitOrder;
		int[] distances;
		public int nbVerticesVisited;

		@Override
		public String toString()
		{
			return "BFS [visitOrder=" + Arrays.toString(visitOrder) + ", distances="
					+ Arrays.toString(distances) + "]";
		}

	}

	public static BFSResult classic(int[][] adj, int src, int maxDistance, int maxSize)
	{
		LongProcess lp = new LongProcess("BFS (classic)", " vertex", adj.length);
		int[] distances = new int[adj.length];
		Arrays.fill(distances, - 1);
		int[] q = new int[adj.length];
		int from = 0;
		int to = 1;
		q[0] = src;
		distances[src] = 0;
		int nbVerticesVisited = 1;

		while (from != to)
		{
			++lp.sensor.progressStatus;

			int v = q[from++];
			int d = distances[v];

			if (d <= maxDistance)
			{
				for (int n : adj[v])
				{
					if (distances[n] == - 1)
					{
						distances[n] = d + 1;

						if (nbVerticesVisited++ >= maxSize)
							break;

						q[to++] = n;
					}
				}
			}
		}

		lp.end();

		BFSResult r = new BFSResult();
		r.visitOrder = q;
		r.distances = distances;
		r.nbVerticesVisited = to;
		return r;
	}

	public static int[] bsp_seq(int[][] adj, int src)
	{
		LongProcess lp = new LongProcess("BFS (seq BSP)", " iteration", - 1);
		int[] distances = new int[adj.length];
		Arrays.fill(distances, - 1);
		IntList inbox = new IntArrayList();
		inbox.add(0);
		IntList inboxSizeHistory = new IntArrayList();

		for (int distance = 0; ! inbox.isEmpty(); ++distance)
		{
			// inboxSizeHistory.add(inbox.size());
			++lp.sensor.progressStatus;

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
		Graph g = new Graph();
		g.out.mem.b = GridGenerator.dgrid_outs(100000, 100, true, true, false, false, 1);
		int[] distances = bsp_seq(g.out.mem.b, 0);
		// FastUtils.printAsMap(distances, " has distance ", System.out);
		System.out.println("*****");
		int[] distances2 = classic(g.out.mem.b, 0, Integer.MAX_VALUE,
				Integer.MAX_VALUE).distances;
		// Cout.debug(distances2);
		// FastUtils.printAsMap(distances2, " has distance ", System.out);
	}

	public static class Plugin implements TooolsPlugin<Direction, BFSResult>
	{
		private int src;

		@Override
		public BFSResult process(Direction d)
		{
			return BFS.classic(d.mem.b, src, Integer.MAX_VALUE, Integer.MAX_VALUE);
		}

		@Override
		public void setParameters(PluginParms p)
		{
			src = p.getInt("src");
		}
	}

}
