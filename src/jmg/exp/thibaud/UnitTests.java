package jmg.exp.thibaud;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import jmg.Graph;
import jmg.io.TinyReader;
import toools.io.Cout;
import toools.text.TextUtilities;

public class UnitTests
{

	@Test
	public void testK23Plus3Arcs() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3, 4, 5);
		t.addLine(1, 3, 4, 5, 6, 7);
		Graph g = t.toGraph();
		g.in.ensureLoaded(8);

		CountK22_Result r = new CountK22().count(g);
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

		CountK22_Result r = new CountK22().count(g);
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

		CountK22_Result r = new CountK22().count(g);
		assertEquals(9, r.nbK22);
		assertEquals(54, r.nbK22pot);
	}

	@Test
	public void testBidirectionalTriangle() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 1, 2);
		t.addLine(1, 0, 2);
		t.addLine(2, 0, 1);
		Graph g = t.toGraph();

		CountTriangles_Result r = CountTriangles.count(g, 1);
		assertEquals(6, r.nbTransitiveTriangles);
		assertEquals(2, r.threeTimesNbCyclicTriangles / 3);
		assertEquals(6, r.nbPotentialTriangles_incremented);
		assertEquals(6, r.nbPotentialTriangles_computed);
	}

	@Test
	public void testBidirectionalTrianglePlusOneArc() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 1, 2);
		t.addLine(1, 0, 2);
		t.addLine(2, 0, 1, 3);
		Graph g = t.toGraph();

		CountTriangles_Result r = CountTriangles.count(g, 1);
		assertEquals(6, r.nbTransitiveTriangles);
		assertEquals(2, r.threeTimesNbCyclicTriangles / 3);
		assertEquals(8, r.nbPotentialTriangles_incremented);
		assertEquals(8, r.nbPotentialTriangles_computed);
	}

	@Test
	public void testCyclicTriangle() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 1);
		t.addLine(1, 2);
		t.addLine(2, 0);
		Graph g = t.toGraph();

		CountTriangles_Result r = CountTriangles.count(g, 1);
		assertEquals(0, r.nbTransitiveTriangles);
		assertEquals(1, r.threeTimesNbCyclicTriangles / 3);
		assertEquals(3, r.nbPotentialTriangles_computed);
		assertEquals(3, r.nbPotentialTriangles_incremented);
	}

	@Test
	public void testK22Triangle() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3);
		t.addLine(1, 2, 3, 0);
		Graph g = t.toGraph();

		CountTriangles_Result r = CountTriangles.count(g, 1);
		assertEquals(2, r.nbTransitiveTriangles);
		assertEquals(0, r.threeTimesNbCyclicTriangles / 3);
		assertEquals(2, r.nbPotentialTriangles_computed);
		assertEquals(2, r.nbPotentialTriangles_incremented);
	}

	@Test
	public void testK23Plus3arcsTriangle() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3, 4, 5);
		t.addLine(1, 3, 4, 5, 6, 7, 0);
		Graph g = t.toGraph();

		CountTriangles_Result r = CountTriangles.count(g, 1);
		assertEquals(3, r.nbTransitiveTriangles);
		assertEquals(0, r.threeTimesNbCyclicTriangles / 3);
		assertEquals(4, r.nbPotentialTriangles_computed);
		assertEquals(4, r.nbPotentialTriangles_incremented);
	}

	@Test
	public void testK22TriangleUndirected() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 2, 3);
		t.addLine(1, 2, 3);
		Graph g = t.toGraph();

		CountTriangles_Undirected_Result r = Count_Triangles_Undirected.count(g, 1);
		assertEquals(0, r.nbTriangles);
		assertEquals(2, r.nbPotentialTrianglesComputed);
		assertEquals(2, r.nbPotentialTrianglesIncremented);
	}

	@Test
	public void testK22TriangleUndirectedSimple() throws IOException
	{
		TinyReader.Text t = new TinyReader.Text();
		t.addLine(0, 1);
		t.addLine(1, 2);
		t.addLine(2, 0, 3);
		Graph g = t.toGraph();
		CountTriangles_Undirected_Result r = Count_Triangles_Undirected.count(g, 1);
		Cout.debug(TextUtilities.box(r.toString()));
		assertEquals(1, r.nbTriangles);
		// assertEquals(5, r.nbPotentialTrianglesIncremented);
		assertEquals(5, r.nbPotentialTrianglesComputed);
	}

}
