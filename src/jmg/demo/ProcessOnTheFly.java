package jmg.demo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import jmg.Digraph;
import jmg.gen.DirectedGNP;
import jmg.io.jmg.EDGFileVertexIterator.EDGFileCursor;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;
import toools.thread.ParallelIntervalProcessing;

public class ProcessOnTheFly
{
	public static void main(String[] args) throws IOException
	{
		Cout.debug("start");
		Digraph g = new Digraph();
		g.out.adj = DirectedGNP.out(1000, 0.1, new Random());
		g.nbVertices = g.out.adj.length;
		JMGDirectory d = new JMGDirectory("$HOME/datasets/demo.jmg");
		g.write(d);
		g.setDataset(d);

		new ParallelIntervalProcessing(g.getNbVertex())
		{
			@Override
			protected void process(int rank, int lowerBound, int upperBound)
					throws IOException
			{
				Iterator<EDGFileCursor> i = g.out.file.iterator(lowerBound, upperBound, 100);

				while (i.hasNext())
				{
					EDGFileCursor r = i.next();
					Cout.result("Vertex " + r.vertex + " has degree " + r.adj.length);
				}
			}
		};

		for (EDGFileCursor c : g.out.file)
		{
			Cout.result(c.vertex);
		}
	}
}
