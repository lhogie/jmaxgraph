package jmg.demo;

import jmg.Graph;
import jmg.JmgUtils;
import jmg.io.jmg.JMGDirectory;
import toools.SystemMonitor;
import toools.io.Cout;
import toools.progression.LongProcess;

public class QuickStart
{
	public static void main(String[] args)
	{
		// the graph is stored as JMG in this directory
		JMGDirectory d = new JMGDirectory("/Users/lhogie/a/datasets/random_graph.jmg");

		// use all cores on the computer
		int nbThreads = Runtime.getRuntime().availableProcessors();

		// map a graph on this directory. No data is loaded here
		Graph g = new Graph(d, false, nbThreads);

		// starts the system monitor, which will periodically print information
		// on the system load
		SystemMonitor.defaultMonitor.start();

		// makes sure the OUTs adjacency is loaded in RAM
		g.out.ensureLoaded(nbThreads);

		Cout.result("#vertices: " + g.getNbVertices());
		Cout.result("#arcs: " + g.getNbArcs());
		
		// gets the number of vertices in this graph
		int nbVertices = g.getNbVertices();

		int nbLoops = 0;

		// we will monitor the evolution of the counting algorithm
		LongProcess lp = new LongProcess("couting loops", " vertex", nbVertices);

		// iterator over all vertices in RAM
		for (int u = 0; u < nbVertices; ++u)
		{
			// retrieves its out-neighbors
			int[] outs = g.out.mem.get(u);

			// check if u is a successor of itself
			if (JmgUtils.contains(outs, u))
			{
				++nbLoops;
			}

			// the counting algorithm has done one more step
			lp.sensor.progressStatus++;
		}

		// the process has completed
		lp.end();

		Cout.result("Nubmer of loops: " + nbLoops);
	}
}
