package jmg;

import toools.collection.LazyArray;
import toools.progression.LongProcess;

public class Vertex2LabelMap extends LazyArray
{
	public Vertex2LabelMap(Vertex2LabelMap a)
	{
		super(a);
	}

	public Vertex2LabelMap(int[] label2vertex)
	{
		super(128, - 1);

		int nbLabels = label2vertex.length;
		LongProcess pm = new LongProcess("creating (vertex => label) map", nbLabels);

		for (int label = 0; label < nbLabels; ++label)
		{
			++pm.sensor.progressStatus;

			int v = label2vertex[label];
			put(v, label);
		}

		assert countDefinedCells() == label2vertex.length;
		pm.end(nbLabels + " labels defined");
	}

}
