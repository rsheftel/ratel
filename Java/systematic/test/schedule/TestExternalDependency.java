package schedule;
import static schedule.JobTable.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import schedule.JobTable.*;
import schedule.dependency.*;

public class TestExternalDependency extends AbstractJobTest {

    public void testExternalDependencyReleasedByMain() throws Exception {
    
        Job waits = JOBS.insert("waits", new RunMe(), THREE_PM);
        External.create(waits, "for me");
        Date date = date("2008/02/15");
        assertFalse(waits.needsRun(date));
        External.main(array("-name", "for me", "-status", "SUCCESS", "-date", "2008/02/15"));
        assertTrue(waits.needsRun(date));
        
    }
    
}
