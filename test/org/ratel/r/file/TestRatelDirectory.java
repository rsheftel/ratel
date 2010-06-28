package org.ratel.r.file;

import junit.framework.*;

public class TestRatelDirectory extends TestCase {

    private RatelDirectory directory = new RatelDirectory("foo");

    @Override protected void setUp() throws Exception {
        if(directory.exists()) directory.destroy();
    }

    @Override protected void tearDown() throws Exception {
        if(directory.exists()) directory.destroy();
    }

    public void testRemoveAllFiles() throws Exception {
        directory.create();
        new RatelFile("foo/a1").create("I am text");
        new RatelFile("foo/a2").create("No, I am text");
        new RatelDirectory("foo/d1").create();
        assertEquals(3, directory.size());
        directory.removeAllFiles();
        assertEquals(1, directory.size());
        directory.destroy();
        directory.requireNotExists();
    }
}
