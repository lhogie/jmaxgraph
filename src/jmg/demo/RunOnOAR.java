package jmg.demo;

import joar.Job;

public class RunOnOAR
{
	public static void main(String[] args)
	{
		String frontal = "nef-frontal.inria.fr";
		String cmd = "'java -classpath $(cat $HOME/.classpath) -Xmx200G jmg.chain.Run $HOME/datasets/sample-0.001a.jmg info'";
		String resourceRequested = "-p 'mem > 180000' -l '/nodes=1,walltime=15:0:0'";
		String name = "foobar";
		Job j = Job.getOrCreate(frontal, resourceRequested, cmd, name);

		j.connect();
	}
}
