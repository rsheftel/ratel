package com.malbec.tsdb.ivydb;

import static tsdb.DataSource.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Times.*;

import java.util.*;

import tsdb.*;

import com.malbec.tsdb.loader.*;

import db.*;
import db.clause.*;

public class IvyDbLoaderHistorical {
	public static final int NUM_THREADS = 8;
	
	public static void main(String[] unused) {
		doNotDebugSqlForever();
		Date d = date("2009/05/14");
		final SecurityPriceLoader securityPrice = new SecurityPriceLoader("us");
		final TimeSeriesLookup securityPriceLookup = securityPrice.seriesLookup(Clause.TRUE);
		final StdOptionPriceLoader optionPrice = new StdOptionPriceLoader("us");
		final TimeSeriesLookup optionPriceLookup = optionPrice.seriesLookup(Clause.TRUE);
		final OptionVolumeLoader volume = new OptionVolumeLoader("us");
		final TimeSeriesLookup volumeLookup = volume.seriesLookup(Clause.TRUE);
		final DataSource source = source("ivydb_2009");
		List<Thread> threads = empty();
		while(d.after(date("2009/03/18"))) {
			if(threads.size() == NUM_THREADS) {
				boolean threadStopped = false;
				while(!threadStopped) {
					for (Thread thread : threads) {
						if(!thread.isAlive()) {
							threadStopped = true;
							threads.remove(thread);
							break;
						}
					}
					sleep(100);
				}
			} else {
				sleepSeconds(60);
			}
			final Date myD = d;
			Thread thread = new Thread() {
				@Override
				public void run() {
					info("processing " + ymdHuman(myD));
					securityPrice.loadAll(source, myD, securityPriceLookup);
					optionPrice.loadAll(source, myD, optionPriceLookup);
					volume.loadAll(source, myD, volumeLookup);
					Db.commit();
				}
			};
			threads.add(thread);
			thread.start();
			d = businessDaysAgo(1, d, "nyb");
		}
	}
}
