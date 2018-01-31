package jmg.exp.nathann;

public class CountK22_Result
{
	public long nk22 = - 1;
	public long twiceNK22 = 0;
	public long nbK22pot;

	@Override
	public String toString()
	{
		String s = "";
		s += " - nbK22=" + nk22;
		s += "\n - nbK22pot=" + nbK22pot;
		s += "\n - CK=" + (4d * nk22 / nbK22pot);
		return s;
	}
}