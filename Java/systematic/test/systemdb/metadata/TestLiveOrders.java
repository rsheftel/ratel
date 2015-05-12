package systemdb.metadata;

import static systemdb.metadata.LiveOrders.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import systemdb.metadata.LiveOrders.*;
import db.*;

public class TestLiveOrders extends DbTestCase {

    public void testLiveOrdersDoNotAppearInQueryResultsAfterWeek() throws Exception {
        int discard = fakeOrder(daysAgo(7, now()));
        int keep = fakeOrder(daysAgo(6, now()));
        Set<Integer> ids = emptySet();
        for(LiveOrder order : ORDERS.ordersFilled(39, "TY.1C")) ids.add(order.id());
        assertDoesNotContain(discard, ids);
        assertContains(keep, ids);
    }
    
    private int fakeOrder(Date date) {
        return ORDERS.insert(39, "TY.1C", date, null, "Enter", "long", 7, 19.5, "market", "enter", "NYWS007", "TEST", null);
    }

    private int fakeSubmission(Date submittedAt) {
        return ORDERS.insert(39, "TY.1C", null, submittedAt, "Enter", "long", 7, null, "market", "enter", "NYWS007", "TEST", null);
    }
    
    public void testOrderUpdateFill() throws Exception {
        int id = fakeSubmission(now());
        LiveOrder order = ORDERS.order(id);
        order.updateFill(17, midnight());
        assertEquals(17.0, order.price());
        assertEquals(midnight(), order.simFillTime());
        order = ORDERS.order(id);
        assertEquals(17.0, order.price());
        assertEquals(midnight(), order.simFillTime());
    }
    
    public void testMaxIdBeforeToday() throws Exception {
        freezeNow("2006/01/01");
        assertEquals(0, ORDERS.maxIdBeforeToday());
        Date yesterday = midnight(yesterday());
        fakeOrder(timeOn("23:58:58", yesterday));
        int lastYesterday = fakeOrder(timeOn("23:58:59", yesterday));
        fakeOrder(midnight());
        fakeSubmission(midnight());
        assertEquals(lastYesterday, ORDERS.maxIdBeforeToday());
        lastYesterday = fakeSubmission(timeOn("23:59:00", yesterday));
        assertEquals(lastYesterday, ORDERS.maxIdBeforeToday());
    }
    
}
