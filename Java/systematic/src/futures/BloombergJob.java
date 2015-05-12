package futures;

import static db.tables.BloombergFeedDB.BloombergDataBase.*;
import static db.tables.BloombergFeedDB.JobBloombergDataBase.*;
import static futures.BloombergJobTable.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import tsdb.*;
import db.*;
import db.clause.*;

public class BloombergJob {

	private int id;

	public BloombergJob(String name) {
		id = BLOOMBERG_JOBS.id(name);
	}
	
	@Override public String toString() {
	    return "" + id;
	}

	public void add(BloombergJobEntry jobEntry) {
		jobEntry.addTo(id);
	}

	public List<BloombergJobEntry> entries() {
		List<BloombergJobEntry> result = empty();
		Clause jobMatches = T_JOBBLOOMBERGDATA.C_IDJOB.is(id);
		Clause isForJob = T_BLOOMBERGDATA.C_IDBBDATA.is(T_JOBBLOOMBERGDATA.C_IDBBDATA);
		List<Row> rows = T_BLOOMBERGDATA.rows(jobMatches.and(isForJob));
		for (Row row : rows) { 
			SeriesSource ss = new SeriesSource(row.value(T_BLOOMBERGDATA.C_NAMETIMESERIES));
			result.add(new BloombergJobEntry(
				row.value(T_BLOOMBERGDATA.C_TICKERBB),
				row.value(T_BLOOMBERGDATA.C_FIELDBB),
				ss 
			));
		}
		return result;
	}
	
	public static void addJobs(BloombergLoadable loadable, Date asOf) {
		List<BloombergField> fields = loadable.fields();
		for (BloombergField field : fields) {
            try {
                BloombergJob job = loadable.job(field);
			    for (BloombergJobEntry entry : loadable.jobEntries(asOf, field))
			        job.add(entry);
			} catch (BloombergJobDoesNotExist e) {
			    info("Skipping Bloomberg job, since no entry exists in Job table: " + loadable);
			}
		}
	}

    public boolean hasObservation(BloombergJobEntry entry) {
        return entry.seriesSource().hasObservation(observationTime());
    }

    public Date observationTime() {
        Date refDate = BLOOMBERG_JOBS.isPastMidnight(id) ? yesterday() : now();
        return observationTime(midnight(refDate));
    }
    
    public Date observationTime(Date midnight) {
        return timeOn(BLOOMBERG_JOBS.observationTime(id), midnight);
    }

    public void updateLastRunOn() {
        BLOOMBERG_JOBS.updateLastRunOn(id);
    }


}
