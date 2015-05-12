package malbec.fer;

import static org.testng.Assert.assertEquals;
import malbec.AbstractBaseTest;

import org.testng.annotations.Test;

public class OrderStatusTest extends AbstractBaseTest {

    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_CANCELREQUESTFAILED = "CANCELREQUESTFAILED";
    private static final String STATUS_CANCELREQUESTED = "CANCELREQUESTED";
    private static final String STATUS_CANCELREPLACEREQUESTFAILED = "CANCELREPLACEREQUESTFAILED";
    private static final String STATUS_CANCELREPLACEREQUESTED = "CANCELREPLACEREQUESTED";
    private static final String STATUS_DUPLICATE = "DUPLICATE";
    private static final String STATUS_FAILEDINSERT = "FAILEDINSERT";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_INVALID = "INVALID";
    private static final String STATUS_NEW = "NEW";
    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_DONFORDAY = "DONEFORDAY";
    
    private static final String STATUS_UNKNOWNORDER = "UNKNOWN";
    
    private static final String STATUS_EXECUTING = "EXECUTING";
    private static final String STATUS_PENDINGNEW = "PENDINGNEW";
    private static final String STATUS_PENDINGCANCEL = "PENDINGCANCEL";
    private static final String STATUS_FILLED = "FILLED";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_REPLACED = "REPLACED";
    private static final String STATUS_PLATFORMREJECTED = "PLATFORMREJECTED";
    private static final String STATUS_FERRETREJECTED = "FERRETREJECTED";
    

    private String[] STATUSES = { STATUS_DUPLICATE, STATUS_FAILED, STATUS_SENT, STATUS_INVALID,
            STATUS_UNKNOWNORDER, STATUS_NEW, STATUS_CANCELREQUESTFAILED, STATUS_CANCELREQUESTED,
            STATUS_CANCELREPLACEREQUESTFAILED, STATUS_CANCELREPLACEREQUESTED,
            STATUS_ACCEPTED, STATUS_FAILEDINSERT, STATUS_EXECUTING, STATUS_PENDINGNEW,
            STATUS_PENDINGCANCEL, STATUS_FILLED, STATUS_EXPIRED, STATUS_CANCELLED,
            STATUS_REPLACED, STATUS_PLATFORMREJECTED, STATUS_FERRETREJECTED, STATUS_DONFORDAY};

    @Test(groups = { "unittest" })
    public void testFromString() {

        assertEquals(OrderStatus.values().length, STATUSES.length, "Not testing all cases");
        
        for (String status : STATUSES) {
            OrderStatus os = OrderStatus.fromString(status);
            
            assertEquals(os.toString().toUpperCase(), status, "Failed to find match: " + status);
        }
        
    }

}
