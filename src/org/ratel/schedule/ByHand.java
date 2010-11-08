package org.ratel.schedule;

import static org.ratel.util.Arguments.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.util.*;

public class ByHand {

    public static void main(String[] args) {
        List<String> notParameters = list("id", "name", "date");
        List<String> empty = empty();
        Arguments arguments = arguments(args, empty);
        Job job = JobTable.job(arguments);
        for (String arg : arguments.keySet()) {
            if(notParameters.contains(arg)) continue;
            job.setTempParameter(arg, arguments.get(arg));
        }

        Date asOf = arguments.get("date", midnight(now()));
        JobStatus status = job.run(asOf);
        Log.info("Job completed with status " + status);
        System.exit(status.isSuccess() ? 0 : -1);
    }

}
