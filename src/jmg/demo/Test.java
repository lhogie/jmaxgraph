package jmg.demo;

import java.io.IOException;

import jmg.Graph;
import jmg.io.TinyReader;
import jmg.io.jmg.JMGDirectory;

public class Test
{
	public static void main(String[] args) throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3);
		t.addLine(1, 2, 3);
		JMGDirectory d = new JMGDirectory("$HOME/datasets/k22.jmg");
		Graph g = new Graph(d);
		t.toGraph(g);
		g.writeToDisk();
	}
}
