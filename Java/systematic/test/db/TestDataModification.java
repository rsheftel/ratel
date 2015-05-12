package db;

import java.sql.*;

import db.tables.TSDB.*;


public class TestDataModification extends DbTestCase {

	static class SeriesTable extends TimeSeriesBase {
	    private static final long serialVersionUID = 1L;
	    protected SeriesTable() {
			super("testTs");
		}

		public void insert(String name) {
			insert(C_TIME_SERIES_NAME.with(name), C_DATA_TABLE.with("test"));
		}

		public void delete(String name) {
		    deleteAll(C_TIME_SERIES_NAME.is(name)); 
		}

		public int id(String name) {
			return C_TIME_SERIES_ID.value(C_TIME_SERIES_NAME.is(name));
		}
		
		public void update(String fromTsName, String toTsName) {
			updateOne(new Row(C_TIME_SERIES_NAME.with(toTsName)), C_TIME_SERIES_NAME.is(fromTsName));
		}

	}
	private static final SeriesTable SERIES = new SeriesTable();
    private static final long serialVersionUID = 1L;
    static class DataTable extends TimeSeriesDataBase {
        private static final long serialVersionUID = 1L;
        protected DataTable() {
			super("testData");
		}

		public void insert(int id, double d, Timestamp date) {
			insert(
				C_TIME_SERIES_ID.with(id),
				C_DATA_SOURCE_ID.with(4),
				C_OBSERVATION_TIME.with(date),
				C_OBSERVATION_VALUE.with(d)
			);
		}

		public void delete(int id) {
		    deleteAll(C_TIME_SERIES_ID.is(id));
		}

		public void delete(String tsName) {
		    deleteAll(C_TIME_SERIES_ID.is(SERIES.C_TIME_SERIES_ID).and(SERIES.C_TIME_SERIES_NAME.is(tsName)));
		}


	}
	private static final DataTable DATA = new DataTable();
	
	public void testCanInsertOneRow() throws Exception {
		try {
			SERIES.insert("javaTestTs");
		} catch (RuntimeException e) {
			System.out.println("failed to inser.");
		}
	}
	
	public void testIdentity() throws Exception {
		SERIES.insert("test_row");
		int id = Db.identity();
		assertEquals(id, SERIES.id("test_row"));
	}

	public void testCanInsertMultipleColumns() throws Exception {
		SERIES.insert("javaTestTs");
		int id = SERIES.id("javaTestTs");
		DATA.insert(id, 178.0, new Timestamp(System.currentTimeMillis()));
		assertEquals(1, DATA.count(DATA.C_TIME_SERIES_ID.is(id)));
	}
	
	public void testCanUpdateOneRow() throws Exception {
		SERIES.insert("javaTestTs");
		SERIES.update("javaTestTs", "javaTestTs2");
		assertEquals(1, SERIES.count(SERIES.C_TIME_SERIES_NAME.is("javaTestTs2")));
		assertEquals(0, SERIES.count(SERIES.C_TIME_SERIES_NAME.is("javaTestTs")));
	}
	
	@Override protected void tearDown() throws Exception {
		DATA.delete("javaTestTs");
		SERIES.delete("javaTestTs");
		SERIES.delete("javaTestTs2");
		super.tearDown();
	}

}
