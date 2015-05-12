package db;

import static db.Column.*;
import static db.clause.Clause.*;
import static java.util.Collections.*;
import static util.Objects.*;

import java.util.*;

import db.clause.*;
import db.columns.*;
import db.tables.TSDB.*;
public class TestSelect extends DbTestCase {
	private static final CcyPairBase PAIR2 = new CcyPairBase("pair2");

	private static final class CcyWithBadColumn extends Table {
        private static final long serialVersionUID = 1L;
        public IntIdentityColumn C_CCY_ID = new IntIdentityColumn("ccy_id", "int identity", this, NOT_NULL);
        public VarcharColumn C_CCY_NAME = new VarcharColumn("ccy_name", "varchar(200)", this, NOT_NULL);
        public FloatColumn C_PRECEDENCE = new FloatColumn("precedence", "float(53)", this, NOT_NULL);
        public VarcharColumn C_DESCRIPTION = new VarcharColumn("description", "varchar(200)", this, NULL);
        public VarcharColumn C_DESCRIPTION2 = new VarcharColumn("description2", "varchar(200)", this, NULL);
        private CcyWithBadColumn(String name) { super("TSDB.." + name, "currency2"); }
        IntIdentityColumn idCol() { return C_CCY_ID; }
    }

    private static class CurrencyTable extends CcyBase {
	    private static final long serialVersionUID = 1L;
	    private CurrencyTable(String alias) {
			super(alias);
		}
	}

	private static final TimeSeriesBase SERIES = new TimeSeriesBase("ts");
	private static final TimeSeriesBase SERIES2 = new TimeSeriesBase("ts2");
	private static final CcyBase CURRENCY = new CurrencyTable("ccy");
	private static final CcyPairBase PAIR = new CcyPairBase("ccy_pair");
	
	
	public void testSelectWithMissingButUnusedColumn() throws Exception {
	    CcyWithBadColumn t = new CcyWithBadColumn("ccy");
	    SelectMultiple select = t.select(TRUE);
	    select.orderBy(t.C_CCY_ID.ascending());
        Row example = first(select.rows());
	    assertMatches(40, example.value(t.idCol()));
	    try {
	        example.value(t.C_DESCRIPTION2);
	        fail();
	    } catch (Exception e) {
	        assertMatches("no column currency2.description2", e);
	    }
    }
	
	public void testCrossJoinCheckNoJoin() throws Exception {
		Clause nameMatches = SERIES.C_TIME_SERIES_NAME.is("foo");
		Select s = new SelectOne<String>(SERIES.C_TIME_SERIES_NAME, nameMatches, false);
		assertFalse(s.isCrossJoin());
	}
	
	public void testCrossJoinCheckGoodOne() throws Exception {
		Clause nameMatchesSelf = SERIES.C_TIME_SERIES_NAME.is(SERIES2.C_TIME_SERIES_NAME);
		Select s = new SelectOne<String>(SERIES.C_TIME_SERIES_NAME, nameMatchesSelf, false);
		assertFalse(s.isCrossJoin());
	}
	
	public void testCrossJoinCheckCausedBySelect() throws Exception {
		Clause nameMatches = SERIES.C_TIME_SERIES_NAME.is("foo");
		Select s = new SelectOne<String>(SERIES2.C_TIME_SERIES_NAME, nameMatches, false);
		assertTrue(s.isCrossJoin());		
	}

	public void testCrossJoinCheckCausedByWhere() throws Exception {
		Clause nameMatches = SERIES.C_TIME_SERIES_NAME.is("foo");
		Clause name2Matches = SERIES2.C_TIME_SERIES_NAME.is("foo");
		Select s = new SelectOne<String>(SERIES.C_TIME_SERIES_NAME, nameMatches.and(name2Matches), false);
		assertTrue(s.isCrossJoin());		
	}
	
	public void testCrossJoinsDetectedCorrectlyWhenJoinedOnlyInSubquery() throws Exception {
		Clause pairMatches = PAIR.C_CCY_ID1.is(CURRENCY.C_CCY_ID);
		pairMatches = pairMatches.and(PAIR2.C_CCY_ID1.is(PAIR.C_CCY_ID1));
		assertContains("sit", CURRENCY.C_CCY_NAME.values(PAIR.notExists(pairMatches)));

		CURRENCY.C_CCY_NAME.values(PAIR.notExists(PAIR.C_CCY_ID1.is(PAIR2.C_CCY_ID1)));
		try {
			CURRENCY.C_CCY_NAME.values(PAIR.notExists(PAIR.C_CCY_ID1.is(7).and(PAIR2.C_CCY_ID1.is(7))));
			fail("unexpected success");
		} catch (RuntimeException success) {
			assertMatches("cross .*", success);
		}
	}

	
	public void testSubSelectOuterTable() throws Exception {
		assertContains("sit", CURRENCY.C_CCY_NAME.values(PAIR.notExists(PAIR.C_CCY_ID1.is(CURRENCY.C_CCY_ID))));
	}
	
	public void testLikeColumnWorks() throws Exception {
		assertEquals("usd", CURRENCY.C_CCY_NAME.value(CURRENCY.C_CCY_NAME.like("us%")));
	}
	
	public void testIsNotClause() throws Exception {
		int notUsd = CURRENCY.count(CURRENCY.C_CCY_NAME.isNot("usd"));
		int all = CURRENCY.count(TRUE);
		assertEquals(all, notUsd + 1);
	}
	
	public void testOrClause() throws Exception {
		int isAndIsNot = CURRENCY.count(CURRENCY.C_CCY_NAME.is("usd").or(CURRENCY.C_CCY_NAME.isNot("usd")));
		int all = CURRENCY.count(TRUE);
		assertEquals(all, isAndIsNot);
	}

	public void testClauseWithParenGrouping() throws Exception {
		Clause isUsd = CURRENCY.C_CCY_NAME.is("usd");
		Clause isCop = CURRENCY.C_CCY_NAME.is("cop");
		Clause isNotCop = CURRENCY.C_CCY_NAME.isNot("cop");
		int all = CURRENCY.count(TRUE);

		int parenSecond = CURRENCY.count(isUsd.and(parenGroup(isCop.or(isNotCop))));
		int parenFirst = CURRENCY.count(parenGroup(isUsd.and(isCop)).or(isNotCop));

		assertEquals(1, parenSecond);
		assertEquals(all - 1, parenFirst);
	}
	
	public void testSelectDistinct() throws Exception {
		List<Row> rows = PAIR.select(columns(PAIR.C_CCY_ID1), Clause.TRUE).rows();
		List<Integer> ids = ids(rows);
		Set<Integer> uniqueIds = new HashSet<Integer>(ids);
		assertFalse(ids.size() == uniqueIds.size());
		List<Row> distinctRows = PAIR.selectDistinct(columns(PAIR.C_CCY_ID1), Clause.TRUE).rows();
		List<Integer> distinctIds = ids(distinctRows);
		Set<Integer> uniqueDistinctIds = new HashSet<Integer>(distinctIds);
		assertTrue(distinctIds.size() == uniqueDistinctIds.size());
	}
	
	public void testSelectToTemp() throws Exception {
		SelectMultiple s = CURRENCY.select(TRUE);
		Table t = s.intoTemp("temp");
		t.deleteAll(t.column(CURRENCY.C_CCY_NAME).isNot("usd"));
		assertEquals(1, t.count(t.column(CURRENCY.C_CCY_NAME).is("usd")));
	}

	private List<Integer> ids(List<Row> rows) {
		List<Integer> ids = empty();
		for (Row r : rows) 
			ids.add(r.value(PAIR.C_CCY_ID1));
		return ids;
	}
	
	public void testInClauseSupportsMoreThan2000Choices() throws Exception {
		CURRENCY.C_CCY_ID.values(CURRENCY.C_CCY_ID.in(nCopies(2001, 1)));
	}
	
	public void testGroupBySelect() throws Exception {
		SelectMultiple select = PAIR.select(columns(PAIR.C_CCY_ID1, PAIR.countColumn()), TRUE);
		select.groupBy(PAIR.C_CCY_ID1);
		SelectOne<Integer> distinctCcyId1 = PAIR.C_CCY_ID1.select(TRUE, true);
		assertEquals(distinctCcyId1.values().size(), select.rows().size());
	}
	
	public void testOrderBySelect() throws Exception {
	    SelectMultiple select = CURRENCY.select(TRUE);
	    select.top(2, CURRENCY.C_CCY_ID.ascending());
	    List<Row> rows = select.rows();
	    assertMatches(40, first(rows).value(CURRENCY.C_CCY_ID)); // usd
	    select = CURRENCY.select(TRUE);
	    select.top(1, CURRENCY.C_CCY_ID.descending());
	    rows = select.rows();
	    assertTrue(first(rows).value(CURRENCY.C_CCY_ID) >= 2354); // sar
	}
	
}