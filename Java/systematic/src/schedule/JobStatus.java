package schedule;

import static util.Errors.*;
import static util.Objects.*;
import db.*;
import db.columns.*;


public enum JobStatus {
	NOT_STARTED(false), 
	RESTART(false), 
	NOT_BUSINESS_DAY(false), 
	LATE(false),
	BLOCKED(false),
	BLOCKED_LATE(false),
	IN_PROGRESS(true), 
	IN_PROGRESS_LATE(true), 
	FAILED(true), 
	CANCELLED(true), 
	SUCCESS(true),
	RETRY_NEXT_RUN(false);

	private final boolean isNotRunnable;

	private JobStatus(boolean isNotRunnable) {
		this.isNotRunnable = isNotRunnable;
		
	}
	
	public boolean isNotRunnable() {
		return isNotRunnable;
	}

	public boolean isStarted() {
	    return !list(NOT_STARTED, RESTART, CANCELLED, BLOCKED, BLOCKED_LATE, LATE, NOT_BUSINESS_DAY).contains(this);
	}

	public boolean isLate() {
		return list(LATE, IN_PROGRESS_LATE, BLOCKED_LATE).contains(this);
	}

	public boolean inProgress() {
		return equals(IN_PROGRESS) || equals(IN_PROGRESS_LATE);
	}

	public boolean isSuccess() {
		return equals(SUCCESS);
	}
	
	public boolean isCancelled() {
	    return equals(CANCELLED);
	}

	public boolean waitingForRetry() {
		return equals(RETRY_NEXT_RUN);
	}

    public boolean isFailed() {
        return equals(FAILED);
    }

    public Cell<?> cell(NvarcharColumn column) {
        return column.with(name());
    }

    public JobStatus toInProgress() {
        bombIf(isNotRunnable(), "cannot move from a not runnable state (" + this + ") to 'in progress'");
        return isLate() ? IN_PROGRESS_LATE : IN_PROGRESS;
        
    }

    public boolean isBlocked() {
        return equals(BLOCKED) || equals(BLOCKED_LATE);
    }

    public JobStatus toLate() {
        if(equals(IN_PROGRESS)) return IN_PROGRESS_LATE;
        if(equals(BLOCKED)) return BLOCKED_LATE;
        if(equals(NOT_STARTED) || equals(RESTART)) return LATE;
        throw bomb("can't go from state (" + this + ") to LATE");
    }

    public boolean isHoliday() {
        return equals(NOT_BUSINESS_DAY);
    }

    public void exit() {
        System.exit(isSuccess() ? 0 : 1);
    }

    public static JobStatus from(String status) {
        return bombNull(valueOf(status), "invalid status " + status);
    }

	
}
