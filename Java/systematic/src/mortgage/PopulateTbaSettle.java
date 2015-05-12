package mortgage;

import java.util.*;
import static mortgage.TbaSettleTable.*;
import static schedule.JobStatus.*;
import static tsdb.DataSource.*;
import static util.Dates.*;

import schedule.*;
import schedule.JobTable.*;

public class PopulateTbaSettle implements Schedulable {

	@Override public JobStatus run(Date asOf, Job item) {
		TBA_SETTLE.populate(BLOOMBERG_BBT3, businessDaysAgo(1, asOf, "nyb"));
		return SUCCESS;
	}


}
