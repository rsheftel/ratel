package malbec.fer.mapping;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class RouteAccountMapperTest {

    @Test(groups = { "unittest" })
    public void testMapping() {

        // construct without initializing
        RouteAccountMapper ram = new RouteAccountMapper();
        
        int resultCount = ram.initialize();
        
        assertTrue(resultCount > 0, "Failed to read mappings");

        String account = ram.lookupAccount("EMSX", "MANS", "EQUITY");
        assertNotNull(account, "Failed to find test mapping");
        assertEquals("MQF", account, "Test mapping does not contain expected result");
        
        ram.addMapping("Test", "TEST", "MyAccount", "FUTURES");
        String tmpAccount = ram.lookupAccount("Test", "TEST", "FUTURES");
        assertNotNull(tmpAccount, "Failed to find test mapping");
        assertEquals("MyAccount", tmpAccount, "Test mapping does not contain expected result");
        
        int count = ram.reload();
        assertTrue(count > 0, "Failed to re-load");
    }
}
