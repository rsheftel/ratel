package db;

import static util.Objects.*;
import tsdb.*;
import db.columns.*;
import db.tables.TSDB.*;
public class TestTable extends DbTestCase {
    private static final long serialVersionUID = 1L;	
	private static final TempMonthBase TEMP = TempMonthBase.T_MONTH;

    public void testCanReadOneRowFromDataSourceTable() throws Exception {
		DataSourceBase source = new DataSourceBase("source");
		VarcharColumn name = source.C_DATA_SOURCE_NAME;
		Row row = source.row(name.is("test"));
		assertEquals("test", name.value(row));
		assertEquals(new Integer(4), source.C_DATA_SOURCE_ID.value(row));
	}

	public void testAlternativeArrangement2() throws Exception {
		class SourceTable extends DataSourceBase {
		    private static final long serialVersionUID = 1L;
		    SourceTable() { super("testSource"); }
			int idByName(String name) {
				Row row = row(C_DATA_SOURCE_NAME.is(name));
				return C_DATA_SOURCE_ID.value(row);
			}
		}
		SourceTable source = new SourceTable();
		assertEquals(4, source.idByName("test"));
	}
	
	public void testSimpleAnd() throws Exception {
		TimeSeries series = new TimeSeries("aapl close");
		DataSource bogus = new DataSource("bogus");
		int seriesId = series.id();
		int source = bogus.id();
		TimeSeriesDataBase t = new TimeSeriesDataBase("data");
		assertEquals(7, t.rows(t.C_DATA_SOURCE_ID.is(source).and(t.C_TIME_SERIES_ID.is(seriesId))).size());
		assertEquals(7, series.observations(bogus).size());
	}
	
	static class TempMonthBase extends Table {
	    private static final long serialVersionUID = 1L;
	    public static final TempMonthBase T_MONTH = new TempMonthBase("monthbase");

	    public TempMonthBase(String alias) { super("TSDB..tempmonth", alias); }

	    public IntColumn C_NUMBER = new IntColumn("number", "int identity", this, NOT_NULL);
	    public NvarcharColumn C_NAME = new NvarcharColumn("name", "nvarchar(10)", this, NOT_NULL);
	    public NvarcharColumn C_NAME_NULLABLE = new NvarcharColumn("namenull", "nvarchar(10)", this, NULL);
	    public NcharColumn C_FUTURES_LETTER = new NcharColumn("futures_letter", "nchar", this, NOT_NULL);

	} 
	
	public void testCreateTable() throws Exception {
	    SchemaTable table = TEMP.schemaTable();
        assertFalse(table.exists());
	    table.create();
	    assertTrue(table.exists());
	    TEMP.insert(
	        TEMP.C_NAME.with("hi"), 
	        TEMP.C_FUTURES_LETTER.with("j")
	    );
	    try {
	        TEMP.insert(
	            TEMP.C_FUTURES_LETTER.with("j")
	        );
	        fail("");
        } catch (Exception success) {
            assertMatches("sql failed", success);
        }
	    assertMatches(1, the(TEMP.C_NUMBER.values()));
    }
	
	@Override protected void tearDown() throws Exception {
	    TEMP.schemaTable().destroyIfExists();
	    super.tearDown();
	}
	
}
