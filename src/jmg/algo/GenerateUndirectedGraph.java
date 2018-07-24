package jmg.algo;

import java.io.IOException;
import java.util.Iterator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import jmg.Graph;
import jmg.VertexCursor;
import jmg.io.jmg.ArcFileWriter;
import jmg.io.jmg.JMGDirectory;
import toools.progression.LongProcess;

public class GenerateUndirectedGraph
{
	public static void main(String[] args) throws IOException
	{
		JMGDirectory d = new JMGDirectory(args[0]);
		Graph g = new Graph(d);

		JMGDirectory outd = new JMGDirectory(args[1]);
		Graph h = new Graph(outd);
		
		Iterator<VertexCursor> outIterator = g.out.disk.iterator();
		Iterator<VertexCursor> inIterator = g.in.disk.iterator();


		int nbVertices = g.getNbVertices();
		LongProcess lp = new LongProcess("generating undirected topology", " vertex",
				nbVertices);

		ArcFileWriter w = new ArcFileWriter(h.out.disk.getArcFile(), nbVertices);

		IntArrayList r = new IntArrayList();

		for (int u = 0; u < nbVertices; ++u)
		{
			lp.sensor.progressStatus++;
			VertexCursor outC = outIterator.next();
			VertexCursor inC = inIterator.next();

			if (outC.vertex != u)
				throw new IllegalStateException();

			if (inC.vertex != u)
				throw new IllegalStateException();

			int i = 0, j = 0;

			// drop elements lower than current vertex
			while (i < outC.adj.length && outC.adj[i] <= u)
				i++;

			while (j < inC.adj.length && inC.adj[j] <= u)
				j++;

			while (i < outC.adj.length && j < inC.adj.length)
			{
				if (outC.adj[i] < inC.adj[j])
				{
					r.add(outC.adj[i++]);
				}
				else if (outC.adj[i] > inC.adj[j])
				{
					r.add(inC.adj[j++]);
				}
				else
				{
					r.add(outC.adj[i++]);
					++j;
				}
			}

			while (i < outC.adj.length)
			{
				r.add(outC.adj[i++]);
			}

			while (j < inC.adj.length)
			{
				r.add(inC.adj[j++]);
			}

			w.writeADJ(r.toIntArray());
			r.clear();
		}

		w.close();

		if (inIterator.hasNext())
			throw new IllegalStateException();

		lp.end();

	}
}
