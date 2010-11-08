package org.ratel.schedule;

import static org.ratel.schedule.JobStatus.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;

public class DoNothing implements Schedulable {

    @Override public JobStatus run(Date asOf, Job item) {
        return SUCCESS;
    }
}
