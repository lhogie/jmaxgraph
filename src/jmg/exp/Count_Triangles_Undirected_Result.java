package jmg.exp;

class Count_Triangles_Undirected_Result
{
	long nbTriangles;
	long nbPotentialTrianglesComputed, nbPotentialTrianglesIncremented;

	@Override
	public String toString()
	{
		return " - nbTriangles=" + nbTriangles + "\n - nbPotentialTrianglesComputed="
				+ nbPotentialTrianglesComputed + "\n - nbPotentialTrianglesIncremented="
				+ nbPotentialTrianglesIncremented;
	}
}