package jmg.exp.stephane;

import toools.text.TextUtilities;

class CountK22_Result
{
	public long nbIteration = 0;
	public long nK22 = 0;
	public long sumDegrees;
	public long nbPotentialK22 = 0;

	@Override
	public String toString()
	{
		double pK22 = nK22 / (double) nbIteration;

		double estimatedNbK22 = pK22 * sumDegrees / 2;

		String s = "";
		s += " - sumDegrees=" + sumDegrees;
		s += "\n - nK22=" + nK22;
		s += "\n - nbPotentialK22=" + TextUtilities.toHumanString(nbPotentialK22);
		s += "\n - nbIteration=" + nbIteration;
		s += "\n - pK22=" + pK22;
		s += "\n - estimatedNbK22=" + (int) estimatedNbK22;
		s += "\n - nK22 / nbPotentialK22=" + (nK22 / (double) nbPotentialK22);
		return s;
	}

}