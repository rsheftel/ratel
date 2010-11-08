package org.ratel.tsdb;

import static org.ratel.tsdb.TimeSeries.*;
import static org.ratel.tsdb.TimeSeriesGroupTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.tsdb.TimeSeriesGroupTable.*;
import org.ratel.util.*;
import org.ratel.db.*;
import static org.ratel.util.Arguments.*;
public class TimeSeriesGroupStatus {


    public static void main(String[] arguments) {
        doNotDebugSqlForever();
        Arguments  args = arguments(arguments, list("group", "date"));
        String name = args.get("group");
        Date asOf = args.date("date");
        TimeSeriesGroup group = GROUPS.get(name);
        info("available source data for \ngroup " + dQuote(name) + " asOf " + ymdHuman(asOf));
        SelectOne<Integer> seriesLookup = group.seriesLookup(asOf);
        for(TimeSeries series : sortedSeries(seriesLookup)) {
            lineStart(series.name()+ "\t", false);
            for(DataSource s : DataSource.allSources()) {
                SeriesSource ss = series.with(s);
                if (!ss.hasObservationToday(asOf)) continue;
                linePart(" " + s.toString());
            }
            lineEnd("");
        }
    }

    private static List<TimeSeries> sortedSeries(SelectOne<Integer> seriesLookup) {
        List<TimeSeries> serieses = empty();
        for(int id : seriesLookup.values())
            serieses.add(series(id));
        Collections.sort(serieses, BY_NAME);
        return serieses;
    }

}
