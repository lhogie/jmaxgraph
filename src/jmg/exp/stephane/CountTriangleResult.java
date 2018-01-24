package jmg.exp.stephane;

public class CountTriangleResult
{
	public long nbCyclicTriangles, nbTransitiveTriangles;
	public int nbIteration;
	public long nPossibleEvents;

	@Override
	public String toString()
	{
		String s = "";
		s += " - 3*nbCyclicTriangles=" + nbCyclicTriangles;
		s += "\n - nbTransitiveTriangles=" + nbTransitiveTriangles;
		double pTransitive = nbTransitiveTriangles / (double) nbIteration;
		s += "\n - pTransitiveTriangles=" + pTransitive;

		double pCyclic = nbCyclicTriangles / (double) nbIteration;
		s += "\n - pCyclicTriangles=" + pCyclic;
		s += "\nnbIteration=" + nbIteration;
		s += "\nnbpossibleEvents=" + nPossibleEvents;
		s += "\nnb estimated nb cycle triangles=" + (pCyclic * nPossibleEvents);
		s += "\nnb estimated nb transitive triangles=" + (pTransitive * nPossibleEvents);

		return s;
	}
}