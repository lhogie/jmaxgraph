package jmg.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.gen.GridGenerator;
import toools.io.Bits;
import toools.io.Utilities;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;

public class JMGWriter
{

	public static class Plugin implements TooolsPlugin<Digraph, JMGDataset>
	{
		public Directory to;

		@Override
		public JMGDataset process(Digraph g)
		{
			try
			{
				return write(g, to);
			}
			catch (IOException e)
			{
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void setup(PluginConfig p)
		{
		}
	}

	public static JMGDataset write(Digraph g, Directory d) throws IOException
	{
		d.mkdirs();
		writeProperties(d, g);

		assert new IntOpenHashSet(g.label2vertex)
				.size() == g.label2vertex.length : new IntOpenHashSet(g.label2vertex)
						.size() + " != " + g.label2vertex.length;

		if (g.label2vertex != null)
		{
			JMGDataset.getLabel2VertexFile(d).saveValues(g.label2vertex.length,
					l -> g.label2vertex[l], 4);
		}

		int[][] adj = g.getRefAdj();

		if (adj != null)
		{
			writeADJ(d, adj);
		}

		return new JMGDataset(d);
	}

	public static void writeProperties(Directory d, Digraph g) throws IOException
	{
		Properties p = new Properties();
		p.put("nbVertices", "" + g.getNbVertex());
		p.put("adjType", getRefAdjType(g));
		OutputStream pos = JMGDataset.getPropertyFile(d).createWritingStream();
		p.store(pos, "JMG property file");
		pos.close();
	}

	public static String getRefAdjType(Digraph g)
	{
		if (g.out == null && g.in == null)
			return "none";
		else if (g.out == null)
			return "in";
		else
			return "out";
	}

	public static void writeADJ(Directory d, int[][] adj) throws IOException
	{
		LongProcess saving = new LongProcess("saving ADJ", " adj-list", adj.length);

		RegularFile adjFile = JMGDataset.getADJFile(d);
		OutputStream os = adjFile.createWritingStream(false, 1024 * 1024 * 1024);

		byte[] b = new byte[8];

		int nbVertex = adj.length;
		long[] index = new long[nbVertex];

		// tracks the index for each entry
		long pos = 0;

		for (int u = 0; u < nbVertex; ++u)
		{
			index[u] = pos;

			// write the number of neighbors
			int[] neighbors = adj[u];

			if (neighbors.length > 0)
			{
				// writes the first neighbor
				Bits.putLong(b, 0, neighbors[0]);
				int previous = neighbors[0];
				os.write(b, 0, 8);
				pos += 8;

				if (neighbors.length > 1)
				{
					// write the encoding for the other neighbors
					int encoding = Utilities.getNbBytesRequireToEncode(maxgap(neighbors));
					Bits.putLong(b, 0, encoding, 1);
					os.write(b, 0, 1);
					pos += 1;

					// write other neighbors
					for (int i = 1; i < neighbors.length; ++i)
					{
						int v = neighbors[i];
						int delta = v - previous;
						Bits.putLong(b, 0, delta, encoding);
						os.write(b, 0, encoding);
						pos += encoding;
						previous = v;
					}
				}
			}

			saving.progressStatus.incrementAndGet();
		}

		os.close();

		JMGDataset.getIndexFile(d).saveValues(index);
		saving.end();
	}

	private static int maxgap(int[] a)
	{
		int r = 0;

		for (int i = 1; i < a.length; ++i)
		{
			int diff = a[i] - a[i - 1];
			assert diff >= 0;

			if (diff > r)
				r = diff;
		}

		return r;
	}

	public static void main(String[] args) throws IOException
	{
		Digraph g = new Digraph();
		g.out = GridGenerator.dgrid_outs(100, 100, true, true, false, false);

		Directory d = new Directory("$HOME/tmp/grid100x100");
		new JMGWriter().write(g, d);
	}

}
