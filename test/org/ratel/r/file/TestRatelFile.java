package org.ratel.r.file;

import static org.ratel.util.Asserts.*;
import junit.framework.*;

public class TestRatelFile extends TestCase {


    private RatelFile file = new RatelFile("somefile");
    private RatelFile otherfile = new RatelFile("someotherfile");

    @Override protected void setUp() throws Exception {
        if(file.exists()) file.delete();
        if(otherfile.exists()) otherfile.delete();
    }

    @Override protected void tearDown() throws Exception {
        if(file.exists()) file.delete();
        if(otherfile.exists()) otherfile.delete();
    }

    public void testCanCreateATextFile() throws Exception {
        assertFileText("this is some file text\nwith2Lines");
    }

    public void testWithLastCharacterCarriageReturn() throws Exception {
        assertFileText("this is some file text\nwith2Lines\n");
    }

    private void assertFileText(String text) {
        String expectedText = text;
        file.create(expectedText);
        assertEquals(expectedText, file.text());
    }

    public void testCreateExistingFileFails() throws Exception {
        file.create("foo");
        try {
            file.create("foo");
            fail();
        } catch (RuntimeException success) {
            assertMatches("already exists", success);
        } finally {
            file.delete();
        }
    }

    public void testFileCopy() throws Exception {
        String text = "I am not a file";
        otherfile.create(text);
        otherfile.copyTo(file);
        assertEquals(text, file.text());
        assertTrue(otherfile.exists());
    }

   public void testFileMove() throws Exception {
       String text = "I am not a file";
       otherfile.create(text);
       otherfile.moveTo(file);
       assertEquals(text, file.text());
       assertFalse(otherfile.exists());
    }

}
