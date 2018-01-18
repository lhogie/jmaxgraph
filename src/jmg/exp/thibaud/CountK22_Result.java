package jmg.exp.thibaud;

public class CountK22_Result
{
	public long fourTimesNbK22pot = 0;
	public long nK22 = 0;
	public int[] distri;

	@Override
	public String toString()
	{
		String s = "";
		s += " - nbK22=" + nK22;
		s += "\n - K22pot=" + (fourTimesNbK22pot);
		s += "\n - CK=" + (4 * nK22 / (double) fourTimesNbK22pot);
		return s;
	}
}