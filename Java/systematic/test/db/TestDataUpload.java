package db;
import static db.tables.SystemDB.TSDBBase.*;
import static db.tables.TSDB.TimeSeriesDataScratchBase.*;
import static systemdb.metadata.SystemTimeSeriesTable.*;
import static tsdb.DataSource.*;
import static util.Objects.*;

import java.util.*;

import systemdb.metadata.*;

import db.tables.TSDB.*;
import file.*;

public class TestDataUpload extends DbTestCase {

	private static final DataSourceBase SOURCE = new DataSourceBase("ds");
	private static final CcyBase CCY = new CcyBase("ccy");
	private static final String FILE = "foo.csv";
	private final QFile foo = new QFile(FILE);
	private DataUpload upload;

	@Override protected void setUp() throws Exception {
		super.setUp();
		foo.deleteIfExists();
		upload = new DataUpload(FILE);
		T_TIME_SERIES_DATA_SCRATCH.deleteAll(
			TEST_SOURCE.is(T_TIME_SERIES_DATA_SCRATCH.C_DATA_SOURCE_ID)
		);
	}
	
	public void testCanUploadTwoSimpleRecords() throws Exception {
		addDsTable();
		addDataSource("jefftest");
		addDataSource("jefftest2");
		upload.writeCsv();
		assertEquals("\"TSDB..data_source\"\n\"data_source_name\"\n\"jefftest\"\n\"jefftest2\"\n", foo.text());
		upload = new DataUpload(FILE);
		upload.upload();
		assertCount(2, SOURCE.C_DATA_SOURCE_NAME.like("jeff%"));
	}
	
	public void testCanUploadRecordsWithLineNoise() throws Exception {
		foo.append("data_source\n");
		foo.append("data_source_name\n");
		foo.append("\"je[]ff\\te\"\",\"\"s\nt\"\n");
		upload = new DataUpload(FILE);
		upload.upload();
		String expected = "je[]ff\\te\",\"s\nt";
		assertCount(1, SOURCE.C_DATA_SOURCE_NAME.is(expected));
	}
	
	private void addDsTable() {
		upload.addColumn(SOURCE.C_DATA_SOURCE_NAME);
	}

	private void addDataSource(String name) {
		List<Cell<?>> cells = empty();
		cells.add(SOURCE.C_DATA_SOURCE_NAME.with(name));
		upload.add(cells);
	}
	
	public void testCanUploadTwoComplexRecords() throws Exception {
		addCcyTable();
		addCcy(CCY, "jeffe2");
		addCcy(CCY, "jeffe3");
		addCcy(CCY, "jeffe4");
		upload.writeCsv();
		assertSize(5, foo.lines());
		upload = new DataUpload(FILE);
		upload.upload();
		assertCount(3, CCY.C_CCY_NAME.like("jeff%"));
	}

	private void addCcyTable() {
		upload.addColumn(CCY.C_CCY_NAME);
		upload.addColumn(CCY.C_DESCRIPTION);
		upload.addColumn(CCY.C_PRECEDENCE);
	}
	
	public void testInsertNullInBitColumn() throws Exception {
	    SYSTEM_TS.insert("something", HistoricalProvider.ASC11, HistoricalProvider.ASC11);
	    SYSTEM_TS.insert("something2", HistoricalProvider.ASC11, HistoricalProvider.ASC11);
	    foo.append("SystemDB..TSDB, SystemDB..TSDB, SystemDB..TSDB, SystemDB..TSDB, SystemDB..TSDB\n");
	    foo.append("Name, Data_source, Template, Name_open, Name_close\n");
	    foo.append("something,test,  , 7, 9\n");
	    foo.append("something2,test,  ,, 7\n");
	    new DataUpload(FILE).upload();
	    Row r = T_TSDB.row(T_TSDB.C_NAME.is("something"));
	    assertNull(r.value(T_TSDB.C_TEMPLATE));
	    Row r2 = T_TSDB.row(T_TSDB.C_NAME.is("something2"));
	    assertNull(r2.value(T_TSDB.C_NAME_OPEN));
    }
	
	public void testSkipsBadRowsAndBadColumnsAndEmptyRecords() throws Exception {
		foo.append("ccy,ccy,ccy\n");
		foo.append("ccy_name*,description,precedence\n");
		foo.append("jeffccy1,some durn thing,1.0\n");
		foo.append("jeffccy2,some durn thing,notnumeric\n");
		foo.append("\n");
		foo.append(",,\n");
		foo.append("jeffccy3,some durn thing,1.1\n");
		new DataUpload(FILE).upload();
		assertEquals(list("jeffccy1", "jeffccy3"), CCY.C_CCY_NAME.values(CCY.C_CCY_NAME.like("jeff%")));
	}
	
	public void testUpdatesPrimaryKeys() throws Exception {
	    foo.append("ccy,ccy,ccy\n");
	    foo.append("ccy_name*,description,precedence\n");
	    foo.append("jeffccy1,some durn thing,1.0\n");
	    foo.append("jeffccy1,some durn thing,2.7\n");
	    foo.append("jeffccy2,some other thing,34\n");
	    new DataUpload(FILE).upload();
	    assertEquals(2.7, CCY.C_PRECEDENCE.value(CCY.C_CCY_NAME.is("jeffccy1")));
	}
	
	public void testAlternateFormat() throws Exception {
		foo.append("ccy:ccy_name*,ccy:description,ccy:precedence\n");
		foo.append("jeffccy1,some durn thing,1.0\n");
		foo.append("jeffccy3,some durn thing,1.1\n");
		new DataUpload(FILE).upload();
		assertCount(2, CCY.C_CCY_NAME.like("jeff%"));
	}
	
	public void testCanUploadMultipleTablesWithIntermingledColumns() throws Exception {
		addDsTable();
		addCcyTable();
		addDsCcy("jeffds1", "jefcc1");
		addDsCcy("jeffds2", "jefcc1");
		addDsCcy("jeffds1", "jefcc2");
		upload.writeCsv();
		new DataUpload(FILE).upload();
		assertCount(2, SOURCE.C_DATA_SOURCE_NAME.like("jeff%"));
		assertCount(2, CCY.C_CCY_NAME.like("jefcc%"));
	}
	
	public void testCanUploadToTimeSeriesDataScratch() throws Exception {
		foo.append("time_series_data_scratch,time_series_data_scratch,time_series_data_scratch,time_series_data_scratch\n");
		foo.append("time_series_id,data_source_id,observation_time,observation_value\n");
		foo.append("4,4,2007/01/01,123.45");
		new DataUpload(FILE).upload();
		assertCount(1, T_TIME_SERIES_DATA_SCRATCH.C_TIME_SERIES_ID.is(4));
	}

	private void addDsCcy(String sourceName, String ccyName) {
		List<Cell<?>> cells = empty();
		cells.add(SOURCE.C_DATA_SOURCE_NAME.with(sourceName));
		cells.add(CCY.C_CCY_NAME.with(ccyName));
		cells.add(CCY.C_DESCRIPTION.with(ccyName));
		cells.add(CCY.C_PRECEDENCE.with(1.0));
		upload.add(cells);
	}

	private void addCcy(CcyBase ccy, String name) {
		List<Cell<?>> cells = empty();
		cells.add(ccy.C_CCY_NAME.with(name));
		cells.add(ccy.C_DESCRIPTION.with(name));
		cells.add(ccy.C_PRECEDENCE.with(1.0));
		upload.add(cells);
	}
	
}
