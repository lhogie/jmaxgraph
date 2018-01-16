package jmg.io.adj;

import java.io.OutputStream;
import java.io.PrintStream;

import jmg.Digraph;
import toools.io.PrintStreamCounter;
import toools.progression.LongProcess;

public class TextADJWriter extends ADJWriter
{
	@Override
	public long[] write(Digraph g, OutputStream os)
	{
		PrintStreamCounter out = new PrintStreamCounter(new PrintStream(os));
		int nbVertex = g.out.length;
		LongProcess p = new LongProcess("writing text ADJ", nbVertex);
		long[] index = new long[nbVertex];
		int pos = 0;
		pos += out.print(nbVertex);
		pos += out.print('\n');

		for (int label = 0; label < nbVertex; ++label)
		{
			index[label] = pos;
			pos += out.print(g.label2vertex == null ? label : g.label2vertex[label]);
			pos += out.print(' ');

			int[] neighbors = g.out[label];
			pos += out.print(neighbors.length);
			
			if (neighbors.length > 0)
			{
				pos += out.print(' ');

				for (int i = 0; i < neighbors.length; ++i)
				{
					int n = neighbors[i];

					if (g.label2vertex != null)
						n = g.label2vertex[n];

					pos += out.print(n);

					if (i < neighbors.length - 1)
					{
						pos += out.print(' ');
					}
				}
			}

			pos += out.print('\n');
			p.progressStatus.incrementAndGet();
		}

		out.flush();
		p.end();
		return index;
	}

}
