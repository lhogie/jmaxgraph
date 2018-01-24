package jmg.exp.nathann;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import jmg.Digraph;
import jmg.io.TinyReader;

public class UnitTests
{

	@Test
	public void testK23Plus3Arcs() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3, 4, 5);
		t.addLine(1, 3, 4, 5, 6, 7);
		Digraph g = t.toGraph();

		CountK22_Result r = CountK22.count(g);
		assertEquals(3, r.nk22);
		assertEquals(21, r.nbK22pot);
	}

	@Test
	public void testK22() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3);
		t.addLine(1, 2, 3);
		Digraph g = t.toGraph();

		CountK22_Result r = CountK22.count(g);
		assertEquals(1, r.nk22);
		assertEquals(4, r.nbK22pot);
	}

	@Test
	public void testK33() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 3, 4, 5, 6);
		t.addLine(1, 4, 5, 6);
		t.addLine(2, 4, 5, 6, 7, 8);
		Digraph g = t.toGraph();

		CountK22_Result r = CountK22.count(g);
		assertEquals(9, r.nk22);
		assertEquals(54, r.nbK22pot);
	}

}
