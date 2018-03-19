package jmg.exp.nathann;

import java.util.Iterator;

import it.unimi.dsi.fastutil.longs.LongArrays;
import jmg.Digraph;
import jmr.FSMapReduce;
import jmr.GlobalOutput;
import toools.Longs;
import toools.io.file.Directory;
import toools.io.file.nbs.NBSFile;

public class GlobalCount extends GlobalOutput<CountK22WorkerOutput>
{
	static final long serialVersionUID = 136049;
	private transient final Digraph g;
	private transient final FSMapReduce<CountK22WorkerOutput> mr;

	public long nbVertices;
	public long nbK22s_times2 = 0;
	public long nbK22sPot;
	public long nbTransitiveTriangles;
	public long nbCyclicTriangles_times3;
	public long nbTrianglesPot;
	public long[] nbK22sPerVertex_times2;
	public long[] nbK22sPotPerVertex;
	public long[] nbTrianglesPerVertex;
	public long[] nbTrianglesPotPerVertex;

	public GlobalCount(Digraph g, FSMapReduce<CountK22WorkerOutput> mr, int nbThreads)
	{
		this.g = g;
		this.mr = mr;

		Iterator<CountK22WorkerOutput> i = mr.resultIterator();

		while (i.hasNext())
		{
			CountK22WorkerOutput l = i.next();
			merge(l, nbThreads);
		}
	}

	private void merge(CountK22WorkerOutput o, int nbThreads)
	{
		super.merge(o);
		this.nbK22s_times2 += o.nbK22s_times2;
		this.nbK22sPot += o.nbK22sPot;
		this.nbTransitiveTriangles += o.nbTriangles;
		this.nbCyclicTriangles_times3 += o.nbCyclicTriangles_times3;
		this.nbTrianglesPot += o.nbTrianglesPot;
		this.nbVertices += o.endVertex - o.startVertex;
	}

	

	public void save()
	{
		save(mr.baseDirectory);
	}

	@Override
	public void save(Directory d)
	{
		if (nbK22sPerVertex_times2 != null)
		{
			long[] l = LongArrays.copy(nbK22sPerVertex_times2);
			Longs.apply(l, e -> e / 2);
			new NBSFile(d, "nbK22sPerVertex").saveValues(nbK22sPerVertex_times2);
		}

		if (nbK22sPotPerVertex != null)
			new NBSFile(d, "nbK22sPotPerVertex").saveValues(nbK22sPotPerVertex);

		if (nbTrianglesPerVertex != null)
			new NBSFile(d, "nbTrianglesPerVertex").saveValues(nbTrianglesPerVertex);

		if (nbTrianglesPotPerVertex != null)
			new NBSFile(d, "nbTrianglesPotPerVertex").saveValues(nbTrianglesPotPerVertex);
	}



	public GlobalCount(LocalCount r)
	{
		this.g = null;
		this.mr = null;
		nbVertices = r.endVertex - r.startVertex;
		nbK22s_times2 = r.nbK22s_times2;
		nbK22sPot = r.nbK22sPot;
		nbTransitiveTriangles = r.nbTransitiveTriangles;
		nbCyclicTriangles_times3 = r.nbCyclicTriangles_times3;
		nbTrianglesPot = r.nbTrianglesPot;
	}

	@Override
	public String toString()
	{
		String s = super.toString();
		s += '\n';
		s += "\n - nbK22=" + nk22();
		s += "\n - nbK22pot=" + nbK22sPot;
		s += "\n - CK=" + (4d * nk22() / nbK22sPot);
		s += "\n - nbTransitiveTriangles=" + nbTransitiveTriangles;
		s += "\n - nbCyclicTriangles_times3=" + nbCyclicTriangles_times3 / 3;
		s += "\n - nbTransitiveTrianglesPot=" + nbTrianglesPot;
		return s;
	}

	public long nk22()
	{
		return nbK22s_times2 / 2;
	}
}