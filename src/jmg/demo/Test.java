package jmg.demo;

import java.io.IOException;

import jmg.Digraph;
import jmg.io.TinyReader;
import jmg.io.jmg.JMGDirectory;
import jmg.io.jmg.JMGWriter;

public class Test
{
	public static void main(String[] args) throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3);
		t.addLine(1, 2, 3);
		Digraph g = t.toGraph();
		
		JMGDirectory d = new JMGDirectory("$HOME/datasets/k22.jmg");
		g.write(d);
	}
}
