package jmg.exp;

class CountK2_2_Thilbaud_Result
{
	long fourTimesNbK22pot = 0;
	long nK22 = 0;
	int[] distri;

	@Override
	public String toString()
	{
		return nK22 + " K22 and " + (fourTimesNbK22pot) + " K22pot, CK="
				+ 4 * nK22 / (double) fourTimesNbK22pot;
	}
}