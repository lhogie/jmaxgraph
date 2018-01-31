package jmg.chain;

import java4unix.pluginchain.PluginConfig;
import jmg.Digraph;
import jmg.io.PrettyAdjPrinter;
import toools.io.Cout;

public class info extends JMGPlugin<Digraph, Digraph>
{
	private boolean showOuts, shownIns;
	private boolean hashCode, inhash, outhash;
	private boolean maxOutDegree, maxInDegree;

	@Override
	public Digraph process(Digraph g)
	{
		g.ensureADJLoaded(nbThreads);
		Cout.info("nbVertex=" + g.getNbVertex() + ", nbArcs=" + g.countArcs(nbThreads));

		if (showOuts)
		{
			g.out.ensureDefined(nbThreads);
			Cout.info("out-ADJ:");
			Cout.info(PrettyAdjPrinter.f(g.out.adj));
		}

		if (shownIns)
		{
			g.in.ensureDefined(nbThreads);
			Cout.info("in-ADJ:");
			Cout.info(PrettyAdjPrinter.f(g.in.adj));
		}

		if (maxOutDegree)
		{
			g.out.ensureDefined(nbThreads);
			Cout.info("max-out-degree=" + g.out.maxDegree(nbThreads));
		}

		if (maxInDegree)
		{
			g.in.ensureDefined(nbThreads);
			Cout.info("max-in-degree=" + g.in.maxDegree(nbThreads));
		}

		if (hashCode)
		{
			g.in.ensureDefined(nbThreads);
			Cout.info("hash=" + g.hashCode());
		}

		if (inhash)
		{
			g.in.ensureDefined(nbThreads);
			Cout.info("in-hash=" + g.in.hashCode());
		}

		if (outhash)
		{
			g.out.ensureDefined(nbThreads);
			Cout.info("out-hash=" + g.out.hashCode());
		}

		return g;
	}

	@Override
	public void setup(PluginConfig p)
	{
		super.setup(p);
		showOuts = p.containsAndRemove("outs");
		shownIns = p.containsAndRemove("ins");
		maxOutDegree = p.containsAndRemove("max-out-degree");
		maxInDegree = p.containsAndRemove("max-in-degree");
		hashCode = p.containsAndRemove("hash");
		inhash = p.containsAndRemove("inhash");
		outhash = p.containsAndRemove("outhash");
	}

}
