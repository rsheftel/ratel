package com.malbec.tsdb.loader;

import static db.Column.*;
import static transformations.Constants.*;
import static tsdb.TSAMTable.*;
import static util.Objects.*;

import java.util.*;

import mail.*;
import tsdb.*;
import util.*;
import static util.Errors.*;

import com.malbec.tsdb.markit.*;

import db.*;
import db.clause.*;
import db.temptables.TSDB.*;

public class TimeSeriesLookup {

	private static final int INVALID_TIME_SERIES_ID = -1;
	private final Clause filter;
	private final Attribute[] attributes;
	private final Map<String, Integer> ids = emptyMap();
	private final List<Duplicate> dupes = Objects.empty();
	boolean cacheNames = false;
	
	public TimeSeriesLookup(Clause filter, Attribute ... attributes) {
		this.filter = filter;
		this.attributes = attributes;
		Log.info("construct series lookup");
		createKeysTable();
		populateLookup();
		emailDuplicates();
	}
	
	private void emailDuplicates() {
		if (dupes.isEmpty()) return;
		Email email = Email.problem("duplicate time series ids found", "");
		for (Duplicate dupe : dupes) 
			dupe.addTo(email);
		email.sendTo(FAILURE_ADDRESS);
	}
	
	private TimeSeriesLookup() {
		filter = Clause.FALSE;
		attributes = new Attribute[0];
	}
	
	class Duplicate {

		private final Integer tsId1;
		private final Integer tsId2;
		private final String key;

		public Duplicate(String key, Integer tsId1, Integer tsId2) {
			this.key = key;
			this.tsId1 = tsId1;
			this.tsId2 = tsId2;
		}

		public void addTo(Email email) {
			email.append("key: " + key + " ts1: " + tsId1 + " ts2: " + tsId2);
		}
		
	}
	
	private void populateLookup() {
		TsamKeysBase keys = new TsamKeysBase("keys");
		for (Row r : keys.rows(Clause.TRUE)) {
			String key = r.value(keys.C_TS_KEY);
			Integer value = r.value(keys.C_TIME_SERIES_ID);
			if (ids.containsKey(key)) {
				dupes.add(new Duplicate(key, value, ids.get(key)));
				value = INVALID_TIME_SERIES_ID;
			}
			ids.put(key, value);
		}
	}

	private void createKeysTable() {
		ConvertColumn<String, Integer> tsKeyColumn = new ConvertColumn<String, Integer>("varchar(255)", TSAM.C_ATTRIBUTE_VALUE_ID, "ts_key");
		Clause matches = TSAM.C_ATTRIBUTE_ID.is(attributes[0].id()).and(filter);
		SelectMultiple select = TSAM.select(columns(TSAM.C_TIME_SERIES_ID, tsKeyColumn), matches);
		TsamKeysBase keys = new TsamKeysBase("keys");
		select.intoTemp(keys.name());
//		new Generator().writeFile(temp.schemaTable(), "temptables");
		for(int i = 1; i < attributes.length; i++)
			concatenateValueToKey(tsKeyColumn, keys, attributes[i]);
	}

	private void concatenateValueToKey(ConvertColumn<String, Integer> tsKeyColumn, TsamKeysBase keys, Attribute attribute) {
		Clause quoteType = TSAM.C_ATTRIBUTE_ID.is(attribute.id());
		Clause join = TSAM.C_TIME_SERIES_ID.joinOn(keys);
		Clause notMissing = TSAM.C_ATTRIBUTE_VALUE_ID.isNot(INVALID_TIME_SERIES_ID);
		Clause matches = quoteType.and(join).and(notMissing);
		Row replacements = new Row(keys.C_TS_KEY.withColumn(keys.C_TS_KEY.plus(":" + attribute.id() + "=").plus(tsKeyColumn)));
		keys.updateAll(replacements, matches);
	}

	public Integer id(AttributeValues values) {
		Integer id = ids.get(key(values));
		bombIf(id != null && id == INVALID_TIME_SERIES_ID, "id requested for time series with duplicated attributes: " + values);
		return id;
	}

	private String key(AttributeValues values) {
		StringBuilder buf = new StringBuilder();
		buf.append(values.get(attributes[0]).id()).append(":");
		for (int i = 1; i < attributes.length; i++) {
			Attribute attribute = attributes[i];
			if(values.has(attribute))
				buf.append(attribute.id() + "=" + values.get(attribute).id()).append(":");
		}
		buf.setLength(buf.length() - 1);
		return buf.toString();
	}

	public static TimeSeriesLookup empty() {
		return new TimeSeriesLookup();
	}

	public int create(String tsName, AttributeValues values) {
		TimeSeries ts = new TimeSeries(tsName);
		ts.create(values);
		int newId = ts.id();
		ids.put(key(values), newId);
		return newId;
	}

}
