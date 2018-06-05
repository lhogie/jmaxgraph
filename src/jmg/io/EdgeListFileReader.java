package jmg.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import j4u.chain.PluginConfig;
import j4u.chain.TooolsPlugin;
import jmg.Digraph;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;
import toools.util.Conversion;

public class EdgeListFileReader
{
	public static class Plugin implements TooolsPlugin<Void, Digraph>
	{
		public RegularFile from;

		@Override
		public Digraph process(Void v)
		{
			try
			{
				Digraph g = new Digraph();
				g.out.mem.b = load(from);
				return g;
			}
			catch (IOException e)
			{
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void setup(PluginConfig parms)
		{
		}
	}

	public static int[][] load(RegularFile f) throws FileNotFoundException
	{
		Int2ObjectMap<IntList> map = parse(f);
		int nbVertex = map.size();
		int[][] adj = new int[nbVertex][];

		LongProcess lp = new LongProcess("converting " + f, " arc", nbVertex);

		for (int v = 0; v < nbVertex; ++v)
		{
			++lp.sensor.progressStatus;

			if (map.containsKey(v))
			{
				int[] neighbors = map.get(v).toIntArray();
				adj[v] = neighbors;
			}
		}

		lp.end();
		return adj;
	}

	public static Int2ObjectMap<IntList> parse(RegularFile f) throws FileNotFoundException
	{
		LongProcess lp = new LongProcess("parsing " + f, " number", - 1);
		InputStream is = f.createReadingStream();
		Scanner sc = new Scanner(is);
		int expectedNbEdge = (int) (f.getSize() / 15);
		Int2ObjectMap<IntList> r = new Int2ObjectOpenHashMap<>(expectedNbEdge);
		int nbEdge = 0;

		while (sc.hasNext())
		{
			++lp.sensor.progressStatus;
			int a = sc.nextInt();
			int b = sc.nextInt();
			neighbor(a, r, nbEdge).add(b);
			neighbor(b, r, nbEdge);
			++nbEdge;
		}

		sc.close();
		lp.end();
		return r;
	}

	public static IntList neighbor(int v, Int2ObjectMap<IntList> m, long nbEdge)
	{
		IntList neighbors = m.get(v);

		if (neighbors == null)
		{
			long nbVertex = m.size() + 1;
			int avgDegree = Conversion.long2int(nbEdge / nbVertex);
			m.put(v, neighbors = new IntArrayList(avgDegree));
		}

		return neighbors;
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		int[][] r = load(new RegularFile("/Users/lhogie/biggrph/datasets/acc2007_2.tsv"));
		Digraph g = new Digraph();
		g.out.mem.b = r;

		System.out.println(g.countArcs(1));
	}

}
