package jmg.exp.thibaud;

public class CountK22v2_Result
{
	public long nK22 = 0;
	public long nbK22pot;

	@Override
	public String toString()
	{
		String s = "";
		s += " - nbK22=" + nK22;
		s += "\n - nbK22pot=" + nbK22pot;
		s += "\n - CK=" + (4 * nK22 / (double) nbK22pot);
		return s;
	}
}