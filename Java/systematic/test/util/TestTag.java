package util;

import static util.Tag.*;

public class TestTag extends Asserts {
    public void testTag() throws Exception {
        Tag html = tag("html");
        assertEquals("html", html.name());
        assertEquals("<html />", html.xml());
        html.add("head");
        assertEquals("<html><head /></html>", html.xml());
        assertEquals(html, parse(html.xml()));
        assertEquals("head", html.child("head").name());
        try { 
            html.child("doesNotExist");
            fail("no child");
        } catch (Exception e) {
            assertMatches("single child not found: doesNotExist", e);
            assertMatches("empty list passed to the", e.getCause());
        }
        html.add("head");
        try { 
            html.child("head");
            fail("too many child");
        } catch (Exception e) {
            assertMatches("single child not found: head", e);
            assertMatches("the of multiple", e.getCause());
        }
        html.add("body", "withsome text");
        assertEquals("withsome text", html.child("body").text());
        html.child("body").delete();
        assertFalse(html.hasChild("body"));
        
    }
}
