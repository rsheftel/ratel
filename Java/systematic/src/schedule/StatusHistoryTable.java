package schedule;

import static db.clause.Clause.*;
import static schedule.JobStatus.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import schedule.JobTable.*;
import schedule.dependency.*;
import db.*;
import db.clause.*;
import db.tables.ScheduleDB.*;

public class StatusHistoryTable extends StatusHistoryBase {
    private static final long serialVersionUID = 1L;
    public StatusHistoryTable() {
        this("status");
    }
    
    public StatusHistoryTable(String alias) {
        super(alias);
    }

    public static final StatusHistoryTable STATUS = new StatusHistoryTable();

    public JobStatus status(String type, int id, Date asOf) {
        if(!hasEntry(type, id, asOf)) return NOT_STARTED;

        Clause latestStatusAsOf = matchesLatestStatus(type, id, asOf);
        return JobStatus.from(C_STATUS.value(latestStatusAsOf));
    }

    private Clause matchesLatestStatus(String type, int id, Date asOf) {
        Clause matches = matches(type, id, asOf);
        StatusHistoryTable inner = new StatusHistoryTable("innerstatus");        
        Clause latest = C_RECORD_NUMBER.is(inner.C_RECORD_NUMBER.max().select(inner.matches(type, id, asOf)));
        return matches.and(latest);
    }

    public Date updateTime(String type, int id, Date asOf) {
        return C_UPDATE_TIME.valueOrNull(matchesLatestStatus(type, id, asOf));
    }

    public boolean hasEntry(String type, int id, Date asOf) {
        return matches(type, id, asOf).exists();
    }

    private Clause matches(String type, int id, Date asOf) {
        return C_TYPE.is(type).and(C_ID.is(id).and(C_AS_OF.is(midnight(asOf))));
    }

    public void setStatusAndCommit(String type, int id, JobStatus newStatus, Date asOf) {
        setStatusAndCommit(type, id, newStatus, asOf, true);
    }

    public void setStatusAndCommit(String type, int id, JobStatus newStatus, Date asOf, boolean isReported) {
        Row data = new Row(
            C_TYPE.with(type),
            C_ID.with(id),
            newStatus.cell(C_STATUS),
            C_AS_OF.with(midnight(asOf)),
            C_UPDATE_TIME.now()
        );
        insert(data);
        Db.commit();
        if (isReported) Scheduler.publish(data);
    }

    public void deleteAll() {
        deleteAll(TRUE);
    }

    public void delete(String type, int id) {
        deleteAll(C_TYPE.is(type).and(C_ID.is(id)));
    }


    public class StatusEntry {
        private final Row data;
        public StatusEntry(Row row) {
            data = row;
        }
        public JobStatus status() {
            return JobStatus.from(data.value(C_STATUS));
        } 
        
    }

    public List<StatusEntry> statuses(Date asOf, Job job) {
        return statuses(asOf, C_TYPE.is("job").and(C_ID.is(job.id())));
    }

    private List<StatusEntry> statuses(Date asOf, Clause itemMatches) {
        List<StatusEntry> result = empty();
        Clause matches = itemMatches.and(C_AS_OF.is(asOf));
        SelectMultiple select = select(matches);
        select.orderBy(C_UPDATE_TIME.ascending());
        List<Row> statuses = rows(matches);
        for (Row row : statuses) 
            result.add(new StatusEntry(row));
        return result;
    }

    public List<StatusEntry> statuses(Date asOf, Dependency dependency) {
        return statuses(asOf, C_TYPE.is("dependency").and(C_ID.is(dependency.id())));
    }
    
}
