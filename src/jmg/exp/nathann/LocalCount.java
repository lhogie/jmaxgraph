package jmg.exp.nathann;

public class LocalCount
{
	public int startVertex, endVertex;
	public long nbK22s_times2 = 0;
	public long nbK22sPot;
	public long nbTransitiveTriangles;
	public long nbCyclicTriangles_times3;
	public long nbTrianglesPot;
	public long[] nbK22sPerVertex_times2;
	public long[] nbK22sPotPerVertex;
	public long[] nbTrianglesPerVertex;
	public long[] nbTrianglesPotPerVertex;

	@Override
	public String toString()
	{
		String s = "";
		s += " - from " + startVertex + " to " + endVertex;
		s += "\n - nbK22=" + nk22();
		s += "\n - nbK22pot=" + nbK22sPot;
		s += "\n - CK=" + (4d * nk22() / nbK22sPot);
		s += "\n - nbTransitiveTriangles=" + nbTransitiveTriangles;
		s += "\n - nbCyclicTriangles=" + (nbCyclicTriangles_times3 / 3);
		s += "\n - nbTrianglesPot=" + nbTrianglesPot;
		return s;
	}

	public long nk22()
	{
		return nbK22s_times2 / 2;
	}
}