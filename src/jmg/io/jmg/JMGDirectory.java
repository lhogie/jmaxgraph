package jmg.io.jmg;

import java.io.IOException;
import java.util.Properties;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import jmg.Digraph;
import jmg.Vertex2LabelMap;
import toools.io.IORuntimeException;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.io.file.nbs.NBSFile;
import toools.text.TextUtilities;
import toools.util.Conversion;

public class JMGDirectory extends Directory
{
	static
	{
		RegularFile.extension_class.put("nbs", NBSFile.class);
		RegularFile.extension_class.put("arc", ArcFile.class);
	}

	public int nbVertex;
	public final NBSFile label2vertexFile;
	public final ArcFile outFile, inFile;

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

	public ArcFile getOutFile()
	{
		return new ArcFile(this, "out.arc");
	}

	public ArcFile getInFile()
	{
		return new ArcFile(this, "in.arc");
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
		String s = "JMG Dataset " + getPath();

		if (exists())
			s += " is " + TextUtilities.toHumanString(getSize()) + "B";

		return s;
	}

	public Properties getProperties()
	{
		return properties;
	}

	public Digraph mapGraph(int nbThreads, boolean useLabels)
	{
		if ( ! exists())
			throw new IllegalStateException(getPath() + " does not exist");

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

		return g;
	}
}
