package jmg;

import java.util.Iterator;

public interface AdjacencyPrimitives extends Iterable<VertexCursor>
{
	 int countVertices(int nbThreads);
	
	// random access
	int[] get(int u);

	// set all in one shot
	void setAllFrom(Adjacency from, int nbThreads);

	// sequential reading
	Iterator<VertexCursor> iterator(int from, int to);

	boolean isDefined();

}
