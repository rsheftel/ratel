package file;

import static util.Objects.*;

import java.util.*;

import systemdb.*;

public class TestCsv extends FileGeneratorTestCase {
	
	private static final String FILE = "foo.csv";
	
	private final QFile file = toDir.file(FILE);
	
	@Override protected void setUp() throws Exception {
	    super.setUp();
	    toDir.create();
	}
	
	public void testBasicCsvParsing() throws Exception {
		file.append("foo,bar,baz\n");
		file.append("foo,bar,baz\n");
		file.append("\n");
		List<List<String>> records = records();
		assertSize(2, records);
		assertEquals("[[foo, bar, baz], [foo, bar, baz]]", records.toString());
	}

	public void testBasicCsvParsingWithHeader() throws Exception {
	    file.append("Foo, Bar, Baz\n");
	    file.append("foo1,bar1,baz1\n");
	    file.append("foo2,  bar3,baz5\n");
	    Csv csv = new Csv(file, true);
	    List<List<String>> records = csv.records();
        assertSize(2, records);
	    assertEquals("[[foo1, bar1, baz1], [foo2,   bar3, baz5]]", records.toString());
	    assertEquals(list("Foo", "Bar", "Baz"), csv.columns());
	    assertEquals("foo1", csv.value("Foo", first(records)));
	    assertEquals(2, csv.count());
	}
	
	public void testHeaderMethod() {
	    file.append("H1, H2, H3\n");
	    assertEquals(list("H1", "H2", "H3"), file.csvHeader());
	}
	
	public void testCsvParsingWithQuotedString() throws Exception {
		file.append("foo,\"bar\",baz\n");
		List<List<String>> records = records();
		assertSize(1, records);
		assertEquals("[[foo, bar, baz]]", records.toString());
	}

	public void testCsvParsingWithQuoteInString() throws Exception {
		file.append("foo,\"'\"\"'\",baz\n");
		List<List<String>> records = records();
		assertSize(1, records);
		assertEquals("[[foo, '\"', baz]]", records.toString());
	}
	
	public void testBasicCsvParsingWithOneColumn() throws Exception {
		file.append("foo bar\n");
		file.append("fooboz\n");
		List<List<String>> records = records();
		assertSize(2, records);
		assertEquals("[[foo bar], [fooboz]]", records.toString());
	}
	
	public void testCsvSplit() throws Exception {
	    file.append("header1,header2\n");
	    file.append("content11,content12\n");
	    file.append("content21,content22\n");
	    file.append("content31,content32\n");
	    Csv toSplit = new Csv(file, true);
	    toSplit.split(1, toDir, "foo");
	    
	    Csv first = new Csv(toDir.file("foo-1.csv"), true);
	    assertSize(1, first.records());
	    Csv second = new Csv(toDir.file("foo-2.csv"), true);
	    assertSize(1, second.records());
	    Csv third = new Csv(toDir.file("foo-3.csv"), true);
	    assertSize(1, third.records());
	    
	    toSplit.split(2, toDir, "foo");
        first = new Csv(toDir.file("foo-1.csv"), true);
        assertSize(2, first.records());
        second = new Csv(toDir.file("foo-2.csv"), true);
        assertSize(1, second.records());
        
        toSplit = new Csv(file);
        toSplit.split(2, toDir, "foo");
        first = new Csv(toDir.file("foo-1.csv"), false);
        assertSize(2, first.records());
        second = new Csv(toDir.file("foo-2.csv"), false);
        assertSize(2, second.records());
    }
	
	private List<List<String>> records() {
		return new Csv(file).records();
	}
	
}
