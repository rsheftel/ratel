package systemdb.portfolio;

import static java.util.Collections.*;
import static util.Objects.*;

import java.util.*;

import systemdb.metadata.*;

public class WeightedMsivPvFiles {

	List<String> filenames = empty();
	List<Double> weights = empty();
	List<String> systems = empty();
	List<String> intervals = empty();
	List<String> versions = empty();
	List<String> markets = empty();
    List<String> pvs = empty();
    List<String> msivs = empty();
	
	public WeightedMsivPvFiles(Map<MsivPv, Double> weighting) {
		List<MsivPv> msivPvs = list(weighting.keySet());
		sort(msivPvs);
        for (MsivPv mp : msivPvs) {
			weights.add(weighting.get(mp));
			filenames.add(mp.fileName());
			systems.add(mp.siv().system());
			intervals.add(mp.siv().interval());
			versions.add(mp.siv().version());
			markets.add(mp.market());
			pvs.add(mp.pv().name());
			msivs.add(mp.msivName());
		}
	}
	
	public String[] systems() {
	    return systems.toArray(new String[0]);
	}

	public String[] versions() {
	    return versions.toArray(new String[0]);
	}
	
	public String[] intervals() {
	    return intervals.toArray(new String[0]);
	}
	
	public String[] pvs() {
	    return pvs.toArray(new String[0]);
	}
	
	public String[] filenames() {
		return filenames.toArray(new String[0]);
	}

	public double[] weights() {
		double[] result = new double[weights.size()];
		int i = 0;
		for (double d : weights) 
			result[i++] = d;
		return result;
	}

    public String[] markets() {
        String[] result = new String[weights.size()];
        int i = 0;
        for (String s : markets) 
            result[i++] = s;
        return result;
    }
    
    public String[] msivs() { 
        return msivs.toArray(new String[0]);
    }

}