package db;


public class SchemaColumnTest extends DbTestCase {

	public void testColumnClassWorks() throws Exception {
		assertEquals("IntIdentityColumn", column("int() identity").columnClass());
		assertEquals("IntColumn", column("int").columnClass());
		assertEquals("NumericColumn", column("numeric()").columnClass());
		assertEquals("NumericColumn", column("numeric").columnClass());
		assertEquals("NumericIdentityColumn", column("numeric() identity").columnClass());
	}

	private SchemaColumn column(String type) {
		StringRow definition = new StringRow();
		definition.put("TYPE_NAME", type);
		definition.put("COLUMN_SIZE", "0");
		definition.put("DECIMAL_DIGITS", "0");
		SchemaColumn column = new SchemaColumn(definition);
		return column;
	}
	
}
