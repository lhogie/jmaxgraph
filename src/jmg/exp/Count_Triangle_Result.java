package jmg.exp;

public class Count_Triangle_Result
{
	long threeTimesNbCyclicTriangles, nbTransitiveTriangles;
	long nbPotentialTriangles_computed, nbPotentialTriangles_incremented;

	@Override
	public String toString()
	{
		return " - nbCyclicTriangles=" + (threeTimesNbCyclicTriangles / 3)
				+ "\n - nbTransitiveTriangles=" + nbTransitiveTriangles
				+ "\n - nbPotentialTriangles_computed=" + nbPotentialTriangles_computed
				+ "\n - nbPotentialTriangles_incremented="
				+ nbPotentialTriangles_incremented;
	}
}