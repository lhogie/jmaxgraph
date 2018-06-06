package jmg.exp.nathann.post;

import java.io.OutputStream;
import java.io.PrintStream;

import it.unimi.dsi.fastutil.doubles.Double2LongAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2LongMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import toools.io.Cout;
import toools.io.file.RegularFile;
import toools.io.file.nbs.NBSFile;
import toools.progression.LongProcess;
import toools.text.CSV;

public class CC
{
	public static void main(String[] args)
	{
		double[] CCs = computeCC();

		ccDist(CCs);
		// System.gc();
		cumulativeCCDistribution(CCs);
	}

	private static void ccDist(double[] CCs)
	{
		LongProcess lp = new LongProcess("computing", " number", CCs.length);
		Double2LongMap distribution = new Double2LongAVLTreeMap();

		for (double cc : CCs)
		{
			if ( ! Double.isNaN(cc))
			{
				long n = distribution.getOrDefault(cc, 0);
				distribution.put(cc, n + 1);
			}

			lp.sensor.progressStatus++;
		}

		lp.end();
		Cout.result("number of +Infinity: " + distribution.get(Double.POSITIVE_INFINITY));
		Cout.result("number of NaN: " + distribution.get(Double.NaN));

		save(distribution, "cc_distribution.txt");
	}

	public static void cumulativeCCDistribution(double[] CCs)
	{
		LongProcess sorting = new LongProcess("sorting", null, - 1);
		DoubleArrays.parallelQuickSort(CCs);
		sorting.end();

		LongProcess lp = new LongProcess("computing cumulated", " number", CCs.length);
		Double2LongMap cumulatedDistribution = new Double2LongAVLTreeMap();

		long sum = 0;

		for (double cc : CCs)
		{
			if ( ! Double.isNaN(cc))
			{
				++sum;
				cumulatedDistribution.put(cc, sum);
			}

			++lp.sensor.progressStatus;
		}

		lp.end();
		save(cumulatedDistribution, "cc_distribution_cumulated.txt");
	}

	public static double[] computeCC()
	{
		long[] nbK22s = new NBSFile("nbK22s.nbs").readValues(1);
		long[] nbK22sPot = new NBSFile("nbK22sPot.nbs").readValues(1);

		double[] CCs = new double[nbK22s.length];

		Cout.progress("computing CCs for nb vertices: " + CCs.length);

		for (int i = 0; i < nbK22s.length; ++i)
		{
//			CCs[i] =  nbK22s[i] / (double) nbK22sPot[i];
			CCs[i] = 4 * nbK22s[i] / (double) nbK22sPot[i];
		}

		return CCs;
	}

	public static void save(Double2LongMap distribution, String filename)
	{
		Cout.progress("saving " + filename);
		RegularFile out = new RegularFile(filename);
		OutputStream os = out.createWritingStream();
		PrintStream pos = new PrintStream(os);
		CSV.print(distribution.keySet().iterator(), distribution, pos);
		pos.close();
	}

}
