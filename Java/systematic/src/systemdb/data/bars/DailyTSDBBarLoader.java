package systemdb.data.bars;

import static java.lang.Math.*;
import static systemdb.data.bars.BasicBarSmith.*;
import static tsdb.TimeSeriesDataTable.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import systemdb.data.*;
import tsdb.*;
import util.*;

public class DailyTSDBBarLoader {

    private final DataSource source;
    private final TimeSeries open;
    private final TimeSeries high;
    private final TimeSeries low;
    private final TimeSeries close;
    private final TimeSeries volume;
    private final TimeSeries openInterest;
    private final BarSmith smith;
    TsdbObservations cache;
    private final HistoricalDailyData data;

    public DailyTSDBBarLoader(HistoricalDailyData data, DataSource source, 
        TimeSeries open, TimeSeries high, TimeSeries low, TimeSeries close, 
        TimeSeries volume, TimeSeries openInterest, BarSmith smith
    ) {
        this.data = data;
        this.source = source;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.openInterest = openInterest;
        this.smith = smith;
    }

    protected List<TimeSeries> seriesList() {
        List<TimeSeries> result = list(list(close, open, high, low));
        if (hasOpenInterest()) result.add(openInterest);
        if (hasVolume()) result.add(volume);
        return result;
    }

    @Deprecated
    public TsdbObservations observations(Integer count) {
        TsdbObservations closeOnly = observationsMap(source, count, array(close));
        Set<TimeSeries> serieses = set(seriesList());
        TsdbObservations openEtc = observationsMap(source, seriesArray(serieses));
        closeOnly.add(openEtc);
        return closeOnly;
    }
    
    private TsdbObservations observations(Range range) {
        return observationsMap(source, range, seriesArray(new HashSet<TimeSeries>(seriesList())));
    }

    private TimeSeries[] seriesArray(Collection<TimeSeries> result) {
        return result.toArray(new TimeSeries[0]);
    }

    private boolean hasOpenInterest() {
        return openInterest != null;
    }

    private boolean hasVolume() {
        return volume != null;
    }

    public List<Bar> bars(Range range) {
        return bars(observations(range));
    }
    
    public List<Bar> bars(int count) {
        List<Bar> bars = bars();
        if (isEmpty(bars)) return bars;
        count = min(count, bars.size());
        int start = bars.size() - count;
        int end = bars.size();
        return bars.subList(start, end);
    }

    private List<Bar> bars() {
        Range alltime = Range.range((Date) null, null);
        List<Bar> bars = bars(alltime);
        return bars;
    }

    private List<Bar> bars(TsdbObservations group) {
    	List<ProtoBar> protoBars = protoBars(group, ss(open), ss(high), ss(low), ss(close), ss(volume), ss(openInterest));
        return smith.convert(data, protoBars);
    }


    private SeriesSource ss(TimeSeries series) {
    	if(series == null) return null;
        return source.with(series);
    }

    public int count() {
        return bars().size();
    }
    
}