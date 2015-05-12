package systemdb.live;

import static db.clause.Clause.*;
import static util.Dates.*;
import systemdb.live.ExecutionConfigurationTable.*;
import db.*;
import static systemdb.live.ExecutionConfigurationTable.*;

public class TestExecutionConfigTable extends DbTestCase {

    @Override protected void setUp() throws Exception {
        super.setUp();
        CONFIG.deleteAll(TRUE);
    }
    
    public void testCanGetCurrentConfig() throws Exception {
        freezeNow("2009/07/22");
        
        CONFIG.insert("baz", "foo", "bar");
        assertConfig("baz", "foo", "bar");
        
        freezeNow("2009/07/23");
        CONFIG.insert("secondtype", "second", "secondroute");
        
        assertConfig("secondtype", "second", "secondroute");
        assertConfig("baz", "foo", "bar");
        
        CONFIG.insert("baz", "newFoo", "newBar");

        assertConfig("secondtype", "second", "secondroute");
        assertConfig("baz", "newFoo", "newBar");
        
    }

    private void assertConfig(String type, String expectedPlatform, String expectedRoute) {
        Configuration config = ExecutionConfigurationTable.currentConfiguration(type);
        assertEquals(expectedPlatform, config.platform());
        assertEquals(expectedRoute, config.route());
        assertEquals(type, config.type());
    }

}
