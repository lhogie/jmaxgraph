package jmg.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import toools.io.BinaryReader;
import toools.io.NBSFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.text.TextUtilities;

public class JMGDataset
{
	public final Directory directory;
	public final int nbVertex;
	public final NBSFile label2vertexFile;
	public final RegularFile ADJFile;
	public final RegularFile propertyFile;
	public final Properties properties = new Properties();

	public JMGDataset(Directory directory) throws IOException
	{
		this.directory = directory;
		this.propertyFile = new RegularFile(directory, "properties.txt");
		this.ADJFile = getADJFile(directory);
		this.label2vertexFile = getLabel2VertexFile(directory);
		properties.load(propertyFile.createReadingStream());
		this.nbVertex = getNbVertex();

		if ( ! properties.containsKey("nbVertices"))
			throw new IllegalArgumentException(
					"properties does not specify the number of vertices");

		if ( ! properties.containsKey("adjType")
				|| ! properties.getProperty("adjType").matches("in|out"))
			throw new IllegalArgumentException(
					"properties does not specify the type of ADJ (in or out)");
	}

	public int getNbVertex() throws IOException
	{
		return Integer.valueOf(properties.getProperty("nbVertices"));
	}

	public int[] readOuts(int v) throws IOException
	{
		InputStream is = getADJFile(directory).createReadingStream();

		long index = getIndexFile(directory).readValue(v);
		is.skip(index);

		int degree = (int) getDegreesFile(directory).readValue(v);

		int[] out = new int[degree];

		BinaryReader r = new BinaryReader(is, 65536 * 256);
		out[0] = (int) r.next(8);
		int previous = out[0];
		int encoding = (int) r.next(1);

		for (int i = 1; i < degree; ++i)
		{
			int delta = (int) r.next(encoding);
			int current = previous + delta;
			out[i] = current;
			previous = current;
		}

		is.close();
		return out;
	}

	public static RegularFile getADJFile(Directory d)
	{
		return new RegularFile(d, "adj.edg");
	}

	public static NBSFile getIndexFile(Directory d)
	{
		return new NBSFile(d, "index.nbs");
	}

	public static NBSFile getDegreesFile(Directory d)
	{
		return new NBSFile(d, "degrees.nbs");
	}

	public static NBSFile getLabel2VertexFile(Directory d)
	{
		return new NBSFile(d, "label2vertex.nbs");
	}

	public static RegularFile getPropertyFile(Directory d)
	{
		return new RegularFile(d, "properties.txt");
	}

	@Override
	public String toString()
	{
		return "JMG Dataset " + directory + " is "
				+ TextUtilities.toHumanString(directory.getSize()) + "B";
	}

	public Properties getProperties()
	{
		return properties;
	}

}
