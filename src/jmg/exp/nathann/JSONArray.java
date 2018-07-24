package jmg.exp.nathann;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import toools.text.TextUtilities;

public class JSONArray extends JSONElement
{
	List l = new ArrayList<>();

	public JSONArray(Object... e)
	{
		l.addAll(Arrays.asList(e));
	}

	public JSONArray(int... e)
	{
		for (int n : e)
		{
			l.add(n);
		}
	}
	
	public int size()
	{
		return l.size();
	}

	public Object get(int i)
	{
		return l.get(i);
	}

	public String toString(int tab, boolean alwaysQuote)
	{
		String s = "[";

		for (int i = 0; i < l.size(); ++i)
		{
			s += "\n";
			s += TextUtilities.repeat(tabText, tab + 1);
			Object e = l.get(i);

			if (e instanceof JSONElement)
			{
				s += ((JSONElement) e).toString(tab + 1, alwaysQuote);
			}
			else
			{
				s += quoteIfNecessary(e.toString(), alwaysQuote);
			}

			if (i < l.size() - 1)
			{
				s += ",";
			}
		}

		s += "\n" + TextUtilities.repeat(tabText, tab) + "]";
		return s;
	}


}
