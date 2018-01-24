package jmg.chain;

import java4unix.pluginchain.PluginConfig;
import java4unix.pluginchain.TooolsPlugin;
import jmg.Digraph;
import jmg.io.PrettyAdjPrinter;
import toools.io.Cout;

public class info implements TooolsPlugin<Digraph, Digraph>
{
	private boolean showOuts, shownIns;
	private boolean maxOutDegree, maxInDegree;

	@Override
	public Digraph process(Digraph g)
	{
		g.ensureADJLoaded();
		Cout.info("nbVertex=" + g.getNbVertex() + ", nbArcs=" + g.countArcs());

		if (showOuts)
		{
			g.out.ensureDefined();
			Cout.info("out-ADJ:");
			Cout.info(PrettyAdjPrinter.f(g.out.adj));
		}

		if (shownIns)
		{
			g.in.ensureDefined();
			Cout.info("in-ADJ:");
			Cout.info(PrettyAdjPrinter.f(g.in.adj));
		}

		if (maxOutDegree)
		{
			g.out.ensureDefined();
			Cout.info("max-out-degree=" + g.out.maxDegree());
		}

		if (maxInDegree)
		{
			g.in.ensureDefined();
			Cout.info("max-in-degree=" + g.in.maxDegree());
		}

		return g;
	}

	@Override
	public void setup(PluginConfig p)
	{
		showOuts = p.containsAndRemove("out");
		shownIns = p.containsAndRemove("in");
		maxOutDegree = p.containsAndRemove("max-out-degree");
		maxInDegree = p.containsAndRemove("max-in-degree");
	}
}
