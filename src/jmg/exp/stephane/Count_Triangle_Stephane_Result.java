package jmg.exp.stephane;

public class Count_Triangle_Stephane_Result
{
	public long nbCyclicTriangles, nbTransitiveTriangles;
	public int nbIteration;

	@Override
	public String toString()
	{
		String s = "";
		s += " - nbCyclicTriangles=" + (nbCyclicTriangles / 3);
		s += "\n - nbTransitiveTriangles=" + nbTransitiveTriangles;
		s += "\n - pTransitiveTriangles="
				+ (nbTransitiveTriangles / (double) nbIteration);
		s += "\n - pCyclicTriangles="
				+ (nbCyclicTriangles / (double) nbIteration);
		s += "\nnbIteration=" + nbIteration;

		return s;
	}
}