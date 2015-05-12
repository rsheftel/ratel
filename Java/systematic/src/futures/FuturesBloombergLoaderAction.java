package futures;

import static schedule.JobStatus.*;

import java.util.*;

import schedule.*;
import schedule.JobTable.*;

public class FuturesBloombergLoaderAction implements Schedulable {

	@Override public JobStatus run(Date asOf, Job item) {
		FuturesBloombergLoader.main(new String[0]);
		return SUCCESS;
	}

}
