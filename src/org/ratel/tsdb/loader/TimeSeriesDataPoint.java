package org.ratel.tsdb.loader;

import static org.ratel.tsdb.TimeSeries.*;

public class TimeSeriesDataPoint {

    private final int id;
    private final Double value;

    public TimeSeriesDataPoint(int id, Double value) {
        this.id = id;
        this.value = value;
    }
    
    public Double value() {
        return value;
    }

    public int id() {
        return id;
    }

    @Override public String toString() {
        return series(id).name() + " " + value;
    }
}
