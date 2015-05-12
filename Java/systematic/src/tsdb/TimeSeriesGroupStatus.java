package tsdb;

import static tsdb.TimeSeries.*;
import static tsdb.TimeSeriesGroupTable.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import tsdb.TimeSeriesGroupTable.*;
import util.*;
import db.*;
import static util.Arguments.*;
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
