package com.malbec.tsdb.ivydb;

import static schedule.JobStatus.*;

import java.util.*;

import schedule.*;
import schedule.JobTable.*;
import tsdb.*;

public class IvyDbLoaderAction implements Schedulable {

	@Override public JobStatus run(Date asOf, Job job) {
		DataSource source = new DataSource(job.parameter("source", "ivydb"));
		boolean failed = new OptionVolumeLoader(job.recipients()).loadAll(source, asOf);
		failed |= new SecurityPriceLoader(job.recipients()).loadAll(source, asOf);
		failed |= new StdOptionPriceLoader(job.recipients()).loadAll(source, asOf);
		return failed ? FAILED : SUCCESS;
	}

}
