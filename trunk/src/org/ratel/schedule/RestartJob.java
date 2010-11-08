package org.ratel.schedule;

import static org.ratel.schedule.JobStatus.*;
import static org.ratel.util.Arguments.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.util.*;

public class RestartJob {
    public static void main(String[] args) {
        Arguments arguments = arguments(args, list("id", "name", "date"));
        Job job = JobTable.job(arguments);
        Date asOf = arguments.get("date", midnight());
        job.setStatusAndCommit(asOf, RESTART);
    }
}
