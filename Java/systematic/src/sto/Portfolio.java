package sto;

import static sto.PortfolioBacktestTable.*;
import static systemdb.metadata.SystemDetailsTable.*;
import static util.Errors.*;
import static util.Objects.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import systemdb.metadata.*;
import systemdb.metadata.SystemDetailsTable.*;
import util.*;
import db.*;
import db.columns.*;
import file.*;

public class Portfolio implements Serializable {

    private static final long serialVersionUID = 1L;
    private final List<WeightedMsiv> msivs = empty();
	private final String name;

	public Portfolio(QFile file) {
	    for (String line : file.lines()) 
			msivs.add(new WeightedMsiv(line));
		bombIf(msivs.isEmpty(), "empty portfolio not allowed!");
		String market = file.name();
		name = first(msivs).sivString() + "_" + market;
	}

	@Override public String toString() {
	    return name;
	}
	
	@Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((msivs == null) ? 0 : msivs.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Portfolio other = (Portfolio) obj;
        if (msivs == null) {
            if (other.msivs != null) return false;
        } else if (!msivs.equals(other.msivs)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    public Portfolio(String name, List<WeightedMsiv> msivs) {
	    this.name = name;
	    this.msivs.addAll(msivs);
	}
	
	public Portfolio(String name) {
	    this.name = name;
	}
	
	public String name() {
	    return name;
	}
	
	public Siv siv() {
	    return first(msivs).siv();
	}

    public List<WeightedMsiv> msivs() { 
		return msivs;
	}

	public void combineCurves(final Curves curves, int nParallel) {
		curves.curve(name, 1).ensurePath();
		final double[] dates = curves.dates(msivs());
		List<Callable<Integer>> combiners = empty();
		for (Integer run : curves.runs(msivs())) {	
			final int runNumber = run;
			combiners.add(new Callable<Integer>() {
				@Override public Integer call() {
					combineOneCurve(curves, dates, runNumber);
					return runNumber;
				}
			});
		}
		ExecutorService pool = Executors.newFixedThreadPool(nParallel);
		try {
			List<Future<Integer>> results = pool.invokeAll(combiners);
			int left = results.size();
			for (Future<Integer> result : results) 
				Log.info("master collected run " + result.get() + " remaining: " + --left);
			pool.shutdown();
		} catch (Exception e) {
			throw bomb("interrupted", e);
		}
		Log.info("master finished");
	}

	private void combineOneCurve(Curves curves, double[] dates, int run) {
		CurveFile portfolioRun = curves.curve(name, run);
		if(portfolioRun.exists()) { return; }
		portfolioRun.init(dates);
		for (WeightedMsiv msiv : msivs) {
			CurveFile curveFile = curves.curve(msiv.msiv(), run);
			curveFile.load();
			portfolioRun.add(curveFile, msiv.weight());
		}
		portfolioRun.save();
	}
	
	public static void main(String[] args) {
		bombUnless(args.length == 3, "Usage: java sto.Portfolio stoDir portfolioName nParallel");
		QDirectory stoDir = new QDirectory(args[0]);
		Portfolio portfolio = new Portfolio(stoDir.file("Portfolios", args[1]));
		int nParallel = Integer.valueOf(args[2]);
		portfolio.combineCurves(new Curves(stoDir), nParallel);
	}

    public void store(int systemId) {
        SystemDetails details = DETAILS.details(systemId);
        for(WeightedMsiv msiv : msivs)
            PORTFOLIO_BACKTEST.insert(details, this, msiv);
    }

    public Cell<?> cell(NvarcharColumn nameCol) {
        return nameCol.with(name);
    }

    public static List<Portfolio> portfolios(int systemId) {
        List<Portfolio> result = empty();
        for (String name : PORTFOLIO_BACKTEST.names(systemId))
            result.add(portfolio(systemId, name));
        return result;
    }

    public static Portfolio portfolio(int systemId, String name) {
        return PORTFOLIO_BACKTEST.portfolio(systemId, name);
    }

    public void add(WeightedMsiv msiv) {
        msivs.add(msiv);
    }
}
