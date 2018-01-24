package jmg.exp.thibaud;

public class CountTriangles_Result
{
	public long threeTimesNbCyclicTriangles, nbTransitiveTriangles;
	public long nbPotentialTriangles_computed, nbPotentialTriangles_incremented;
	public long nbOfBidiArcs;

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
		s += "\nnbOfBidiArcs=" + nbOfBidiArcs;

		return s;
	}
}