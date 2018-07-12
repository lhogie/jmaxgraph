package jmg.exp.thibaud;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import jmg.Graph;
import jmg.io.TinyReader;

public class UnitTests_streaming
{

	@Test
	public void testK23Plus3Arcs() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3, 4, 5);
		t.addLine(1, 3, 4, 5, 6, 7);
		Graph g = t.toGraph();
		g.in.ensureLoaded(8);

		CountK22v2_Result r = new CountK22_streaming().count(g);
		assertEquals(3, r.nbK22);
		assertEquals(21, r.nbK22pot);

	
	}

	@Test
	public void testK22() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3);
		t.addLine(1, 2, 3);
		Graph g = t.toGraph();

		CountK22v2_Result r = new CountK22_streaming().count(g);
		assertEquals(1, r.nbK22);
		assertEquals(4, r.nbK22pot);
	}

	@Test
	public void testK33() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 3, 4, 5, 6);
		t.addLine(1, 4, 5, 6);
		t.addLine(2, 4, 5, 6, 7, 8);
		Graph g = t.toGraph();

		CountK22v2_Result r = new CountK22_streaming().count(g);
		assertEquals(9, r.nbK22);
		assertEquals(54, r.nbK22pot);
	}


}
