package jmg.exp;
class CountK2_2_Stephane_Result
{
	long nbIteration = 0;
	long nK22 = 0;
	long sumDegrees;

	@Override
	public String toString()
	{
		double pK22 = nK22 / (double) nbIteration;
		double estimatedNbK22 = pK22 * sumDegrees / 2;

		return "sumDegrees=" + sumDegrees + "\nnK22=" + nK22 + "\nnbIteration="
				+ nbIteration + "\npK22=" + pK22 + "\nestimatedNbK22="
				+ (int) estimatedNbK22;
	}

}