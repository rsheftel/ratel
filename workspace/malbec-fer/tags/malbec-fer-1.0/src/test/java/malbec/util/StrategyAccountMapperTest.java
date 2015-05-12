package malbec.util;

import static org.testng.Assert.*;


import org.testng.annotations.Test;

public class StrategyAccountMapperTest {

    @Test(groups = { "unittest" })
    public void testMapping() {

        // construct without initializing
        StrategyAccountMapper sam = new StrategyAccountMapper();
        
        int resultCount = sam.initialize();
        
        assertTrue(resultCount > 0, "Failed to read mappings");
        
        String account = sam.lookupAccount("TS", "testF", "Futures");
        assertNotNull(account, "Failed to find test mapping");
        assertEquals("SIM6918", account, "Test mapping does not contain expected result");
        
        sam.addMapping("Test", "Test.SAM", "Equity", "MyAccount");
        String tmpAccount = sam.lookupAccount("Test", "Test.SAM", "Equity");
        assertNotNull(tmpAccount, "Failed to find test mapping");
        assertEquals("MyAccount", tmpAccount, "Test mapping does not contain expected result");
        
        
    }
}
