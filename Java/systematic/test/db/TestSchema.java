package db;

import static util.Objects.*;
import db.clause.*;
import db.tables.TSDB.*;
import db.temptables.TSDB.*;
public class TestSchema extends DbTestCase {
	private SchemaTable table = Schema.table("TSDB..time_series_data");

	public void testCanRetrieveTableObjects() throws Exception {
		assertEquals("TSDB..time_series_data", table.qualifiedName());
		assertSize(4, table.columns());
	}

	public void testFileName() throws Exception {
		assertEquals("TSDB/TimeSeriesDataBase.java", table.fileName());
	}
	
	public void testTablesReturnsNonEmptyList() throws Exception {
		nonEmpty(Schema.tables("TSDB"));
	}
	
	public void testCanCreateTableObjects() throws Exception {
		SchemaTable securitySchemaTable = Schema.table("IvyDB..SECURITY");
		assertEquals("IvyDB..SECURITY", securitySchemaTable.qualifiedName());
		assertSize(9, securitySchemaTable.columns());
	}
	
	public void testCanCreateTempTableObjects() throws Exception {
		CcyTempTestBase ccyT = new CcyTempTestBase("foo");
		
		CcyBase ccy = new CcyBase("ccy");
		ccy.select(Clause.TRUE).intoTemp(ccyT.name());
		assertEquals(ccy.count(ccy.C_CCY_NAME.is("usd")), ccyT.count(ccyT.C_CCY_NAME.is("usd")));
	}

	public void testCanCreateTempTableJoin() throws Exception {		
		CcyBase ccy = new CcyBase("ccy");
		CcyPairBase pair = new CcyPairBase("pair");
		SelectMultiple select = ccy.select(pair.C_CCY_ID1.is(ccy.C_CCY_ID));
		select.add(pair.C_CCY_PAIR_NAME);
		select.add(pair.C_CCY_PAIR_ID);
		select.intoTemp("pair_w_ccy_name");
//		new Generator().writeFile(temp.schemaTable(), "temptables");
		PairWCcyNameBase wName = new PairWCcyNameBase("wname");
		int originalPairCount = pair.count(pair.C_CCY_PAIR_NAME.like("usd%"));
		int tempPairCount = wName.count(wName.C_CCY_PAIR_NAME.like("usd%"));
		assertEquals(tempPairCount, originalPairCount);
	}
}
