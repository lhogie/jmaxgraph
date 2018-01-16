package jmg.exp;

import java.io.IOException;

import jmg.Digraph;
import jmg.io.JMGReader;
import toools.io.Cout;
import toools.io.file.Directory;

public class CountTrianglesOnEachSample
{

	public static void main(String[] args) throws IOException
	{
		for (char c : "abc".toCharArray())
		{
			Directory d = new Directory("$HOME/datasets/sample-0.001" + c + ".jmg");
			Cout.result("Reading " + d);
			Digraph g = JMGReader.readDirectory(d, 8, false);

			Cout.result("CountK2_2_Thibaud on directed graph");
			Cout.result(new CountK2_2_Thibaud().process(g));

			Cout.result("Count_Triangles on directed graph");
			Cout.result(new Count_Triangles().process(g));
			
			Cout.result("Count_Triangles on UNdirected graph");
			Cout.result(new Count_Triangles_Undirected().process(g));
		}
	}
}
