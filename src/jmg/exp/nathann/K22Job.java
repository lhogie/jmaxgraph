package jmg.exp.nathann;

import jmg.Graph;
import jmg.io.jmg.JMGDirectory;
import jmr.Job;

public class K22Job extends Job<CountK22WorkerOutput>
{
	static final long serialVersionUID = 876543;
	static Graph g;

	String jmgDirectoryPath;
	int startVertex, endVertex;
	int nbThreads;
	boolean countK22;

	@Override
	public CountK22WorkerOutput call() throws Exception
	{
		nbThreads = countK22 ? 8 : 16;

		JMGDirectory d = new JMGDirectory(jmgDirectoryPath);

		if (g == null)
		{
			g = new Graph(d, false, nbThreads);
		}

		LocalCount ar;

		if (countK22)
		{
			ar = K22AndTransitiveTrianglesCounter.count(g, startVertex, endVertex,
					nbThreads);
		}
		else
		{
			ar = TrianglesCounter.count(g, startVertex, endVertex, nbThreads);
		}

		return new CountK22WorkerOutput(ar, mapReduce.resultAuxDir, file.getName());
	}

	@Override
	public String toString()
	{
		String s = super.toString();
		s += "\n vertices from " +  startVertex + " to " +  endVertex;
		s += "\n - nbThreads=" + nbThreads;
		return s;
	}
}
