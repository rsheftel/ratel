package tsdb;
import static tsdb.Attribute.*;
import static util.Objects.*;

import java.util.*;

import db.*;

public class TestAttributeValue extends DbTestCase {
	public void testAttributeValueId() throws Exception {
		assertEquals(4, QUOTE_TYPE.value("close").id());
		assertEquals(1, TICKER.value("aapl").id());
		assertEquals(105169, SECURITY_ID.value("105169").id());
	}
	
	public void testInsertAttributeValue() throws Exception {
		AttributeValue value = QUOTE_TYPE.value("test_value_zzClose");
		value.create();
		assertTrue(value.exists());
	}
	
	public void testFromTimeSeries() throws Exception {
		TimeSeries series = new TimeSeries("aapl close");
		Set<AttributeValue> values = emptySet();
		for (AttributeValue value : series.attributes())
            values.add(value);
		Set<AttributeValue> expected = emptySet();
		expected.add(TICKER.value("aapl"));
		expected.add(QUOTE_TYPE.value("close"));
		assertEquals(expected, values);
	}
	
	public void testExistWorksOnNonStringNameColumns() throws Exception {
		assertTrue(SECURITY_ID.valuesExist(list("101569", "5001")));
		assertFalse(SECURITY_ID.valuesExist(list("4000")));
		assertFalse(SECURITY_ID.valuesExist(list("101569", "4000")));
	}
}
