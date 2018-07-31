package jmg.algo;

import java.io.IOException;

import jexperiment.Curve;
import jexperiment.Experiment;
import jexperiment.NumericalPlot2D;
import jmg.io.adj.ADJReader;
import jmg.io.adj.TextADJFastReader;
import jmg.io.adj.TextADJSlowReader;
import toools.io.Cout;
import toools.io.file.RegularFile;
import toools.math.MathsUtilities;

public class BenchLoad
{
	public static void main(String[] args) throws IOException
	{
		Experiment exp = new Experiment(BenchLoad.class.getName());
		NumericalPlot2D plot = exp.createPlot("perf", "#cores", "time (s)", null);

		for (ADJReader reader : new ADJReader[] { new TextADJFastReader(),
				new TextADJSlowReader() })
		{
			Curve curve = plot.createCurve(reader.getClass().getName());

			
			
			for (int nbThreads = 1; nbThreads <= Runtime.getRuntime()
					.availableProcessors() * 4; nbThreads *= 2)
			{
				long[] durations = new long[5];

				for (int run = 0; run < durations.length; ++run)
				{
					if (curve.getNumberOfValuesAt(nbThreads) <= run)
					{
						long a = System.currentTimeMillis();
						reader.nbThreads = nbThreads;
						reader.nbVerticesExpected = 400000000;
						reader.nbArcsExpected = 23000000000L;
						reader.readFile(new RegularFile("/home/lhogie/data/big.adj"));
						long durationS = (System.currentTimeMillis() - a) / 1000;
						curve.addPoint(nbThreads, durationS);
						durations[run] = durationS;
						Cout.result(reader.getClass(), nbThreads, run, durationS);
					}
				}

				double avg = MathsUtilities.avg(durations);
				double stddev = MathsUtilities.stdDev(durations);
				Cout.result(reader.getClass(), " nbThreads=" + nbThreads, "avg=" + avg,
						"stddev=" + stddev);

			}
		}

		exp.plot();
	}
}
