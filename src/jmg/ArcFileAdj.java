package jmg;

import java.util.Iterator;

import jmg.io.jmg.ArcFile;
import toools.io.IORuntimeException;

public class ArcFileAdj extends Adjacency
{

	private final ArcFile file;

	public ArcFileAdj(ArcFile f, int nbThreads)
	{
		super(f == null ? null : f.getParent(), nbThreads);
		file = f;
	}

	@Override
	public String toString()
	{
		return "ADJ stored in" + file;
	}

	
	@Override
	public int[] get(int u)
	{
		return file.readEntry(u).adj;
	}

	@Override
	public void setAllFrom(Adjacency adj, int nbThreads) throws IORuntimeException
	{
		file.writeADJ(adj);
	}

	@Override
	public int[] computeDegrees(int nbThreads)
	{
		return file.getDegrees(nbThreads);
	}

	@Override
	public boolean isDefined()
	{
		return file != null && file.exists();
	}

	@Override
	public int countVertices(int nbThreads)
	{
		return file.getNbEntries();
	}

	@Override
	public Iterator<VertexCursor> iterator(int from, int to)
	{
		return iterator(from, to, 0, 160000000);
	}

	public Iterator<VertexCursor> iterator(int from, int to, int nbPreallocatedArrays,
			int bufSize)
	{
		return (Iterator<VertexCursor>) (Object) file.iterator(from, to,
				nbPreallocatedArrays, bufSize);
	}
/*
	public void setFile(ArcFile f)
	{
		this.file = f;
	}
	*/

	public ArcFile getArcFile()
	{
		return file;
	}
}
