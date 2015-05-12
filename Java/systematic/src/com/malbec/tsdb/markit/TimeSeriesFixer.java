package com.malbec.tsdb.markit;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static tsdb.TSAMTable.*;
import static tsdb.TimeSeriesTable.*;
import static util.Strings.*;

import java.util.*;

import tsdb.*;
import db.*;
import file.*;


public class TimeSeriesFixer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Load time series names from csv
		List<String> names = new QFile("H:/bad_cds_tickers_weight.csv").lines();
		AttributeValue instrument = INSTRUMENT.value("cds");
		for (String name : names) {
			System.out.println(name);
			String[] parts = name.split("_");
			AttributeValues values = values();
			values.add(instrument);
			values.add(TICKER.value(parts[0]));
			values.add(TIER.value(parts[1]));
			values.add(CCY.value(parts[2]));
			values.add(DOC_CLAUSE.value(parts[3]));
			values.add(CDS_TICKER.value(join("_", parts[0], parts[1], parts[2], parts[3])));
			if(parts[4].equals("av")) {
				values.add(QUOTE_TYPE.value("av_rating"));
			} else if(parts[4].equals("composite")) {
				values.add(QUOTE_TYPE.value("composite_depth"));
				values.add(TENOR.value("5y"));
			} else if (parts[4].equals("recovery")) {
				values.add(QUOTE_TYPE.value("recovery"));
			} else if (parts[4].equals("spread")) {
				values.add(QUOTE_TYPE.value("spread"));
				values.add(TENOR.value(parts[5]));
			}
			System.out.println(values);
			TimeSeries series = new TimeSeries(name);
			TSAM.deleteAttributes(series.id());
			TIME_SERIES.createAttributeValues(name, values);
			Db.commit();
		}
	}

}
