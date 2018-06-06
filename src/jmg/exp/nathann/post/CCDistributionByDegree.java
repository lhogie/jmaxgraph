package jmg.exp.nathann.post;

import java.io.OutputStream;
import java.io.PrintStream;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import toools.io.Cout;
import toools.io.file.RegularFile;
import toools.io.file.nbs.NBSFile;
import toools.progression.LongProcess;
import toools.text.CSV;

public class CCDistributionByDegree
{
	public static void main(String[] args)
	{
		double[] CCs = CC.computeCC();
		long[] in_degrees = new NBSFile("in_degrees.nbs").readValues(1);

		class DegreeStats
		{
			int nbOccurences;
			double ccSum;
		}

		Long2ObjectMap<DegreeStats> distribution = new Long2ObjectAVLTreeMap<>();

		LongProcess computing = new LongProcess("computing", " number", CCs.length);

		int nbDegrees = 0;
		double sum = 0;
		
		for (int v = 0; v < CCs.length; ++v)
		{
			long inDegree = in_degrees[v];
			DegreeStats statForThisDegree = distribution.getOrDefault(inDegree, null);

			if (statForThisDegree == null)
			{
				distribution.put(inDegree, statForThisDegree = new DegreeStats());
			}

			++nbDegrees;
			
			if (!Double.isNaN(CCs[v]))
			{
				statForThisDegree.ccSum += CCs[v];
				++statForThisDegree.nbOccurences;
			}

			computing.sensor.progressStatus++;
		}

		computing.end();

		RegularFile out = new RegularFile("cc_distribution_by_degree.txt");
		Cout.progress("writing " + out);
		OutputStream os = out.createWritingStream();
		PrintStream pos = new PrintStream(os);
		CSV.print(distribution.keySet().iterator(), d -> {
			DegreeStats dStat = distribution.get(d);
			return dStat.ccSum / dStat.nbOccurences;
		}, pos);
		pos.close();
	}
}
