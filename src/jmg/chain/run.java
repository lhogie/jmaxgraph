package jmg.chain;

import j4u.CommandLine;
import j4u.License;
import j4u.chain.PluginFactory;
import j4u.chain.Run;
import toools.SystemMonitor;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;
import toools.thread.MultiThreadProcessing;

public class run extends Run
{
	private int nbThreads;

	public run(RegularFile launcher)
	{
		super(launcher);
		getVMOptions().add("-Xmx200G");
		addOption("--nbThreads", null, "[0-9]+",
				Runtime.getRuntime().availableProcessors() * 2,
				"number of threads used for parallel processing");
		addOption("--systemMonitor", "-m", "[0-9]+", "10000",
				"period of the system monitor, in millisecond");
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{
		int monitorPeriod = Integer.valueOf(getOptionValue(cmdLine, "--systemMonitor"));

		if (monitorPeriod > 0)
		{
			new SystemMonitor(monitorPeriod).start();
		}

		MultiThreadProcessing.NB_THREADS_TO_USE = Integer
				.valueOf(getOptionValue(cmdLine, "--nbThreads"));
		LongProcess fullProcess = new LongProcess("full process", " element", - 1);
		super.runScript(cmdLine);
		fullProcess.end();
		return 0;
	}

	public int getNbThreads()
	{
		return nbThreads;
	}

	@Override
	public String getApplicationName()
	{
		return "jmaxgraph";
	}

	@Override
	public String getAuthor()
	{
		return "Luc Hogie";
	}

	@Override
	public License getLicence()
	{
		return License.ApacheLicenseV2;
	}

	@Override
	public String getYear()
	{
		return "2017-18";
	}

	public static void main(String[] args) throws Throwable
	{
		new run(null).run(args);
	}

	@Override
	protected PluginFactory getPluginFactory()
	{
		return new JMGPluginFactory();
	}
}
