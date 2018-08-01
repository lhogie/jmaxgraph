package jmg.io.adj;

import java.io.OutputStream;
import java.io.PrintStream;

import jmg.Graph;
import jmg.VertexCursor;
import toools.io.PrintStreamCounter;
import toools.progression.LongProcess;

public class TextADJWriter extends ADJWriter
{

	@Override
	public long[] write(Graph g, OutputStream os)
	{
		PrintStreamCounter out = new PrintStreamCounter(new PrintStream(os));
		int nbVertex = g.getNbVertices();
		LongProcess p = new LongProcess("writing text ADJ", " adj-list", nbVertex);
		long[] index = new long[nbVertex];
		int pos = 0;
		pos += out.print(nbVertex);
		pos += out.print('\n');

		
		for (VertexCursor u : g.out)
		{
			index[u.vertex] = pos;
			pos += out
					.print(g.labelling == null ? u.vertex : g.labelling.label2vertex[u.vertex]);
			pos += out.print(' ');

			int[] neighbors = u.adj;
			pos += out.print(neighbors.length);

			if (neighbors.length > 0)
			{
				pos += out.print(' ');

				for (int i = 0; i < neighbors.length; ++i)
				{
					int n = neighbors[i];

					if (g.labelling != null)
						n = g.labelling.label2vertex[n];

					pos += out.print(n);

					if (i < neighbors.length - 1)
					{
						pos += out.print(' ');
					}
				}
			}

			pos += out.print('\n');
			++p.sensor.progressStatus;
		}
		
		out.flush();
		p.end();
		return index;
	}

}
