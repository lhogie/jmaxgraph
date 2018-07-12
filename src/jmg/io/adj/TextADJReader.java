package jmg.io.adj;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import j4u.chain.PluginParms;
import toools.io.Utilities;
import toools.io.Utilities.ReadLong;
import toools.io.file.RegularFile;

public abstract class TextADJReader extends ADJReader
{
	protected boolean hasNbVertices;

	protected static class Section
	{
		final long fromPosition;
		long endVertex = - 1;

		public Section(long pos)
		{
			fromPosition = pos;
		}

		@Override
		public String toString()
		{
			return "Section [fromPosition=" + fromPosition + ", toNumber=" + endVertex
					+ "[";
		}
	}

	protected Section[] findSections(RegularFile f, int nbSections) throws IOException
	{
		long len = f.getSize();
		final long sectionLength = f.getSize() / nbSections;
		List<Section> sectionBounds = new ArrayList<>();
		InputStream is = f.createReadingStream();
		long pos = 0;
		long nextSectionPos = 0;

		while (true)
		{
			Section s = new Section(nextSectionPos);
			sectionBounds.add(s);

			Utilities.skip(is, sectionLength);
			pos += sectionLength;

			pos += Utilities.skipUntilEndOfLine(is);

			// if EOF
			if (pos >= len)
			{
				return sectionBounds.toArray(new Section[0]);
			}
			else
			{
				nextSectionPos = pos;
				ReadLong endVertex = Utilities.readLong(is);
				s.endVertex = endVertex.v;
				pos += endVertex.n;
			}
		}
	}

	@Override
	public void setParameters(PluginParms parms)
	{
		super.setParameters(parms);
		hasNbVertices = parms.getBoolean("hasNbVertices");
	}

}
