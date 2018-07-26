package jmg.io.adj;

import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import jmg.Graph;
import toools.io.Cout;
import toools.io.file.RegularFile;

public class Demo
{
	public static void main(String[] args) throws IOException
	{
		TextADJFastReader r = new TextADJFastReader();
		Int2ObjectMap<int[]> adj = r.readFile(new RegularFile("demo.adj"));
		Graph g = new Graph();
		g.out.mem.from(adj, true, true, null, 1);
		Cout.result(g);
	}
}
