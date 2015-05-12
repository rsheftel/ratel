package file;

import junit.framework.*;

public class TestDirectory extends TestCase {
	
	private QDirectory directory = new QDirectory("foo");

	@Override protected void setUp() throws Exception {
		if(directory.exists()) directory.destroy();
	}
	
	@Override protected void tearDown() throws Exception {
		if(directory.exists()) directory.destroy();
	}
	
	public void testRemoveAllFiles() throws Exception {
		directory.create();
		new QFile("foo/a1").create("I am text");
		new QFile("foo/a2").create("No, I am text");
		new QDirectory("foo/d1").create();
		assertEquals(3, directory.size());
		directory.removeAllFiles();
		assertEquals(1, directory.size());
		directory.destroy();
		directory.requireNotExists();
	}
}
