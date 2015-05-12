package futures;

import static db.tables.BloombergFeedDB.BloombergDataBase.*;
import static db.tables.BloombergFeedDB.JobBloombergDataBase.*;
import static tsdb.DataSource.*;
import static util.Strings.*;

import java.util.*;

import bloomberg.*;

import db.clause.*;
import tsdb.*;

public class BloombergJobEntry {

	private final String ticker;
	private final String field;
	private final SeriesSource ss;
	public static boolean forceSourceToTestForTesting;

	public BloombergJobEntry(String ticker, String field, SeriesSource seriesSource) {
		this.ticker = ticker;
		this.field = field;
		this.ss = forceSourceToTestForTesting ? seriesSource.series().with(BLOOMBERG_TEST): seriesSource;
	}

	public void addTo(int jobId) {
		T_BLOOMBERGDATA.insert(
			T_BLOOMBERGDATA.C_TICKERBB.with(ticker),
			T_BLOOMBERGDATA.C_FIELDBB.with(field),
			T_BLOOMBERGDATA.C_NAMETIMESERIES.with(ss.name())
		);
		Clause matches = T_BLOOMBERGDATA.C_TICKERBB.is(ticker);
		matches = matches.and(T_BLOOMBERGDATA.C_FIELDBB.is(field));
		matches = matches.and(T_BLOOMBERGDATA.C_NAMETIMESERIES.is(ss.name()));
		int dataId = T_BLOOMBERGDATA.C_IDBBDATA.value(matches);
		T_JOBBLOOMBERGDATA.insert(
			T_JOBBLOOMBERGDATA.C_IDBBDATA.with(dataId),
			T_JOBBLOOMBERGDATA.C_IDJOB.with(jobId)
		);
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((ss == null) ? 0 : ss.hashCode());
		result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final BloombergJobEntry other = (BloombergJobEntry) obj;
		if (field == null) {
			if (other.field != null) return false;
		} else if (!field.equals(other.field)) return false;
		if (ss == null) {
			if (other.ss != null) return false;
		} else if (!ss.equals(other.ss)) return false;
		if (ticker == null) {
			if (other.ticker != null) return false;
		} else if (!ticker.equals(other.ticker)) return false;
		return true;
	}

	@Override public String toString() {
		return join("-", field, ss.name(), ticker);
	}

	public String field() {
		return field;
	}

	public void writeObservation(Date date, double d) {
		ss.write(new Observations(date, d));
	}

    public SeriesSource seriesSource() {
        return ss;
    }

    public BloombergSecurity security() {
        return new BloombergSecurity(ticker);
    }
}
