package db;

import db.columns.*;
import db.tables.TSDB.*;

public class TestUnaryFunctionColumn extends DbTestCase {

	public static final CcyBase CCY = new CcyBase("ccy");
	
	public void testCanSelectWithFunction() throws Exception {
		FunctionColumn<String> upper = CCY.C_CCY_NAME.upper();
		assertEquals("USD", upper.value(upper.is("USD")));
	}

	public void testCaselessIs() throws Exception {
		assertEquals("usd", CCY.C_CCY_NAME.value(CCY.C_CCY_NAME.isWithoutCase("USD")));
		assertEquals(0, CCY.count(CCY.C_CCY_NAME.is("USD")));
	}
	
}
