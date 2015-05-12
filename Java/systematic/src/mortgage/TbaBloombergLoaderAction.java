package mortgage;

import static schedule.JobStatus.*;

import java.util.*;

import schedule.*;
import schedule.JobTable.*;

public class TbaBloombergLoaderAction implements Schedulable {

	@Override public JobStatus run(Date asOf, Job item) {
		TbaBloombergLoader.main(new String[0]);
		return SUCCESS;
	}

}
