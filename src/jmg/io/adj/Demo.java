package jmg.io.adj;

import java.io.IOException;

import jmg.Graph;
import toools.io.Cout;
import toools.io.file.RegularFile;

public class Demo
{
	public static void main(String[] args) throws IOException
	{
		Graph g = new Graph();
		g.out.mem.from(new RegularFile("demo.adj"));
		Cout.result(g);
	}
}
