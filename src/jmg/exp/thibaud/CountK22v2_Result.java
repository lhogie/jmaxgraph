package jmg.exp.thibaud;

import java.util.concurrent.atomic.AtomicLong;

public class CountK22v2_Result
{
	public long nbK22 = 0;
	public long nbK22pot;
	protected AtomicLong nbVertices = new AtomicLong(0);

	@Override
	public String toString()
	{
		String s = "";
		s += " - nbK22=" + nbK22;
		s += "\n - nbK22pot=" + nbK22pot;
		s += "\n - CK=" + (4 * nbK22 / (double) nbK22pot);
		s += "\n\n";
		s += "nbVertices=" + nbVertices.get();
		return s;
	}
}