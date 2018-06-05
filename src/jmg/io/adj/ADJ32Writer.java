package jmg.io.adj;

import java.io.IOException;
import java.io.OutputStream;

import jmg.Digraph;
import toools.io.Bits;
import toools.progression.LongProcess;

public class ADJ32Writer extends ADJWriter
{

	@Override
	public long[] write(Digraph g, OutputStream os) throws IOException
	{
		int nbVertex = g.getNbVertices();
		long pos = 4;
		long[] index = new long[nbVertex];

		// int segmentSize = nbVertex /
		LongProcess lp = new LongProcess("writing binary ADJ", " adjlist", nbVertex);
		byte[] b = new byte[4];
		Bits.putInt(b, 0, nbVertex);
		os.write(b);

		for (int v = 0; v < nbVertex; ++v)
		{
			int[] neighbors = g.out.mem.b[v];

			index[v] = pos;
			Bits.putInt(b, 0, g.labelling == null ? v : g.labelling.label2vertex[v]);
			os.write(b);

			Bits.putInt(b, 0, neighbors.length);
			os.write(b);

			for (int i = 0; i < neighbors.length; ++i)
			{
				int n = neighbors[i];
				Bits.putInt(b, 0, g.labelling == null ? n : g.labelling.label2vertex[n]);
				os.write(b);
			}

			++lp.sensor.progressStatus;
			pos += 8 + neighbors.length * 4;
		}

		lp.end(nbVertex + " vertices written");
		return index;
	}
}
