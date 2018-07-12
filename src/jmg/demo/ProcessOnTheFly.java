package jmg.demo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import jmg.Graph;
import jmg.ParallelAdjProcessing;
import jmg.VertexCursor;
import jmg.gen.DirectedGNP;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;
import toools.thread.MultiThreadProcessing.ThreadSpecifics;

public class ProcessOnTheFly
{
	public static void main(String[] args) throws IOException
	{
		Cout.debug("start");
		Graph g = new Graph();
		g.out.mem = DirectedGNP.out(1000, 0.1, new Random(), 1);
		JMGDirectory d = new JMGDirectory("$HOME/datasets/demo.jmg");
		g.write(d);
		g.setDataset(d);

		new ParallelAdjProcessing(g.out.disk, 8, null)
		{

			@Override
			public void f(ThreadSpecifics s, Iterator<VertexCursor> i)
			{
				while (i.hasNext())
				{
					VertexCursor r = i.next();
					Cout.result("Vertex " + r.vertex + " has degree " + r.adj.length);
				}
			}
		};

		for (VertexCursor c : g.out.disk)
		{
			Cout.result(c.vertex);
		}
	}
}
