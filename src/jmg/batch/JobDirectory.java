package jmg.batch;

import toools.io.file.Directory;

public class JobDirectory extends Directory
{
	final Directory reqD, resD;

	public JobDirectory(Directory parent, String name)
	{
		super(parent, name);
		this.reqD = new Directory(this, "requests");
		this.resD = new Directory(this, "results");
	}

	public void process()
	{
		new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				while (true)
				{
				}
			}
		});
	}

}
