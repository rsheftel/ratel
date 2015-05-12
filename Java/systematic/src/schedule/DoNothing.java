package schedule;

import static schedule.JobStatus.*;

import java.util.*;

import schedule.JobTable.*;

public class DoNothing implements Schedulable {

	@Override public JobStatus run(Date asOf, Job item) {
		return SUCCESS;
	}
}
