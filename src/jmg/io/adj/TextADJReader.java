package jmg.io.adj;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import j4u.chain.PluginConfig;
import toools.io.Utilities;
import toools.io.file.RegularFile;

public abstract class TextADJReader extends ADJReader
{
	protected boolean hasNbVertices;

	protected static class Section
	{
		long fromPosition;
		long endVertex = - 1;

		@Override
		public String toString()
		{
			return "Section [fromPosition=" + fromPosition + ", toNumber=" + endVertex
					+ "[";
		}
	}

	protected Section[] findSection(RegularFile f, int nbSection) throws IOException
	{
		final long sectionLength = f.getSize() / nbSection;
		Section[] sectionBounds = new Section[nbSection];

		for (int i = 0; i < nbSection; ++i)
		{
			sectionBounds[i] = new Section();
			sectionBounds[i].fromPosition = i * sectionLength;
		}

		for (int sectionIndex = 0; sectionIndex < nbSection - 1; ++sectionIndex)
		{
			InputStream is = f.createReadingStream();
			Section section = sectionBounds[sectionIndex];

			if (is.skip(section.fromPosition + sectionLength) != section.fromPosition
					+ sectionLength)
				throw new IllegalStateException();

			int skip = Utilities.skipUntilEndOfLine(is);

			// if we're not building the last section bounds
			if (sectionIndex < nbSection - 1)
			{
				section.endVertex = new Scanner(is).nextLong();
				sectionBounds[sectionIndex + 1].fromPosition = section.fromPosition
						+ sectionLength + skip;
			}

			is.close();
		}

		return sectionBounds;
	}

	@Override
	public void setup(PluginConfig parms)
	{
		super.setup(parms);
		hasNbVertices = parms.getBoolean("hasNbVertices");
	}

}
