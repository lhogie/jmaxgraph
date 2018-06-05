package jmg.io;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import jmg.Digraph;
import jmg.Labelling;

public class TinyReader
{
	public static class Text
	{
		StringBuilder b = new StringBuilder();

		public void addLine(int... N)
		{
			for (int n : N)
			{
				b.append(" " + n);
			}

			b.append('\n');
		}

		@Override
		public String toString()
		{
			return b.toString();
		}

		public Digraph toGraph()
		{
			Digraph g = new Digraph();
			g.labelling = new Labelling();

			Int2ObjectMap<int[]> adj = TinyReader.parse(toString());
			g.out.mem.from(adj, true, true, g.labelling, 1);
			g.nbVertices = g.out.mem.b.length;
			return g;
		}
	}

	public static Int2ObjectMap<int[]> parse(String text)
	{
		Int2ObjectMap<int[]> r = new Int2ObjectOpenHashMap<>();

		for (String line : text.split("\n"))
		{
			line = line.trim();

			if ( ! line.isEmpty())
			{
				line = line.replace('\t', ' ');
				String[] numbers = line.split(" +");
				int[] N = new int[numbers.length - 1];

				for (int i = 1; i < numbers.length; ++i)
				{
					N[i - 1] = Integer.parseInt(numbers[i]);
				}

				int src = Integer.parseInt(numbers[0]);
				r.put(src, N);
			}
		}

		return r;
	}

}
