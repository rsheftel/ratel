package com.malbec.tsdb.ivydb;

import static db.tables.TSDB.TimeSeriesAttributeMapBase.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;

import java.util.*;

import tsdb.*;
import file.*;

public class IvyDbCreateTimeSeriesHistorical {
	public static void main(String[] args) {
		List<Integer> ids = T_TIME_SERIES_ATTRIBUTE_MAP.C_TIME_SERIES_ID.values(T_TIME_SERIES_ATTRIBUTE_MAP.C_ATTRIBUTE_ID.is(25));
		QDirectory directory = new QDirectory("U:/Knell/ivydb_2009/time_series_data");
		directory.createIfMissing();
		DataSource ivydb2009 = source("ivydb_2009");
		for (Integer id : ids) {
			TimeSeries series = series(id);
			Observations observations = series.observations(ivydb2009);
			if(observations.isEmpty()) continue;
			Csv csv = new Csv();
			csv.addHeader("Date", series.name());
			for (Date date : observations)
				csv.add(ymdHuman(date), "" + observations.value(date));
			csv.write(directory.file(series.name() + ".csv"));
		}
	}
}
