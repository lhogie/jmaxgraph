package jmg.exp.nathann;

import static org.junit.Assert.assertEquals;

import jmg.Digraph;
import jmg.io.TinyReader;

public class Demo
{

	public static void main(String[] args)
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3, 4, 5);
		t.addLine(1, 3, 4, 5, 6, 7);
		Digraph g = t.toGraph();

		CountK22_Result r = new CountK22_streaming().count(g);
		assertEquals(3, r.nk22);
		assertEquals(21, r.nbK22pot);
	}
	
}