package tsdb;
import static tsdb.AttributeTable.*;
import static tsdb.TimeSeries.*;
import static util.Arguments.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import tsdb.AttributeTable.*;
import util.*;
import db.*;
import db.clause.*;
public class Attribute implements Comparable<Attribute> {

	
	private final String name;
	private static final Map<String, Attribute> BY_NAME = emptyMap();
	private static final Map<Integer, Attribute> BY_ID = emptyMap();
	private Definition definition;
	private final Map<String, Integer> valueIds = Collections.synchronizedMap(new HashMap<String, Integer>());
	private final Map<Integer, String> valueNames = Collections.synchronizedMap(new HashMap<Integer, String>());
	
	static {
		List<Definition> definitions = ATTRIBUTE.allDefinitions();
		for (Definition definition : definitions) {
			String name = definition.name();
			Attribute attribute = new Attribute(name, definition);
			BY_NAME.put(name, attribute);
			BY_ID.put(attribute.id(), attribute);
		}
	}
	// these constants must be declared after the static block to init by_name and by_id
	public static final Attribute TENOR = attribute("tenor");
	public static final Attribute TIER = attribute("tier");
	public static final Attribute CDS_TICKER = attribute("cds_ticker");
	public static final Attribute CCY = attribute("ccy");
	public static final Attribute TICKER = attribute("ticker");
	public static final Attribute EXPIRY = attribute("expiry");
	public static final Attribute EXPIRY_DATE = attribute("expiry_date");
	public static final Attribute STRIKE = attribute("strike");
	public static final Attribute INSTRUMENT = attribute("instrument");
	public static final Attribute FINANCIAL_CENTER = attribute("financial_center");
	public static final Attribute SECURITY_ID = attribute("security_id");
	public static final Attribute QUOTE_TYPE = attribute("quote_type");
	public static final Attribute QUOTE_SIDE = attribute("quote_side");
	public static final Attribute QUOTE_CONVENTION = attribute("quote_convention");
	public static final Attribute CONTRACT = attribute("contract");
	public static final Attribute FUTURE_MONTH = attribute("future_month");
	public static final Attribute FUTURE_YEAR = attribute("future_year");
	public static final Attribute FUTURE_MONTH_LETTER = attribute("future_month_letter");
	public static final Attribute DOC_CLAUSE = attribute("doc_clause");
	public static final Attribute OPTION_TYPE = attribute("option_type");
	public static final Attribute OPTION_YEAR = attribute("option_year");
	public static final Attribute OPTION_MONTH = attribute("option_month");
	public static final Attribute OPTION_MONTH_LETTER = attribute("option_month_letter");
	public static final Attribute OPTION_CONTRACT = attribute("option_contract");
	public static final Attribute INDEX_SERIES = attribute("index_series");
	public static final Attribute INDEX_VERSION = attribute("index_version");
	public static final Attribute PROGRAM = attribute("program");
	public static final Attribute SETTLE = attribute("settle");
	public static final Attribute COUPON = attribute("coupon");
	public static final Attribute TRANSFORMATION = attribute("transformation");
	public static final Attribute TRANSFORMATION_OUTPUT = attribute("transformation_output");
	public static final Attribute MARKET = attribute("market");
	public static final Attribute INTERVAL = attribute("interval");
	public static final Attribute UNDERLYING = attribute("underlying");
	public static final Attribute BASE = attribute("base");
	public static final Attribute SYSTEM_ID = attribute("system_id");
	public static final Attribute SIV = attribute("siv");
	public static final Attribute PV = attribute("pv");
	public static final Attribute METRIC = attribute("metric");
	public static final Attribute CDS_STRIKE = attribute("cds_strike");
	

	private Attribute(String name, Definition definition) {
		this.name = name;
		this.definition = definition;
	}
	
	public static Attribute attribute(String name) {
		return bombNull(BY_NAME.get(name), "couldn't find attribute for " + name);
	}

	public int id() {
		return definition.id();
	}

	public List<Integer> valueIds(List<String> names) {
		cacheValues(names);
		List<Integer> result = empty();
		for (String value : names)
			result.add(bombNull(
				valueIds.get(value), 
				"cannot find id in attribute " + definition.name() + 
				", table: " + definition.valueTableName() + " " + "for value: " + value
			));
		return result;
	}

	private void cacheValues(List<String> names) {
		if (valueIds.keySet().containsAll(names)) return;
		addValues(names);
	}

	public void cacheAllValues() {
		valueIds.putAll(definition.valueIds());
		for (Map.Entry<String, Integer> entry : valueIds.entrySet()) 
			valueNames.put(entry.getValue(), entry.getKey());
	}

	private void addValues(List<String> names) {
		valueIds.putAll(definition.valueIds(names));
		for (Map.Entry<String, Integer> entry : valueIds.entrySet()) 
			valueNames.put(entry.getValue(), entry.getKey());
	}

	public Clause matches(Column<Integer> attributeId) {
		return attributeId.is(id());
	}

	public void createValues(List<String> newValues, Cell<?> ... extra) {
		definition.createValues(newValues, extra);
		addValues(newValues);
	}

	public boolean valuesExist(List<String> possibleValues) {
		cacheValues(possibleValues);
		for (String valueName : possibleValues)
			if(!valueIds.containsKey(valueName))
				return false;
		return true;
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Attribute other = (Attribute) obj;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}
	
	@Override public String toString() {
		return name;
	}

	public String valueName(Integer valueId) {
		if(!valueNames.containsKey(valueId)) {
			String valueName = definition.valueName(valueId);
			valueNames.put(valueId, valueName);
			valueIds.put(valueName, valueId);
		}
		return bombNull(valueNames.get(valueId),"cannot find value name in " + name + " for value id: " + valueId);
	}

	public static Attribute attribute(Integer attributeId) {
		return bombNull(BY_ID.get(attributeId), "no attribute for id: " + attributeId);
	}
	
	public AttributeValue value(Date d) {
		return value(ymdHuman(d));
	}

	public AttributeValue value(String ... values) {
		return new AttributeValue(this, values);
	}

	public AttributeValue value(TimeSeries series) {
		return series.attributes().get(this);
	}

	public AttributeValue value(Integer id) {
		return value(valueName(id));
	}

	public String name() {
		return name;
	}

	@Override public int compareTo(Attribute o) {
		return name.compareTo(o.name);
	}

	public Row extra(AttributeValue value) {
		return definition.valueRow(value);
	}
	
	public static void main(String[] args) {
		doNotDebugSqlForever();
		bombUnless(args.length > 0, "Usage: tsdb.Attribute -attribute value [-attribute2 value2 ...]\n");
		List<String> empty = empty();
		Arguments arguments = arguments(args, empty);
		AttributeValues values = Arguments.values(arguments);
		List<TimeSeries> serieses = multiSeries(values);
		System.out.println("Found " + serieses.size() + " time series");
		for (TimeSeries series : serieses)
			System.out.println(series.name());
		System.out.println();
		for (TimeSeries series : serieses) {
			AttributeValues v = series.attributes();
			System.out.println(series.name() + "\t" + series.id() + "\t" + v);
		}
	}

}
