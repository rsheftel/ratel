package org.ratel.schedule;

import static org.ratel.schedule.JobTable.*;
import static org.ratel.schedule.Schedulable.*;
import static org.ratel.util.Objects.*;
import org.ratel.schedule.JobTable.*;

public class TestJobParameters extends AbstractJobTest {
    public void testSetTempParameters() throws Exception {
        Job ready = JOBS.insert("all ready", EMAIL, FOUR_PM);
        ready.setParameters(map("subject", "ima subject"));
        ready.setTempParameter("missing", "true");
        assertEquals("ima subject", ready.parameter("subject"));
        assertEquals("true", ready.parameter("missing", "default"));
        
        Job fromDb = JOBS.forName("all ready");
        assertEquals("ima subject", fromDb.parameter("subject"));
        assertEquals("default", fromDb.parameter("missing", "default"));
        
    }
}
