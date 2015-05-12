package tsdb;
import static db.clause.Clause.*;
import static tsdb.DataSourceTable.*;
import static tsdb.TimeSeries.*;
import static util.Objects.*;

import java.util.*;

import util.*;
import db.*;
import db.clause.*;
import db.columns.*;
public class DataSource extends Named {

	private Integer id;
	
	public static final DataSource MARKIT = source("markit");
	public static final DataSource MARKIT_TEST = source("markit_test");
	public static final DataSource INTERNAL = source("internal");
	public static final DataSource INTERNAL_TEST = source("internal_test");
	public static final DataSource TEST_SOURCE = source("test");
	public static final DataSource FINANCIAL_CALENDAR = source("financialcalendar");
	public static final DataSource YAHOO = source("yahoo");
	public static final DataSource IVYDB = source("ivydb");
	public static final DataSource JPMORGAN = source("jpmorgan");
	public static final DataSource JPMORGAN_TEST = source("jpmorgan_test");
	public static final DataSource BLOOMBERG = source("bloomberg");
	public static final DataSource BLOOMBERG_BBT3 = source("bloomberg_BBT3");
	public static final DataSource BLOOMBERG_CMN3 = source("bloomberg_CMN3");
	public static final DataSource BLOOMBERG_BBAM = source("bloomberg_BBAM");
	public static final DataSource BLOOMBERG_TEST = source("bloomberg_test");
	public static final DataSource LEHMAN = source("lehman");
	public static final DataSource MODEL_JPMORGAN_2008 = source("model_jpmorgan_2008");
	
	
	public DataSource(String name) {
		super(name);
	}
	
	public static DataSource source(String name) {
		return new DataSource(name);
	}

	public Observations observations(TimeSeries series) {
		return series.observations(this);
	}

	public Observations observations(TimeSeries series, Range range) {
		return series.observations(this, range);
	}

	@Override
	protected Column<Integer> idColumn() {
		return DATA_SOURCE.C_DATA_SOURCE_ID;
	}

	@Override
	protected StringColumn nameColumn() {
		return DATA_SOURCE.C_DATA_SOURCE_NAME;
	}
	
	@Override
	public int id() {
		if (id == null) id = super.id(); 
		return id;
	}
	
	@Override public String toString() {
		return name();
	}

	public Clause is(IntColumn sourceId) {
		return comment("source = " + name(), sourceId.is(id()));
	}

	public SeriesSource with(TimeSeries series) {
		return new SeriesSource(series, this);
	}
	public SeriesSource with(String seriesName) {
		return new SeriesSource(series(seriesName), this);
	}
	public static List<DataSource> allSources() { 
		List<DataSource> result = empty();
		List<String> names = DATA_SOURCE.C_DATA_SOURCE_NAME.values();
		for (String name : names) 
			result.add(source(name));
		return result;
	}
}
