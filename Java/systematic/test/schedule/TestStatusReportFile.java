package schedule;

import junit.framework.*;
import static schedule.StatusReportFile.*;
public class TestStatusReportFile extends TestCase {

    public void testDowngrade() throws Exception {
        
        assertEquals(RED, StatusReportFile.downgrade(RED, GREEN));
        assertEquals(RED, StatusReportFile.downgrade(GREEN, RED));
        assertEquals(RED, StatusReportFile.downgrade(RED, RED));
        assertEquals(WHITE, StatusReportFile.downgrade(WHITE, GREEN));
        assertEquals(YELLOW, StatusReportFile.downgrade(YELLOW, GREEN));
        assertEquals(RED, StatusReportFile.downgrade(YELLOW, RED));
        assertEquals(WHITE, StatusReportFile.downgrade(GREEN, WHITE));
    }
}
