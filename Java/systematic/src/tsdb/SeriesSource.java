package tsdb;
import static util.Dates.*;
import static util.Errors.*;
import static util.Range.*;

import java.util.*;

import util.*;
import db.*;
import db.clause.*;
import db.columns.*;
public class SeriesSource {

	private final DataSource source;
	private final TimeSeries series;

	public SeriesSource(String seriesName, String sourceName) {
		this(new TimeSeries(seriesName), new DataSource(sourceName));
	}
	
	public SeriesSource(TimeSeries series, DataSource source) {
		this.series = series;
		this.source = source;
	}

	public SeriesSource(String value) {
		this(value.replaceAll(":.*", ""), value.replaceAll(".*:", ""));
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((series == null) ? 0 : series.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final SeriesSource other = (SeriesSource) obj;
		if (series == null) {
			if (other.series != null) return false;
		} else if (!series.equals(other.series)) return false;
		if (source == null) {
			if (other.source != null) return false;
		} else if (!source.equals(other.source)) return false;
		return true;
	}

	public Observations observations() {
		return series.observations(source);
	}

	public void write(Observations observations) {
		series.write(source, observations);
	}

	public TsdbObservations observationsMap() {
		return TimeSeriesDataTable.observationsMap(source, series);
	}

	public Clause matches(IntColumn seriesId, IntColumn sourceId) {
		return series.is(seriesId).and(source.is(sourceId));
	}

	public TimeSeries series() {
		return series;
	}

	public void purge() {
		TimeSeriesDataTable.purge(this);
	}

	public Observations observations(Range range) {
		return series.observations(source, range);
	}
	
	public Observations observations(Range range, int count) {
        return series.observations(source(), range, count);
    }

    public Observations observations(Integer count) {
	    return series.observations(source, count);
	}
	
	public double observationValue(Date d) {
		try {
            return observations(d).value();
        } catch (RuntimeException e) {
            throw bomb("error getting single value for series source " + this, e);
        }
	}

	public Observations observations(Date d) {
		return observations(onDayOf(d));
	}
	
	@Override public String toString() {
		return series + ":" + source;
	}

	public Observations latestObservation() {
		return TimeSeriesDataTable.latestObservation(this);
	}
	
    public Observations firstObservation() {
        return TimeSeriesDataTable.firstObservation(this);
    }

	public void putInto(Row valueRow, IntColumn ts, IntColumn ds) {
		valueRow.put(ts.with(series.id()));
		valueRow.put(ds.with(source.id()));
	}

	public boolean hasObservation(Date closingTime) {
		return observations(Range.range(closingTime, closingTime)).hasContent();
	}

	public String name() {
		return series.name() + ":" + source.name();
	}

	public void write(Date date, double value) {
		write(new Observations(date, value));
	}

	public void write(String date, double value) {
		write(date(date), value);
	}

	public boolean hasObservationToday(Date now) {
		return observations(onDayOf(now)).hasContent();
	}

    public DataSource source() {
        return source;
    }

    public int count() {
        return TimeSeriesDataTable.observationsCount(source(), series());
    }

    public double observationValue(String date) {
        return observationValue(date(date));
    }

    public boolean hasObservation() {
        return count() > 0;
    }

    public Double observationValueBefore(Date date) {
        return TimeSeriesDataTable.observationValueBefore(source(), series(), date);
    }

    public void deletePoint(Date date) {
        TimeSeriesDataTable.deletePoint(source(), series(), date);
    }

}
