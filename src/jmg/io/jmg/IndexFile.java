package jmg.io.jmg;

import java.io.IOException;

import toools.io.file.Directory;
import toools.io.file.nbs.NBSFile;

public class IndexFile extends NBSFile
{
	public static class Bounds
	{
		long start, end;

		@Override
		public String toString()
		{
			return (end - start) + " bytes in [" + start + " " + end + "[";
		}

		public long length()
		{
			return end - start;
		}

	}

	private final ArcFile arcFile;

	public IndexFile(Directory parent, String name, ArcFile arcFile)
	{
		super(parent, name);
		this.arcFile = arcFile;
	}

	public Bounds readBounds(int u) throws IOException
	{
		if (u < 0)
		{
			throw new IllegalArgumentException("out of range: " + u);
		}
		else if (u < getNbValues() - 1)
		{
			long[] r = readConsecutiveValues(u, 2);
			Bounds b = new Bounds();
			b.start = r[0];
			b.end = r[1];
			return b;
		}
		else if (u == getNbValues() - 1)
		{
			long[] r = readConsecutiveValues(u, 1);
			Bounds b = new Bounds();
			b.start = r[0];
			b.end = arcFile.getSize();
			return b;
		}
		else
		{
			throw new IllegalArgumentException("out of range: " + u);
		}
	}

}
