package com.malbec.tsdb.markit;

import static schedule.JobStatus.*;

import java.util.*;

import schedule.*;
import schedule.JobTable.*;
import tsdb.*;

public class MarkitIndexLoaderAction implements Schedulable {

    @Override public JobStatus run(Date asOf, Job job) {
        DataSource source = new DataSource(job.parameter("source", "markit"));
        boolean failed = new MarkitIndexRawLoader(job.recipients()).loadAll(source, asOf);
        failed |= new MarkitIndexOTRLoader(job.recipients()).loadAll(source, asOf);
        return failed ? FAILED : SUCCESS;
    }

}
