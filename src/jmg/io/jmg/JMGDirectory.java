package jmg.io.jmg;

import toools.io.file.Directory;
import toools.io.file.nbs.NBSFile;
import toools.text.TextUtilities;

public class JMGDirectory extends Directory
{

	private final NBSFile label2vertexFile;

	public JMGDirectory(String path)
	{
		super(path);
		this.label2vertexFile = new NBSFile(this, "label2vertex.nbs");
	}
	
	public NBSFile getLabel2VertexFile()
	{
		return label2vertexFile;
	}


	@Override
	public String toString()
	{
		String s = "JMG Dataset " + getPath();

		if (exists())
			s += " is " + TextUtilities.toHumanString(getSize()) + "B";

		return s;
	}


}
