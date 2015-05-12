package schedule;

import static schedule.JobStatus.*;
import static util.Errors.*;

import java.util.*;

import schedule.JobTable.*;

class RunMe implements Schedulable {
	@Override public JobStatus run(Date asOf, Job item) {
		bombUnless(item.status(asOf).inProgress(), "item status not in progress");
		AbstractJobTest.run.put(item.name(), asOf);
		return SUCCESS;
	}
	
}