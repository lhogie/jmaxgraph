package jmg.exp;

public class Count_Triangle_Result
{
	long threeTimesNbCyclicTriangles, nbTransitiveTriangles;
	long nbPotentialTriangles_computed, nbPotentialTriangles_incremented;

	@Override
	public String toString()
	{
		String s = "";
		s += " - nbCyclicTriangles=" + (threeTimesNbCyclicTriangles / 3);
		s += "\n - nbTransitiveTriangles=" + nbTransitiveTriangles;
		s += "\n - nbPotentialTriangles_computed=" + nbPotentialTriangles_computed;
		s += "\n - nbPotentialTriangles_incremented=" + nbPotentialTriangles_incremented;

		s += "\n - CCcyclic=" + (threeTimesNbCyclicTriangles
				/ (double) nbPotentialTriangles_incremented);
		s += "\n - CCtransitive="
				+ (nbTransitiveTriangles / (double) nbPotentialTriangles_incremented);

		return s;
	}
}