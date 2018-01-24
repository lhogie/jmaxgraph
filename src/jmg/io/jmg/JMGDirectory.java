package jmg.io.jmg;

import java.io.IOException;
import java.util.Properties;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import jmg.Digraph;
import jmg.Vertex2LabelMap;
import toools.io.IORuntimeException;
import toools.io.NBSFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;
import toools.text.TextUtilities;
import toools.util.Conversion;

public class JMGDirectory extends Directory
{
	static
	{
		RegularFile.extension_class.put("nbs", NBSFile.class);
		RegularFile.extension_class.put("edg", EDGFile.class);
	}

	public int nbVertex;
	public final NBSFile label2vertexFile;
	public final EDGFile outFile, inFile;

	public final RegularFile propertyFile;
	public final Properties properties = new Properties();

	public JMGDirectory(String path)
	{
		super(path);
		this.propertyFile = getChildRegularFile("properties.txt");
		this.outFile = getOutFile();
		this.inFile = getInFile();
		this.label2vertexFile = getLabel2VertexFile();

		if (propertyFile.exists())
		{
			try
			{
				properties.load(propertyFile.createReadingStream());
				this.nbVertex = Integer.valueOf(properties.getProperty("nbVertices"));
			}
			catch (IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
	}

	public int getNbVertex()
	{
		return nbVertex;
	}

	public EDGFile getOutFile()
	{
		return new EDGFile(this, "out.edg");
	}

	public EDGFile getInFile()
	{
		return new EDGFile(this, "in.edg");
	}

	public NBSFile getDegreesFile()
	{
		return new NBSFile(this, "degrees.nbs");
	}

	public NBSFile getLabel2VertexFile()
	{
		return new NBSFile(this, "label2vertex.nbs");
	}

	public RegularFile getPropertyFile()
	{
		return new RegularFile(this, "properties.txt");
	}

	@Override
	public String toString()
	{
		return "JMG Dataset " + getPath() + " is "
				+ TextUtilities.toHumanString(getSize()) + "B";
	}

	public Properties getProperties()
	{
		return properties;
	}

	public Digraph readDirectory(int nbThreads, boolean useLabels)
	{
		try
		{
			LongProcess loading = new LongProcess("loading graph", - 1);
			Digraph g = new Digraph();
			g.setDataset(this);
			g.nbVertices = getNbVertex();

			if (useLabels && label2vertexFile.exists())
			{
				g.labelling.label2vertex = Conversion
						.toIntArray(label2vertexFile.readValues(nbThreads));

				assert new IntOpenHashSet(g.labelling.label2vertex)
						.size() == g.labelling.label2vertex.length : new IntOpenHashSet(
								g.labelling.label2vertex).size() + " != "
								+ g.labelling.label2vertex.length;

				g.labelling.vertex2label = new Vertex2LabelMap(g.labelling.label2vertex);
			}

			loading.end();
			return g;
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}
}
