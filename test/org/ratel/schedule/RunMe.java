package org.ratel.schedule;

import static org.ratel.schedule.JobStatus.*;
import static org.ratel.util.Errors.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;

class RunMe implements Schedulable {
    @Override public JobStatus run(Date asOf, Job item) {
        bombUnless(item.status(asOf).inProgress(), "item status not in progress");
        AbstractJobTest.run.put(item.name(), asOf);
        return SUCCESS;
    }
    
}