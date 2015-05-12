package futures;

import static db.Column.*;
import static db.clause.Clause.*;
import static db.tables.BloombergFeedDB.BloombergDataBase.*;
import static db.tables.BloombergFeedDB.JobBloombergDataBase.*;
import static db.temptables.TSDB.BloombergMoribundBase.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.tables.BloombergFeedDB.*;
public class BloombergJobTable extends JobBase {
    private static final long serialVersionUID = 1L;
	public static final BloombergJobTable BLOOMBERG_JOBS = new BloombergJobTable();
	
	public BloombergJobTable() {
		super("jobs");
	}

	public BloombergJob insert(String name, String timeObs, boolean isClose, String runTime) {
		insert(
			C_NAMEJOB.with(name),
			C_NAMEFILE.with(name),
			C_TIMEOBSERVATION.with(timeObs),
			C_ISCLOSEOFBUSINESS.with(isClose),
			C_TIMERUN.with(runTime)
		);
		BloombergJob job = new BloombergJob(name);
		return job;
	}

	public int id(String name) {
		Clause named = C_NAMEJOB.is(name);
		if(!rowExists(named)) throw new BloombergJobDoesNotExist(name + " does not exist");
		return C_IDJOB.value(named);
	}

	public void deleteAllEntries(String jobNamePattern) {
		Clause matches = C_NAMEJOB.like(jobNamePattern);
		matches = matches.and(C_IDJOB.joinOn(T_JOBBLOOMBERGDATA));
		SelectMultiple matching = T_JOBBLOOMBERGDATA.selectDistinct(columns(T_JOBBLOOMBERGDATA.C_IDBBDATA), matches);
		matching.intoTemp("bloomberg_moribund");
//		new Generator().writeFile(toDelete.schemaTable(), "temptables");
		T_JOBBLOOMBERGDATA.deleteAll(matches);
		T_BLOOMBERGDATA.deleteAll(T_BLOOMBERGDATA.C_IDBBDATA.joinOn(T_BLOOMBERG_MORIBUND));
	}

    public List<BloombergJob> due() {
        String nowTime = hhMmSs(now()).substring(0, 5);
        List<Row> rows = rows(TRUE);
        List<BloombergJob> result = empty();
        for (Row  row : rows) {
            String jobName = row.value(C_NAMEJOB);
            String timeRun = row.value(C_TIMERUN);
            if (nowTime.compareTo(timeRun) < 0) {
                info("skipping " + jobName + " before " + timeRun);
                continue;
            }
            if (isPastMidnight(row) == nowTime.compareTo("06:00") < 0) 
                result.add(new BloombergJob(jobName));
            else info("skipping " + jobName + " before " + timeRun + " (isPastMidnight(row) == " + isPastMidnight(row) + ")" );
        }
        return result;
    }

    public boolean isPastMidnight(Row row) {
        return row.value(C_TIMERUN).compareTo("06:00") < 0;
    }
    
    public boolean isPastMidnight(int id) {
        return isPastMidnight(row(matches(id)));
    }

    public String observationTime(int id) {
        return C_TIMEOBSERVATION.value(matches(id));
    }

    private Clause matches(int id) {
        return C_IDJOB.is(id);
    }

    public void updateLastRunOn(int id) {
        C_LASTRUNON.updateOne(matches(id));
    }

    public String timeRun(int id) {
        return C_TIMERUN.value(matches(id));
    }

}
