package jmg.io.jmg;

import java.io.IOException;
import java.util.Properties;

import jmg.Graph;
import jmg.exp.nathann.JSONMap;
import jmg.exp.nathann.JSONable;
import toools.io.IORuntimeException;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.io.file.nbs.NBSFile;
import toools.io.serialization.JavaSerializer;
import toools.text.TextUtilities;

public class JMGDirectory extends Directory implements JSONable
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

	public Graph mapGraph(int nbThreads, boolean useLabels)
	{
		return new Graph(this, useLabels, nbThreads);
	}

	@Override
	public JSONMap toJSONElement()
	{
		JSONMap m = new JSONMap();

		for (RegularFile f : getChildRegularFiles())
		{
			if (f.getName().endsWith(".json"))
			{
				m.add(f.getName(), new String(f.getContent()));
			}
			else if (f.getName().endsWith(".ser"))
			{
				Object o = JavaSerializer.getDefaultSerializer()
						.fromBytes(f.getContent());

				if (o instanceof JSONable)
				{
					m.add(f.getName(), ((JSONable) o).toJSONElement());
				}
			}
		}

		return m;
	}

	public void createSummaryJSONFile()
	{
		RegularFile f = new RegularFile(this, "summary.json");
		f.setContent(toJSONElement().toString().getBytes());
	}

}
