package jmg.exp.thibaud;

import java.io.IOException;

import jmg.Digraph;
import jmg.io.jmg.JMGDirectory;
import toools.io.Cout;

public class CountTrianglesOnEachSample
{

	public static void main(String[] args) throws IOException
	{
		for (char c : "abc".toCharArray())
		{
			JMGDirectory d = new JMGDirectory("$HOME/datasets/sample-0.01" + c + ".jmg");
			Cout.result("Reading " + d);
			Digraph g = d.readDirectory(8, false);

			Cout.result("CountK2_2_Thibaud on directed graph");
			Cout.result(CountK22.count(g));

			Cout.result("Count_Triangles on directed graph");
			Cout.result(CountTriangles.count(g));

			Cout.result("Count_Triangles on UNdirected graph");
			Cout.result(Count_Triangles_Undirected.count(g));
		}
	}
}
