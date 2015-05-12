package futures;

import static futures.BloombergJobTable.*;
import static util.Dates.*;
import static util.Errors.*;

import java.util.*;

import db.*;

public class FuturesBloombergLoader {

	private final Date asOf;

	public FuturesBloombergLoader(Date asOf) {
		this.asOf = asOf;
	}

	public void loadAll() {
		for (ContractCurrent contract : ContractCurrent.contracts()) {
			addJobs(contract);
			for (OptionCurrent option : contract.options())
				addJobs(option);
		}
	}

	private void addJobs(BloombergLoadable loadable) {
		BloombergJob.addJobs(loadable, asOf);
	}
	
	public static void main(String[] args) {
		Date d;
		if(args.length == 0) d = now();
		else if (args.length == 1) d = yyyyMmDd(args[0]);
		else throw bomb(usage());
		BLOOMBERG_JOBS.deleteAllEntries("%future%autogen%");
		new FuturesBloombergLoader(d).loadAll();
		Db.commit();
	}

	private static String usage() {
		System.err.println("Usage: java futures.FuturesBloombergLoader [<date>]");
		return "incorrect arguments";
	}
	

}
