package schedule;

import static schedule.JobStatus.*;
import static util.Arguments.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import schedule.JobTable.*;
import util.*;

public class RestartJob {
    public static void main(String[] args) {
        Arguments arguments = arguments(args, list("id", "name", "date"));
        Job job = JobTable.job(arguments);
        Date asOf = arguments.get("date", midnight());
        job.setStatusAndCommit(asOf, RESTART);
    }
}
