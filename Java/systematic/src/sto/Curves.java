package sto;

import static util.Objects.*;

import java.util.*;

import file.*;

public class Curves {

	private final QDirectory stoDir;

	public Curves(QDirectory directory) {
		this.stoDir = directory;
	}

	public CurveFile curve(String msiv, int run) {
		return new CurveFile(stoDir.file("CurvesBin", msiv, "run_" + run + ".bin"));
	}

	public int runCount(List<WeightedMsiv> msivs) {
		return firstMsivDir(msivs).files("run_\\d+\\.bin").size();
	}

	private QDirectory firstMsivDir(List<WeightedMsiv> msivs) {
		return stoDir.directory("CurvesBin", first(msivs).msiv());
	}

	public List<Integer> runs(List<WeightedMsiv> msivs) {
		List<Integer> result = empty();
		for (QFile file : firstMsivDir(msivs).files()) 
			result.add(Integer.valueOf(file.name().replaceAll("run_(\\d+).bin", "$1")));
		return result;
	}

	public List<CurveFile> curves(List<WeightedMsiv> msivs, int run) {
		List<CurveFile> result = empty();
		for (WeightedMsiv msiv : msivs)
			result.add(curve(msiv.msiv(), run));
		return result;
	}

	public double[] dates(List<WeightedMsiv> msivs) {
		Set<Double> dates = new TreeSet<Double>();
		int firstRun = first(runs(msivs));
		for (WeightedMsiv msiv : msivs) {
			CurveFile curve = curve(msiv.msiv(), firstRun);
			curve.load();
			for (double d : curve.dates()) 
				dates.add(d);
		}
		double[] result = new double[dates.size()];
		int i = 0;
		for (double d : dates) result[i++] = d;
		return result;
	}


}
