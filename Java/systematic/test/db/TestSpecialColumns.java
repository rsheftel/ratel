package db;

import static db.clause.Clause.*;
import static db.columns.BinaryOperatorColumn.*;
import static db.columns.ConstantColumn.*;
import static db.tables.PerformanceDB.InvestorDataBase.*;
import static db.tables.PerformanceDB.InvestorDataDefinitionBase.*;
import static db.tables.TSDB.CcyBase.*;

import java.math.*;

import db.clause.*;
import db.columns.*;
import db.tables.PerformanceDB.*;
import db.tables.SystemDB.*;
import db.tables.TSDB.*;

public class TestSpecialColumns extends DbTestCase {

	public void testCanInsertSelectAndWhereBitColumns() throws Exception {
		CcyPairBase pair = new CcyPairBase("pair");
		pair.insert(
			pair.C_CCY_ID1.with(48),
			pair.C_CCY_ID2.with(48),
			pair.C_CCY_PAIR_NAME.with("testpair"),
			pair.C_IS_ACTIVE.with(true)
		);
		Clause isTest = pair.C_CCY_PAIR_NAME.is("testpair");
		assertTrue(pair.C_IS_ACTIVE.value(isTest));
		assertTrue(pair.C_IS_ACTIVE.value(isTest.and(pair.C_IS_ACTIVE.is(true))));
		assertEquals(0, pair.count(isTest.and(pair.C_IS_ACTIVE.is(false))));
	}
	
	public void testCanUseRealColumns() throws Exception {
	    assertNotNull(PnlBase.T_PNL.C_PNL.max().value(TRUE));
    }
	
	public void testCanUseDecimalColumns() throws Exception {
	    InvestorDataDefinitionBase DEF = T_INVESTORDATADEFINITION;
	    InvestorDataBase DATA = T_INVESTORDATA;

	    DEF.insert(DEF.C_DESCINVDATADEF.with("testCanUseDecimalAndRealColumns"));
	    int minDefId = DEF.C_INVDATADEFID.min().value(TRUE);
        BigDecimal nearLargest = new BigDecimal("9999999999999.99");
        DATA.insert(
	        DATA.C_DATE.now(),
	        DATA.C_INVDATADEFID.with(minDefId),
	        DATA.C_VALUE.with(nearLargest)
	    );
        assertEquals(nearLargest, DATA.C_VALUE.max().value(TRUE));
    }

	public void testNTextColumns() throws Exception {
		SystemBase t = SystemBase.T_SYSTEM;
		assertEquals(0, t.count(t.C_DESCRIPTION.like("jeff")));	
		t.insert(
			t.C_NAME.with("jeff"),
			t.C_DESCRIPTION.with("jeff"),
			t.C_DOCUMENTATION.with("jeff"),
			t.C_OWNER.with("jeff")
		);
		assertEquals(1, t.count(t.C_DESCRIPTION.like("jeff")));	
		assertEquals(1, t.count(t.C_DESCRIPTION.is("jeff")));			
	}
	
	public void testPlus() throws Exception {
		Clause matches = T_CCY.C_CCY_NAME.is("usd");
		int id = T_CCY.C_CCY_ID.value(matches);
		assertEquals(new Integer(id * 2), plus(T_CCY.C_CCY_ID, T_CCY.C_CCY_ID).value(matches));
	}
	
	public void testConstantColumn() throws Exception {
		Clause matches = T_CCY.C_CCY_NAME.is("usd");
		int id = T_CCY.C_CCY_ID.value(matches);
		Column<Integer> ten = constant(10);
		assertEquals(new Integer(id * 10), times(T_CCY.C_CCY_ID, ten).value(matches));
		assertEquals(new Integer(id), T_CCY.C_CCY_ID.value(T_CCY.C_CCY_ID.is(minus(plus(T_CCY.C_CCY_ID, ten), ten)).and(matches)));
	}
	
	public void testConcatConcat() throws Exception {
	    ConcatenationColumn col = new ConcatenationColumn(T_CCY.C_CCY_NAME, "::").plus(T_CCY.C_CCY_NAME);
	    col.select(TRUE).values();
	    
    }
	
	
}
