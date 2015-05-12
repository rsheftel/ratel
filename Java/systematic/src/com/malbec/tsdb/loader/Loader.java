package com.malbec.tsdb.loader;

import static mail.Email.*;
import static tsdb.TimeSeriesDataTable.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import mail.*;
import tsdb.*;
import util.*;
import db.*;
import db.clause.*;

public abstract class Loader <ROW> {

	private final String name;
	private final String failureAddresses;
	private final List<TimeSeriesDefinition<ROW>> definitions = empty();


	public Loader(String name, String failureAddresses, TimeSeriesDefinition<ROW> ... definitions) {
		this.name = name;
		this.failureAddresses = failureAddresses;
		add(definitions);
	}

    public <T extends TimeSeriesDefinition<ROW>> void add(T ... definition) {
        definitions.addAll(list(definition));
    }

    public <T extends TimeSeriesDefinition<ROW>> void add(T definition) {
        definitions.add(definition);
    }
	
	protected List<TimeSeriesDefinition<ROW>> definitions() { 
		return definitions;
	}

	protected void addObservations(DataSource source, Date date, ROW row, TimeSeriesLookup lookup, List<Row> observationRows) {
		for (TimeSeriesDefinition<ROW> definition : definitions())
			addObservation(definition.dataPoint(row, lookup), source, date, observationRows);
	}
	
	@SuppressWarnings("unchecked") public boolean loadOne(ROW row, DataSource targetSource, Date date, Clause filter) {
		Db.setQueryTimeout(1500);
		return loadRows(targetSource, date, seriesLookup(filter), list(row));
	}
	
	protected boolean loadRows(DataSource targetSource, Date date,
		TimeSeriesLookup lookup, List<ROW> inputRows
	) {
		int count = 1;
		Email failureMessage = problem("FAILURE in " + name + " loader", "");
 		if (inputRows.isEmpty()) noData(failureMessage, date);
 		List<Row> observationRows = empty();
		for (ROW row : inputRows) {
			try {
				Log.info("processing row " + count++ + " of " + inputRows.size());
				addObservations(targetSource, date, row, lookup, observationRows);
			} catch (RuntimeException e) {
				failureMessage.append("error occured in loading inputRow: " + row + trace(e));
			}
		}
		try {
			if (!observationRows.isEmpty()) {
				writeUsingTempWithCommits(observationRows);
				observationRows.clear();
			}
		} catch (RuntimeException e) {
			failureMessage.append("error occurred writing data " + trace(e));
		}
		if (failureMessage.hasContent()) {
			failureMessage.sendTo(new EmailAddress(failureAddresses));
			Log.info(failureMessage.content());
		}
		return failureMessage.hasContent();
	}

	protected void noData(Email failureMessage, @SuppressWarnings("unused") Date date) {
		failureMessage.append("No data to load!");
	}



	protected void addObservation(TimeSeriesDataPoint point, DataSource source, Date date, List<Row> observationRows) {
		if (point.value() == null) return;
		Observations observations = new Observations(adjustDate(date, point), point.value());
		observationRows.addAll(observationRows(point.id(), source.id(), observations));
	}

	protected Date adjustDate(Date date, @SuppressWarnings("unused") TimeSeriesDataPoint point) {
		return date;
	}

	protected TimeSeriesLookup seriesLookup() {
		return seriesLookup(Clause.TRUE);
	}

	protected abstract TimeSeriesLookup seriesLookup(Clause filter);

	public String failureAddresses() {
		return failureAddresses;
	}

	public boolean loadAll(DataSource targetSource, Date date, TimeSeriesLookup seriesLookup) {
		Log.info("load data into TSDB");
		return loadRows(targetSource, date, seriesLookup, inputRows(date));
	}

	protected abstract List<ROW> inputRows(Date date);
	
	
	public boolean loadAll(DataSource targetSource, Date date) {
		Db.setQueryTimeout(1500);
		return loadAll(targetSource, date, seriesLookup());
	}

	public static void usage(String[] args) {
		String usage = "usage: java <fully.qualified.Loader> <failureaddress> [<date>]";
		bombUnless(list(1, 2).contains(args.length), usage);
		Email.requireValidAddress(args[0], usage);
	}
	
	public void run(String[] args, String dataSource) {
		boolean failed = runNoExit(args, dataSource);
		if (failed) System.exit(1);
	}

	public boolean runNoExit(String[] args, String dataSource) {
		DataSource from = new DataSource(dataSource);
		bombUnless(from.id() > 0, "data source " + dataSource + " does not exist!");
		Date runDate = now();
		if(runDate.before(hoursAhead(16, midnight(runDate))))
			runDate = daysAgo(1, runDate);
		runDate = midnight(runDate);
		if (args.length > 1) 
			runDate = Dates.yyyyMmDd(args[1]);
		checkDate(runDate);
		boolean failed = loadAll(from, runDate);
		Db.commit();
		return failed;
	}

	protected void checkDate(Date runDate) {
		if(isHoliday(runDate, "nyb")) {
			Log.info(yyyyMmDd(runDate) + " is a holiday.  Skipping...");
			System.exit(0);
		}
	}
	
}
