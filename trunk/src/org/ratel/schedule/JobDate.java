package org.ratel.schedule;

import java.util.*;

import org.ratel.schedule.JobTable.*;

public class JobDate {

    private final Job job;
    private final Date asOf;

    public JobDate(Job job, Date asOf) {
        this.job = job;
        this.asOf = asOf;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((asOf == null) ? 0 : asOf.hashCode());
        result = prime * result + ((job == null) ? 0 : job.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final JobDate other = (JobDate) obj;
        if (asOf == null) {
            if (other.asOf != null) return false;
        } else if (!asOf.equals(other.asOf)) return false;
        if (job == null) {
            if (other.job != null) return false;
        } else if (!job.equals(other.job)) return false;
        return true;
    }

    public JobStatus status() {
        return job.status(asOf);
    }

    public void setStatusAndCommit(JobStatus jobStatus) {
        job.setStatusAndCommit(asOf, jobStatus);
    }

    public void run() {
        job.run(asOf);
    }

    public Job job() {
        return job;
    }

    public void markInProgress() {
        job.markInProgress(asOf);
    }

    public void addToMap(Map<String, Date> run) {
        run.put(job.name(), asOf);
    }
    
}
