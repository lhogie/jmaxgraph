package jmg.exp.thibaud;

public class CountK22_Result
{
	public long nbK22pot = 0;
	public long nbK22 = 0;
	public int[] distri;

	@Override
	public String toString()
	{
		String s = "";
		s += " - nbK22=" + nbK22;
		s += "\n - nbK22potK22pot=" + nbK22pot;
		s += "\n - CK=" + (4 * nbK22 / (double) nbK22pot);
		return s;
	}
}