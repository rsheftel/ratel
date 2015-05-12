package tsdb;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import tsdb.TimeSeriesDataTable.*;
import util.*;

public class TsdbObservations extends ObservationsMap<SeriesSource> {
    
    public TsdbObservations() {}
    
    public TsdbObservations(List<ObservationRow> rows) {
        for (ObservationRow row : rows)
            add(row);
    }

    public Set<String> seriesNames() {
        Set<String> result = emptySet();
        for (SeriesSource ss : this)
            result.add(ss.series().name());
        return result;
    }

    public Set<String> sourceNames() {
        Set<String> result = emptySet();
        for (SeriesSource ss : this)
            result.add(ss.source().name());
        return result;
    }

    public void add(ObservationRow row) {
        SeriesSource s = row.seriesSource();
        if(!has(s))
            add(s);
        get(s).add(row);
    }

    public Range range() {
        Range result = null;
        for(SeriesSource ss : this) {
            Range range = get(ss).dateRange();
            result = result == null ? range : result.union(range);
        }
        return bombNull(result, "no series in this observations, cannot get a range");
    }


    
}