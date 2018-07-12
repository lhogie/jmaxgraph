package jmg.exp.nathann;

import java.io.Serializable;

public class LocalCount implements Serializable, JSONable
{
	public final int startVertex, endVertex;
	public long nbK22s_times2 = 0;
	public long nbK22sPot;
	public long nbTransitiveTriangles;
	public long nbCyclicTriangles_times3;
	public long nbTrianglesPot;
	public long[] nbK22sPerVertex_times2;
	public long[] nbK22sPotPerVertex;
	public long[] nbTrianglesPerVertex;
	public long[] nbTrianglesPotPerVertex;

	public LocalCount(int startVertex, int endVertex)
	{
		this.startVertex = startVertex;
		this.endVertex = endVertex;
	}

	@Override
	public String toString()
	{
		return toJSONElement().toString();
	}

	public long nk22()
	{
		return nbK22s_times2 / 2;
	}

	@Override
	public JSONMap toJSONElement()
	{
		JSONMap m = new JSONMap();
		m.add("from", startVertex);
		m.add("to", endVertex);
		m.add("nbK22", nk22());
		m.add("nbK22pot", nbK22sPot);
		m.add("CK", (4d * nk22() / nbK22sPot));
		m.add("nbTransitiveTriangles", nbTransitiveTriangles);
		m.add("nbCyclicTriangles", (nbCyclicTriangles_times3 / 3));
		m.add("nbTrianglesPot", startVertex);
		m.add("from", nbTrianglesPot);
		return m;
	}
}