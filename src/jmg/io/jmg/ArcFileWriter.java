package jmg.io.jmg;

import java.io.IOException;
import java.io.OutputStream;

import toools.io.Bits;
import toools.io.IORuntimeException;
import toools.io.Utilities;

public class ArcFileWriter
{
	final ArcFile f;
	final OutputStream arcOs;
	final long[] index;
	int u = 0;
	final byte[] b = new byte[8];
	// tracks the index for each entry
	long pos = 0;

	public ArcFileWriter(ArcFile f, int nbEntries) 
	{
		this.f = f;
		index = new long[nbEntries];
		arcOs = f.createWritingStream(false, 256 * 256 * 206);
	}

	static byte[] buf = new byte[8];

	public void writeADJ(int[] neighbors) throws IOException
	{
		writeADJ(neighbors, 0, neighbors.length);
	}

	public void writeADJ(int[] neighbors, int start, int len) throws IOException
	{
		index[u++] = pos;

		if (len > 0)
		{
			// writes the first neighbor
			Bits.putLong(buf, 0, neighbors[start]);
			int previous = neighbors[start];
			arcOs.write(buf, 0, 8);
			pos += 8;

			if (neighbors.length > 1)
			{
				// write the encoding for the other neighbors
				int maxGap = ArcFile.maxgap(neighbors, start, len);
				int encoding = Utilities.getNbBytesRequireToEncode(maxGap);
				Bits.putLong(buf, 0, encoding, 1);
				arcOs.write(buf, 0, 1);
				pos += 1;

				// write other neighbors
				for (int i = 1; i < len; ++i)
				{
					int v = neighbors[i + start];
					int delta = v - previous;
					Bits.putLong(buf, 0, delta, encoding);
					arcOs.write(buf, 0, encoding);
					pos += encoding;
					previous = v;
				}
			}
		}
	}

	public void close()
	{
		if (u != index.length)
			throw new IllegalStateException(u + " != " + index.length);

		try
		{
			arcOs.close();
			f.getIndexFile().saveValues(index);
		}
		catch (IOException e)
		{
			throw new IORuntimeException(e);
		}
	}

}
