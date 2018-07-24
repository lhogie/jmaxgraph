package jmg.exp.nathann;

public abstract class JSONElement
{
	public static String tabText = "   ";
	
	@Override
	public String toString()
	{
		return toString(0, true);
	}

	public abstract String toString(int tab, boolean alwaysQuote);

	public static String quoteIfNecessary(String s, boolean alwaysQuote)
	{
		if (alwaysQuote || s.contains(" ") || s.contains("\t") || s.contains("\n"))
			return "\"" + s + "\"";

		return s;
	}
}
