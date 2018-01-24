package jmg.io.jmg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import jmg.Utils;
import toools.io.BinaryReader;
import toools.io.IORuntimeException;
import toools.util.Conversion;

public class EDGFileVertexIterator implements Iterator<EDGFileVertexIterator.EDGFileCursor>
{
	public static class EDGFileCursor
	{
		public int iteration = -1;
		public int vertex;
		public int[] adj;
		public long nbBytes;
	}

	private final BinaryReader reader;
	private final long[] index;
	private int vertex;
	private final int endVertex;
	private long pos;
	private final long finalPos;
	private final EDGFileCursor cursor = new EDGFileCursor();
	private final int[][] preallocatedArrays;

	long nbBytesRead = 0;

	
	public EDGFileVertexIterator(EDGFile f, int startVertex, int endVertex,
			int nbPreallocatedArrays, int bufSize)
	{
		this.index = f.getIndex(8);
		this.vertex = startVertex;
		this.pos = index[startVertex];
		this.endVertex = endVertex;
		this.finalPos = endVertex < index.length ? index[endVertex] : f.getSize();

		InputStream is = f.createReadingStream();

		try
		{
			is.skip(pos);
		}
		catch (IOException e)
		{
			throw new IORuntimeException(e);
		}

		reader = new BinaryReader(is, bufSize);

		preallocatedArrays = new int[nbPreallocatedArrays][];

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
		long entryEndPos = vertex + 1 < index.length ? index[vertex + 1] : finalPos;
		cursor.nbBytes = entryEndPos - index[vertex];

		nbBytesRead += cursor.nbBytes;

		cursor.vertex = vertex;
		++cursor.iteration;
		vertex++;
		pos = entryEndPos;

		// if it has no neighbors
		if (cursor.nbBytes == 0)
		{
			cursor.adj = Utils.emptyArray;
			return cursor;
		}
		else
		{
			int firstNeighbor = Conversion.long2int(reader.next(8));

			// if it has only one neighbor
			if (cursor.nbBytes == 8)
			{
				cursor.adj = 1 < preallocatedArrays.length ? preallocatedArrays[1]
						: new int[1];
				cursor.adj[0] = firstNeighbor;
				return cursor;
			}
			else
			{
				int encoding = (int) reader.next(1);
				int nbNeighbor = 1 + Conversion.long2int((cursor.nbBytes - 9) / encoding);

				int[] neighbors = nbNeighbor < preallocatedArrays.length
						? preallocatedArrays[nbNeighbor]
						: new int[nbNeighbor];

				neighbors[0] = firstNeighbor;
				int previous = firstNeighbor;

				for (int i = 1; i < nbNeighbor; ++i)
				{
					long delta = reader.next(encoding);
					long neighbor = previous + delta;
					previous = neighbors[i] = Conversion.long2int(neighbor);
				}

				cursor.adj = neighbors;
				return cursor;
			}
		}
	}

}
