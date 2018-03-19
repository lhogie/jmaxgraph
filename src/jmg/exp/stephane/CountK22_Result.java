package jmg.exp.stephane;

import toools.text.TextUtilities;

class CountK22_Result
{
	public long nbIteration = 0;
	public long nK22times2 = 0;
	public long sumDegrees;
	public long nbPotentialK22 = 0;

	@Override
	public String toString()
	{
		double pK22 = nK22times2 / (double) nbIteration;

		double estimatedNbK22 = pK22 * sumDegrees / 2;

		long nbK22 = nK22times2 / 2;

		String s = "";
		s += "nbIteration=" + nbIteration;
		s += "\n - sumDegrees=" + sumDegrees;
		s += "\n - nK22=" + nbK22;
		s += "\n - nbPotentialK22=" + TextUtilities.toHumanString(nbPotentialK22);
		s += "\n - nbIteration=" + nbIteration;
		s += "\n - pK22=" + pK22;
		s += "\n - estimatedNbK22=" + (int) estimatedNbK22;
		s += "\n - nK22 / nbPotentialK22=" + (4 * nbK22 / (double) nbPotentialK22);
		return s;
	}

	public String forSteph()
	{

		long nbK22 = nK22times2 / 2;

		return nbIteration + " " + nbK22 + " " + nbPotentialK22;
	}

}