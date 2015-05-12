package mortgage;

import static futures.BloombergJobTable.*;
import static mortgage.TbaTable.*;
import static util.Dates.*;
import static util.Errors.*;

import java.util.*;

import db.*;

import futures.*;
import mortgage.TbaTable.*;

public class TbaBloombergLoader {

	public static void main(String[] args) {
		Date d;
		if(args.length == 0) d = now();
		else if (args.length == 1) d = yyyyMmDd(args[0]);
		else throw bomb(usage());
		BLOOMBERG_JOBS.deleteAllEntries("%tba_autogen%"); 
		for(Tba tba : TBA.tbas()) 
			BloombergJob.addJobs(tba, d);
		Db.commit();
	}

	private static String usage() {
		System.err.println("Usage: java mortgage.TbaBloombergLoader [<date>]");
		return "incorrect arguments";
	}

}
