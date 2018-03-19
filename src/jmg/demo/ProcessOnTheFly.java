package jmg.demo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import jmg.Digraph;
import jmg.gen.DirectedGNP;
import jmg.io.jmg.ArcFileVertexIterator.ArcFileCursor;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;
import toools.thread.ParallelIntervalProcessing;

public class ProcessOnTheFly
{
	public static void main(String[] args) throws IOException
	{
		Cout.debug("start");
		Digraph g = new Digraph();
		g.out.adj = DirectedGNP.out(1000, 0.1, new Random(), 1);
		g.nbVertices = g.out.adj.length;
		JMGDirectory d = new JMGDirectory("$HOME/datasets/demo.jmg");
		g.write(d);
		g.setDataset(d);

		new ParallelIntervalProcessing(g.getNbVertices(), 1, null)
		{
			@Override
			protected void process(ThreadSpecifics s, int lowerBound, int upperBound)
					throws IOException
			{
				Iterator<ArcFileCursor> i = g.out.file.iterator(lowerBound, upperBound,
						100, 256 * 256 * 256);

				while (i.hasNext())
				{
					ArcFileCursor r = i.next();
					Cout.result("Vertex " + r.vertex + " has degree " + r.adj.length);
				}
			}
		};

		for (ArcFileCursor c : g.out.file)
		{
			Cout.result(c.vertex);
		}
	}
}
