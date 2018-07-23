package jmg.algo;

import java.io.IOException;
import java.util.Iterator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import jmg.Graph;
import jmg.VertexCursor;
import jmg.io.jmg.ArcFileWriter;
import jmg.io.jmg.JMGDirectory;
import toools.progression.LongProcess;

public class GenerateUndirectedGraph2
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 0)
		{
			System.out.println(GenerateUndirectedGraph2.class + " srcJmg destJmg");
			System.exit(1);
		}
		
		JMGDirectory d = new JMGDirectory(args[0]);
		Graph g = d.mapGraph(8, false);
		Graph h = new Graph();
		h.out.mem.b = new int[g.getNbVertices()][];

		Iterator<VertexCursor> outIterator = g.out.disk.iterator();
		Iterator<VertexCursor> inIterator = g.in.disk.iterator();

		IntArrayList r = new IntArrayList();

		int nbVertices = g.getNbVertices();
		LongProcess lp = new LongProcess("generating undirected topology", " vertex",
				nbVertices);

		JMGDirectory outd = new JMGDirectory(args[1]);
		outd.create();
		ArcFileWriter w = new ArcFileWriter(outd.outFile, nbVertices);

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
					i++;
				}
				else if (outC.adj[i] > inC.adj[j])
				{
					j++;
				}
				else
				{
					r.add(outC.adj[i++]);
					++j;
				}
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
