package jmg.exp.nathann;

import jmr.LocalOutput;
import toools.io.file.Directory;
import toools.io.file.nbs.NBSFile;

public class CountK22WorkerOutput extends LocalOutput
{
	final static long serialVersionUID = 76543;

	public int startVertex, endVertex;
	public long nbK22s_times2 = 0;
	public long nbK22sPot;
	public long nbTriangles;
	public long nbCyclicTriangles_times3;
	public long nbTrianglesPot;
	public String nbK22sFilename;
	public String nbK22sPotFilename;
	public String nbTrianglesFilename;
	public String nbTrianglesPotFilename;

	public CountK22WorkerOutput(LocalCount r, Directory auxDir, String name)
	{
		startVertex = r.startVertex;
		endVertex = r.endVertex;
		nbK22s_times2 = r.nbK22s_times2;
		nbK22sPot = r.nbK22sPot;
		nbTriangles = r.nbTransitiveTriangles;
		nbCyclicTriangles_times3 = r.nbCyclicTriangles_times3;
		nbTrianglesPot = r.nbTrianglesPot;

		if (r.nbK22sPerVertex_times2 != null)
		{
			for (int i = 0; i < r.nbK22sPerVertex_times2.length; ++i)
			{
				r.nbK22sPerVertex_times2[i] /= 2;
			}

			new NBSFile(auxDir, nbK22sFilename = name + "_nbK22s")
					.saveValues(r.nbK22sPerVertex_times2);
		}

		if (r.nbK22sPotPerVertex != null)
			new NBSFile(auxDir, nbK22sPotFilename = name + "_nbK22sPot")
					.saveValues(r.nbK22sPotPerVertex);

		if (r.nbTrianglesPerVertex != null)
			new NBSFile(auxDir, nbTrianglesFilename = name + "_nbTriangles")
					.saveValues(r.nbTrianglesPerVertex);

		if (r.nbTrianglesPotPerVertex != null)
			new NBSFile(auxDir, nbTrianglesPotFilename = name + "_nbTrianglesPot")
					.saveValues(r.nbTrianglesPotPerVertex);
	}

	@Override
	public String toString()
	{
		String s = "";
		s += " - from " + startVertex + " to " + endVertex;
		s += "\n - nbK22=" + nk22();
		s += "\n - nbK22pot=" + nbK22sPot;
		s += "\n - CK=" + (4d * nk22() / nbK22sPot);
		s += "\n - nbTransitiveTriangles=" + nbTriangles;
		s += "\n - nbCyclicTriangles=" + nbCyclicTriangles_times3 / 3;
		s += "\n - nbTransitiveTrianglesPot=" + nbTrianglesPot;
		return s;
	}

	public long nk22()
	{
		return nbK22s_times2 / 2;
	}

}