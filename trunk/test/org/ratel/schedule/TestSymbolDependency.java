package org.ratel.schedule;

import static org.ratel.schedule.JobTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.schedule.dependency.*;

public class TestSymbolDependency extends AbstractJobTest {

    public void testSymbolDependency() throws Exception {
        Date asOfTime = date("2008/04/18");
        Job job = JOBS.insert("test job", new DoNothing(), FOUR_PM);
        AllSymbolsReady.create(list("TY.1C", "RE.TEST.TY.1C"), job);
        assertTrue(job.needsRun(asOfTime));
        asOfTime = date("2008/04/21");
        assertFalse(job.needsRun(asOfTime));
        
    }

}
