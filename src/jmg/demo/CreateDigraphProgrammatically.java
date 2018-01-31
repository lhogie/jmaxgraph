package jmg.demo;

import java.util.Random;

import jmg.Digraph;
import toools.progression.LongProcess;

public class CreateDigraphProgrammatically
{
	public static void main(String[] args)
	{
		Digraph g = new Digraph();
		g.out.adj = new int[1][];
		g.out.adj[0] = new int[0];
		Random r = new Random();

		LongProcess p = new LongProcess("perfomance", 1000000);

		for (int i = 0; i < 1000000; ++i)
		{
			g.addArc(0, r.nextInt());
			++p.sensor.progressStatus;
		}

		p.end();
	}
}
