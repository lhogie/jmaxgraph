package jmg.exp.thibaud;

class Count_Triangles_Undirected_Result
{
	long nbTriangles;
	long nbPotentialTrianglesComputed, nbPotentialTrianglesIncremented;

	@Override
	public String toString()
	{
		String s = "";
		s += " - nbTriangles=" + nbTriangles;
		s += "\n - nbPotentialTrianglesComputed=" + nbPotentialTrianglesComputed;
		
		// this one is wrong
		s += "\n - nbPotentialTrianglesIncremented=" + nbPotentialTrianglesIncremented;
		
		
		s += "\n - CCundirected=" + (3 * nbTriangles / (double) nbPotentialTrianglesComputed);
		return s;
	}
}