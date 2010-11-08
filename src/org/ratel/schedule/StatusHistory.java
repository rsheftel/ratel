package org.ratel.schedule;

import static java.util.Collections.*;
import static org.ratel.schedule.StatusHistoryTable.*;
import static org.ratel.util.Arguments.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.util.*;
import org.ratel.db.*;

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
