package jmg;

import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import jmg.algo.Degrees;
import jmg.algo.ReverseGraph;
import jmg.io.jmg.EDGFile;
import toools.math.MathsUtilities;
import toools.progression.LongProcess;
import toools.thread.ParallelIntervalProcessing;

public class Direction
{
	public int[][] adj;
	public EDGFile file;
	public Direction opposite;

	public int[] degrees()
	{
		if (adj == null)
			throw new JMGException("no ADJ defined");

		return Degrees.computeDegrees(adj);
	}

	public void load(int nbThreads)
	{
		adj = file.readADJ(nbThreads);
	}

	public void computeFromOppositeDirection()
	{
		adj = null;
		System.gc();
		adj = ReverseGraph.computeInverseADJ(opposite.adj, false);
	}

	public void save() throws IOException
	{
		file.writeADJ(adj);
	}

	public void ensureDefined()
	{
		if (adj == null)
		{
			// it's on disk
			if (file != null && file.exists())
			{
				load(8);
			}
			else
			{
				if (opposite.adj == null
						&& (opposite.file == null || ! opposite.file.exists()))
					throw new IllegalStateException();

				opposite.ensureDefined();
				computeFromOppositeDirection();
			}
		}
	}

	public void from(Int2ObjectMap<int[]> adj, boolean addUndeclared, boolean sort,
			Labelling labelling)
	{
		if (addUndeclared)
		{
			Utils.addUndeclaredVertices(adj);
		}

		if (labelling != null)
		{
			labelling.label2vertex = adj.keySet().toIntArray();
			labelling.vertex2label = new Vertex2LabelMap(labelling.label2vertex);
		}

		load(adj, labelling);

		if (sort)
		{
			ensureSorted();
		}
	}

	public void ensureSorted()
	{
		Utils.ensureSorted(adj);
	}

	private void load(Int2ObjectMap<int[]> m, Labelling labelling)
	{
		int nbVertex = m.size();
		adj = new int[nbVertex][];
		LongProcess relabelSrc = new LongProcess(
				"relabelling " + nbVertex + " src vertices", " vertex", adj.length);

		for (int u = 0; u < nbVertex; ++u)
		{
			adj[u] = m.get(labelling == null ? u : labelling.label2vertex[u]);
			++relabelSrc.progressStatus;
		}

		relabelSrc.end();

		if (labelling != null)
		{
			LongProcess relabelADJ = new LongProcess("relabelling", " list", adj.length);

			new ParallelIntervalProcessing(adj.length)
			{

				@Override
				protected void process(int rank, int lowerBound, int upperBound)
				{
					for (int v = lowerBound; v < upperBound; ++v)
					{
						int[] _list = adj[v];
						int _sz = _list.length;

						for (int _neighborIndex = 0; _neighborIndex < _sz; ++_neighborIndex)
						{
							int neighbor = _list[_neighborIndex];
							assert labelling.vertex2label
									.containsKey(neighbor) : neighbor;
							int neighborLabel = labelling.vertex2label.get(neighbor);
							_list[_neighborIndex] = neighborLabel;
						}

						++relabelADJ.progressStatus;
					}
				}
			};

			relabelADJ.end();
		}
	}

	public int maxDegree()
	{
		return MathsUtilities.max(degrees());
	}

}
