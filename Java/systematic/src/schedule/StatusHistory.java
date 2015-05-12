package schedule;

import static java.util.Collections.*;
import static schedule.StatusHistoryTable.*;
import static util.Arguments.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import schedule.JobTable.*;
import util.*;
import db.*;

public class StatusHistory {
    public static void main(String[] args) {
        doNotDebugSqlForever();
        Arguments arguments = arguments(args, list("id", "name", "date", "recent"));
        Job job = JobTable.job(arguments);
        Date asOf = arguments.get("date", midnight());
        info("status for " + job.name());
        int count = arguments.get("recent", 14);
        for(int i = 0; i < count; i++) {
            List<Row> rows = STATUS.rows(STATUS.C_ID.is(job.id()).and(STATUS.C_AS_OF.is(asOf)).and(STATUS.C_TYPE.is("job")));
            reverse(rows);
            for (Row row : rows) {
                info("asOf " + ymdHuman(asOf) + "@" + ymdHuman(row.value(STATUS.C_UPDATE_TIME)) + " " + row.value(STATUS.C_STATUS));
            }
            info("");
            asOf = daysAgo(1, asOf);
        }
    }

}
