package systemdb.metadata;

import static util.Dates.*;
import static util.Objects.*;
import db.*;

public class TestLiveSystem extends DbTestCase {
    
    public void testTag() throws Exception {
        assertEquals("QF.Example", new LiveSystem(new Siv("TestSystem1", "daily", "1.0"), new Pv("Fast")).bloombergTag());
    }
    
    public void testAddLiveMarket() throws Exception {
        LiveSystem system = new LiveSystem(new Siv("TestSystem1", "daily", "1.0"), new Pv("Pv"));
        system.addLiveMarket("TY.1C", midnight(), null);
        assertEquals(new Market("TY.1C"), the(system.markets()));
    }
}
