package jmg.exp.nathann;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.plugins.JMGPlugin;
import toools.io.file.Directory;

public class Main_CountK22AndTransitiveTriangles extends JMGPlugin<Graph, GlobalCount>
{
	private int nbJobs = 500;
	private boolean waitUntilCompletion;
	private boolean processLocally;

	@Override
	public GlobalCount process(Graph g)
	{
		CountingK22AndTransitiveTrianglesProblem problem = new CountingK22AndTransitiveTrianglesProblem(
				g, true, - 1);

		GlobalCount r = problem.map(new Directory(g.jmgDirectory, "k22"), nbJobs,
				waitUntilCompletion, processLocally);
		return r;
	}

	@Override
	public void setParameters(PluginParms p)
	{
		super.setParameters(p);
		this.waitUntilCompletion = p.getBoolean("waitUntilCompletion");
		this.processLocally = p.getBoolean("processLocally");
	}
}
