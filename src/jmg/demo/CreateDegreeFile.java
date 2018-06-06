package jmg.demo;

import java.io.IOException;
import java.util.Iterator;

import jmg.io.jmg.ArcFile;
import jmg.io.jmg.ArcFileVertexIterator.ArcFileCursor;
import toools.SystemMonitor;
import toools.io.Cout;
import toools.progression.LongProcess;

public class CreateDegreeFile
{
	public static void main(String[] args) throws IOException
	{
		SystemMonitor.defaultMonitor.start();

		ArcFile arcFile = new ArcFile(args[0]);

		if (!arcFile.getDegreeFile().exists())
		{
			gd(arcFile);
		}
		else
		{
			Cout.info("Nothing to do");
		}
	}

	public static void gd(ArcFile arcFile)
	{
		int nbVertices = arcFile.getNbEntries();
		LongProcess generatinDegrees = new LongProcess(
				"generating degrees for " + arcFile, " vertex", nbVertices);
		Iterator<ArcFileCursor> i = arcFile.iterator();
		int degrees[] = new int[nbVertices];

		while (i.hasNext())
		{
			generatinDegrees.sensor.progressStatus++;
			ArcFileCursor n = i.next();
			degrees[n.vertex] = n.adj.length;
		}

		generatinDegrees.end();
		arcFile.getDegreeFile().saveValues(degrees);
	}

}
