package db;

import static tsdb.TimeSeriesTable.*;
import static util.Objects.*;

import java.util.*;

import db.clause.*;


public class TestBatchInsert extends DbTestCase {

	private Table tempTable;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tempTable = TIME_SERIES.createTemp("temp");
	}
	
	public void testBatchInsert() throws Exception {
		checkInsertRows(20001);
	}

	public void testBatchInsertWithExactBatchSize() throws Exception {
		checkInsertRows(10000);
	}
	
	public void testBatchInsertWithNoRows() throws Exception {
		try {
			checkInsertRows(0);
			fail();
		} catch (RuntimeException success) {
			assertMatches("zero rows", success);
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		tempTable.destroy();
		super.tearDown();
	}
	private void checkInsertRows(int nRows) {
		Column<String> name = tempTable.column(TIME_SERIES.C_TIME_SERIES_NAME);
		Column<String> table = tempTable.column(TIME_SERIES.C_DATA_TABLE);
		List<Row> rows = empty();
		for(int i = 0; i < nRows; i++)
			rows.add(new Row(name.with("" + i), table.with("test")));
		tempTable.insert(rows);
		assertEquals(rows.size(), tempTable.count(Clause.TRUE));
	}
}
