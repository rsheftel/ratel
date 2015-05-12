package mortgage;

import static schedule.JobStatus.*;
import static tsdb.DataSource.*;

import java.util.*;

import schedule.*;
import schedule.JobTable.*;

public class JpTbaCopierAction implements Schedulable {
	@Override public JobStatus run(Date asOf, Job schedule) {
		new JpTbaCopier(asOf, JPMORGAN).copy();
		return SUCCESS;
	}
	
}