package org.ratel.schedule;

import static org.ratel.db.clause.Clause.*;
import static org.ratel.mail.Email.*;
import static org.ratel.mail.EmailAddress.*;
import static org.ratel.schedule.JobStatus.*;
import static org.ratel.schedule.ParametersTable.*;
import static org.ratel.schedule.StatusHistoryTable.*;
import static org.ratel.schedule.dependency.DependencyTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Range.*;
import static org.ratel.util.RunCalendar.*;
import static org.ratel.util.Strings.*;
import static org.ratel.util.Systematic.*;

import java.util.*;

import org.ratel.mail.*;
import org.ratel.schedule.dependency.*;
import org.ratel.util.*;
import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.db.tables.ScheduleDB.*;
import org.ratel.file.*;

public class JobTable extends JobBase {
    private static final long serialVersionUID = 1L;
    public static final JobTable JOBS = new JobTable();
    
    public JobTable() {
        super("jobs");
    }
    
    public static String expanded(String string, Date asOf, RunCalendar runCalendar) {
        Date midnight = midnight(asOf);
        string = string.replace("[[DATE]]", ymdHuman(midnight));
        string = string.replace("[[DATA_DIR]]", dataDirectory().path().replace("/", "\\"));
        string = string.replace("[[BIZ_YESTERDAY]]", ymdHuman(runCalendar.priorDay(asOf)));
        string = string.replace("[[BIZ_TWO_DAYS_AGO]]", ymdHuman(runCalendar.priorDay(runCalendar.priorDay(asOf))));
        return string;
    }

    public class Job extends Row {
        private static final long serialVersionUID = 1L;
        private final RunCalendar calendar;
        private Map<String, String> parameters;

        public Job(Row data) { 
            super(data);
            calendar = RunCalendar.from(value(C_FINANCIAL_CENTER));
            parameters = PARAMETERS.parameters(id());
        }

        public JobStatus run(Date asOf) {
            JobStatus status = FAILED;
            try {
                status = action().run(asOf, this);
                setStatusAndCommit(asOf, status);
            } catch(Exception e) {
                reportError("Error occurred running job ", e);
                Db.rollback();
                setStatusAndCommit(asOf, FAILED);
            }
            return status;
        }

        private void reportError(String subject, Exception e) {
            send(problem(subject, this + "\n" + e.getMessage() + "\n" + trace(e)));
        }        

        public String recipients() {
            return value(C_RECIPIENTS);
        }

        public String name() {
            return value(C_NAME);
        }

        public Integer id() {
            return value(C_ID);
        }
        
        public String deadlineTime() {
            return value(C_DEADLINE_TIME);
        }

        public boolean needsRun(Date asOf) {
            if(status(asOf).isHoliday()) return false;
            if(isHoliday(asOf)) {
                setStatusAndCommit(asOf, NOT_BUSINESS_DAY);
                return false;
            }
            if(status(asOf).isNotRunnable()) return false;
            if(isDependencyFailed(asOf)) return false;
            if(isBlocked(asOf)) return false; 
            return true;
        }

        private boolean isBlocked(Date asOf) {
            Date prior = prior(asOf);
            JobStatus priorStatus = status(prior);
            if (!priorStatus.inProgress() && !priorStatus.isBlocked()) return false;
            if(status(asOf).isBlocked()) return true;
            
            setStatusAndCommit(asOf, BLOCKED);
            send(problem(
                "job (" + name() + ") for " + ymdHuman(prior) + " is still running or blocked. " + ymdHuman(asOf) + " is BLOCKED.",
                "This job will not run while the status of the prior day's job is IN_PROGRESS."
            ));
            return true;
        }

        private boolean isDependencyFailed(Date asOf) {
            boolean dependencyFailed = false;
            for (Dependency dependency : dependencies()) {
                try {
                    if(dependency.status(asOf).isSuccess()) continue;
                    boolean isIncomplete = dependency.isIncomplete(asOf);
                    if(isIncomplete) dependency.setStatusAndCommit(RETRY_NEXT_RUN, asOf);
                    else dependency.setStatusAndCommit(SUCCESS, asOf);
                    dependencyFailed |= isIncomplete;
                    Db.commit();
                } catch (RuntimeException e) {
                    Db.rollback();
                    dependency.setStatusAndCommit(FAILED, asOf);
                    Db.commit();
                    dependencyFailed = true;
                    reportError("Error occurred checking dependency " + dependency.id() + " in data_schedule", e);
                }
            }
            return dependencyFailed;
        }

        List<Dependency> dependencies() {
            return DEPENDENCIES.dependencies(id());
        }

        private boolean isHoliday(Date asOf) {
            return !calendar.isValid(asOf);
        }

        @SuppressWarnings("unchecked") public Schedulable action() {
            String type = value(C_ACTION);
            try {
                Class<? extends Schedulable> c = (Class<? extends Schedulable>) Class.forName(type);
                return c.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            } catch (Exception e) {
                throw bomb("failed to create action of type " + type, e);
            }
        }

        public void send(Email email) {
            email.sendTo(recipients());
        }

        public QFile logFile(QDirectory log) {
            QDirectory logDir = log.directory(logDirName());
            logDir.createIfMissing();
            return logDir.file(yyyyMmDdHhMmSs(now()).replaceAll("\\D+", "") + ".log");
        }

        String logDirName() {
            return javaClassify(name());
        }

        public JobStatus status(Date asOf) {
            return STATUS.status("job", id(), asOf);
        }        
        
        public void setStatusAndCommit(Date asOf, JobStatus newStatus, boolean isReported) {
            STATUS.setStatusAndCommit("job", id(), newStatus, asOf, isReported);
        }


        public void setStatusAndCommit(Date asOf, JobStatus newStatus) {
            setStatusAndCommit(asOf, newStatus, true);
        }

        public Dependency insertDependency(Class<? extends Dependency> class1, Map<String, String> params) {
            return DEPENDENCIES.insert(id(), class1.getName(), params);
        }
        
        public String parameter(String name, String defalt) {
            return hasParameter(name) ? parameter(name) : defalt;
        }

        public String parameterExpanded(String string, Date asOf) {
            return expanded(parameter(string), asOf, calendar());
        }

        public String parameter(String name) {
            return bombNull(parameters.get(name), "no parameter with name " + name);
        }

        public boolean hasParameter(String name) {
            return parameters.containsKey(name);
        }

        public void setParameters(Map<String, String> parameters) {
            PARAMETERS.insert(id(), parameters);
            this.parameters = PARAMETERS.parameters(id());
        }

        public void checkDeadline(Date asOf) {
            if(isHoliday(asOf)) return;
            if(status(asOf).isLate() || status(asOf).isFailed()) return;
            if(calendar.isBeforeTime(asOf, deadlineTime())) return;
            if(!action().getClass().isAssignableFrom(DoNothing.class))
                send(problem(
                    "job " + paren(name() + ":" + id()) + " is late for asOf: " + ymdHuman(asOf) + ".  current status: " + status(asOf),
                    isWaitingOnDependencies(asOf) ? explainDependencies(asOf) : "status is " + status(asOf)
                ));
            setStatusAndCommit(asOf, status(asOf).toLate());
        }

        private boolean isWaitingOnDependencies(Date asOf) {
            return !status(asOf).isStarted();
        }

        private String explainDependencies(Date asOf) {
            StringBuilder buf = new StringBuilder();
            for (Dependency d : dependencies()) {
                JobStatus status = d.status(asOf);
                buf.append("dependency " + d + " in status " + status + "\n");
                if(status.waitingForRetry()) {
                    buf.append(bombNull(d.explain(asOf), "no explanation for dependency " + d) + "\n");
                }
            }
            return buf.toString();
        }

        public Dependency runAfter(String time) {
            return WaitForTime.create(this, time);
        }

        public Dependency runAfter(Job parent) {
            return WaitForJob.create(this, parent);
        }

        public Dependency runAfter(Dependency d, int delaySeconds) {
            return WaitForDependency.create(this, d, delaySeconds);
        }
        
        public Dependency runAfter(Dependency d) {
            return WaitForDependency.create(this, d);
        }

        public void delete() {
            PARAMETERS.delete(id());
            DEPENDENCIES.deleteJobDependecies(id());
            JobTable.this.deleteOne(C_ID.is(id()));
            STATUS.delete("job", id());
        }

        public Date asOf(Date now) {
            return calendar.asOf(midnight(now));
        }

        public Date prior(Date asOf) {
            return calendar.priorDay(asOf);
        }

        public void markPriorCancelled(Date asOf) {
            setStatusAndCommit(prior(asOf), CANCELLED);
        }

        public void markInProgress(Date asOf) {
            setStatusAndCommit(asOf, status(asOf).toInProgress());
        }

        public RunCalendar calendar() {
            return calendar;
        }

        public Date statusTime(Date asOf) {
            return STATUS.updateTime("job", id(), asOf);
        }

        public void setTempParameter(String name, String value) {
            parameters.put(name, value);
        }

        public void deleteDependencies() {
            DEPENDENCIES.deleteJobDependecies(id());
        }
        
        public List<StatusHistoryTable.StatusEntry> statusHistory() {
            return STATUS.statuses(midnight(), this);
        }


     }

    public Job insert(String name, Schedulable action, String time,
        EmailAddress recipients, RunCalendar center) {
        insert(
            C_NAME.with(name),
            C_DEADLINE_TIME.with(time),
            recipients.in(C_RECIPIENTS),
            C_ACTION.with(action.getClass().getName()), 
            center.cell(C_FINANCIAL_CENTER)
        );
        return forName(name);
    }

    public Job insert(String name, Schedulable s, String time, RunCalendar center) {
        return insert(name, s, time, NOBODY, center);
    }
    
    public Job insert(String name, Schedulable s, String time) {
        return insert(name, s, time, WEEKDAYS);
    }

    public Job forName(String name) {
        return new Job(row(C_NAME.is(name)));
    }

    public Job forId(int id) {
        return new Job(row(C_ID.is(id)));
    }

    public List<JobDate> jobsToRun(Date now) {
        return runnable(jobs(), now);
    }

    List<Job> jobs() {
        return jobs(TRUE);
    }

    private List<Job> jobs(Clause matches) {
        List<Job> result = empty();
        for (Row row : rows(matches))
            result.add(new Job(row));
        return result;
    }

    private List<JobDate> runnable(List<Job> jobs, Date now) {
        List<JobDate> result = empty();
        for(Job job : jobs) {
            Date asOf = job.asOf(now);
            if (job.status(asOf).isNotRunnable()) continue;
            Date prior = job.prior(asOf);
            if (job.needsRun(asOf)) {
                JobStatus priorStatus = job.status(prior);
                result.add(new JobDate(job, asOf));
                if(!priorStatus.isStarted() && !priorStatus.isCancelled()) 
                    job.markPriorCancelled(asOf);
                continue;
            }
            if (job.needsRun(prior))
                result.add(new JobDate(job, prior));
        }
        return result;
    }

    public void deleteAll() {
        PARAMETERS.deleteAll(TRUE);
        DEPENDENCIES.deleteAll();
        deleteAll(TRUE);
        STATUS.deleteAll();
    }

    public void checkDeadlines(Date now) {
        for (Job j : jobs()) { 
            Date asOf = j.asOf(now);
            try {
                if (j.status(asOf).isSuccess()) continue;
                j.checkDeadline(asOf);
            } catch (RuntimeException e) {
                Db.rollback();
                j.reportError("Failed checking deadline, job is marked as FAILED and will not rerun", e);
                j.setStatusAndCommit(asOf, FAILED);
            }
        }
    }

    public boolean exists(String name) {
        return C_NAME.is(name).exists();
    }

    public void dumpNames(List<String> matches) {
        List<String> names = isEmpty(matches) ? C_NAME.values() : matches;
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        Log.info("\n" + commaSep(names) + "\n");
    }

    public static Job job(Arguments arguments) {
        if (arguments.containsKey("id")) {
            int id = Integer.valueOf(arguments.get("id"));
            return JOBS.forId(id);
        }
        String name = arguments.get("name", "<unspecified>");
        if (!JOBS.exists(name)) {
            List<String> jobNames = JOBS.like(name);
            if (jobNames.size() == 1) name = the(jobNames);
            else {
                JOBS.dumpNames(jobNames);
                Log.err("job " + sQuote(name) + " not found. did you correctly specify -id or -name?");
                System.exit(-1);
            }
        }
        return JOBS.forName(name);
    }

    private List<String> like(String name) {
        return C_NAME.values(C_NAME.like("%" + name + "%"));
    }

    public void setAllSuccess(String asOf) {

        for(Job j : jobs())
            for(Date d : range(daysAgo(4, date(asOf)), 5)) 
                j.setStatusAndCommit(d, SUCCESS, false);
    }


}
