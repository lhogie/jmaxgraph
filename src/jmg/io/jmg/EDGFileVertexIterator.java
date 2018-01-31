package jmg.io.jmg;

import java.io.IOException;
import java.util.Iterator;

import toools.io.BinaryReader;
import toools.io.Cout;
import toools.io.FileIterator;
import toools.io.IORuntimeException;
import toools.io.file.nbs.NBSFileIterator;

public class EDGFileVertexIterator
		extends FileIterator<EDGFileVertexIterator.EDGFileCursor>
		implements Iterator<EDGFileVertexIterator.EDGFileCursor>
{
	public class EDGFileCursor
	{
		public int vertex;
		public int[] adj;
		public long nbBytes;
	}

	private final BinaryReader reader;
	private final NBSFileIterator indexIterator;
	long currentPos;
	private int vertex;
	private final int endVertex;
	private final long finalPos;
	private final EDGFileCursor cursor = new EDGFileCursor();
	private final int[][] preallocatedArrays;
	private final long nbVertices;

	public EDGFileVertexIterator(ArcFile f, int startVertex, int endVertex,
			int nbPreallocatedArrays, int bufSize)
	{
		super(f);
		this.vertex = startVertex;
		this.endVertex = endVertex;

		IndexFile idxf = f.getIndexFile();
		nbVertices = f.getIndexFile().getNbValues();

		try
		{
			if (endVertex < nbVertices)
			{
				this.finalPos = idxf.readValue(endVertex);
			}
			else
			{
				this.finalPos = f.getSize();
			}

			this.indexIterator = new NBSFileIterator(f.getIndexFile(), startVertex);
			is.skip(currentPos = indexIterator.nextLong());
			this.reader = new BinaryReader(is, bufSize);
		}
		catch (IOException e)
		{
			throw new IORuntimeException(e);
		}

		this.preallocatedArrays = new int[nbPreallocatedArrays][];

		for (int i = 1; i < preallocatedArrays.length; ++i)
		{
			preallocatedArrays[i] = new int[i];
		}
	}

	public boolean hasNext()
	{
		return vertex < endVertex;
	}

	public EDGFileCursor next()
	{
		long entryEndPos = indexIterator.hasNext() ? indexIterator.nextLong() : finalPos;

		cursor.vertex = vertex;
		cursor.nbBytes = entryEndPos - currentPos;
		cursor.adj = ArcFile.readADJList(cursor.nbBytes, reader, preallocatedArrays);

		currentPos = entryEndPos;
		++vertex;
		return cursor;
	}

	@Override
	public void close()
	{
		super.close();
		indexIterator.close();
	}

}
