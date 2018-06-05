package jmg.io.jmg;

import jmg.io.jmg.ArcFile.Entry;
import toools.io.Cout;

public class read_adj
{
	public static void main(String[] args)
	{
		ArcFile f = new ArcFile(args[0]);

		if (args.length == 1)
		{
			for (int i = 0;; ++i)
			{
				int u = Integer.valueOf(i);
				Entry e = f.readEntry(u);
				Cout.result(e);
			}
		}
		else
		{
			for (int i = 1; i < args.length; ++i)
			{
				int u = Integer.valueOf(args[i]);
				Entry e = f.readEntry(u);
				Cout.result(e);

				// Cout.debugSuperVisible(new EDGFileVertexIterator(f, u, u+1,
				// 0, 50).next().ram.adj);
			}
		}

	}
}
