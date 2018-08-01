package jmg.plugins;

import j4u.chain.PluginParms;
import jmg.Graph;
import jmg.io.PrettyAdjPrinter;
import toools.io.Cout;

public class info extends JMGPlugin<Graph, Graph>
{
	private boolean showOuts, shownIns;
	private boolean hashCode, inhash, outhash;
	private boolean maxOutDegree, maxInDegree;

	@Override
	public Graph process(Graph g)
	{
		if (g == null)
		{
			Cout.info("object is null");
			return g;
		}

		Cout.info("nbVertex=" + g.getNbVertices());

		if (g.out.isDefined() || g.in.isDefined())
			Cout.result("nbArcs=" + g.getNbArcs());

		if (showOuts)
		{
			g.out.ensureLoaded(nbThreads);
			Cout.info("out-ADJ:");
			Cout.info(PrettyAdjPrinter.f(g.out.mem.b));
		}

		if (shownIns)
		{
			g.in.ensureLoaded(nbThreads);
			Cout.info("in-ADJ:");
			Cout.info(PrettyAdjPrinter.f(g.in.mem.b));
		}

		if (maxOutDegree)
		{
			g.out.ensureLoaded(nbThreads);
			Cout.info("max-out-degree=" + g.out.mem.maxDegree());
		}

		if (maxInDegree)
		{
			g.in.ensureLoaded(nbThreads);
			Cout.info("max-in-degree=" + g.in.mem.maxDegree());
		}

		if (hashCode)
		{
			g.in.ensureLoaded(nbThreads);
			Cout.info("hash=" + g.hashCode());
		}

		if (inhash)
		{
			g.in.ensureLoaded(nbThreads);
			Cout.info("in-hash=" + g.in.hashCode());
		}

		if (outhash)
		{
			g.out.ensureLoaded(nbThreads);
			Cout.info("out-hash=" + g.out.hashCode());
		}

		return g;
	}

	@Override
	public void setParameters(PluginParms p)
	{
		super.setParameters(p);
		showOuts = p.containsAndRemove("outs");
		shownIns = p.containsAndRemove("ins");
		maxOutDegree = p.containsAndRemove("max-out-degree");
		maxInDegree = p.containsAndRemove("max-in-degree");
		hashCode = p.containsAndRemove("hash");
		inhash = p.containsAndRemove("inhash");
		outhash = p.containsAndRemove("outhash");
	}

}
