package org.ratel.tsdb;

import static org.ratel.util.Dates.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.db.*;

import org.ratel.util.*;
import static org.ratel.tsdb.TimeSeries.*;
import static org.ratel.tsdb.DataSource.*;

public class SeriesControl {

    public static void main(String[] args) {
        System.exit(run(args));
    }
    
    public static int run(String[] in) {
        Log.doNotDebugSqlForever();
        Arguments args = Arguments.arguments(in, list("command", "id", "name", "source", "date", "delete", "value"));
        SeriesSource ss  = seriesSource(args);
        Date date = args.date("date");
        String command = args.get("command");
        if(command.equals("delete")) return deletePoint(args, ss, date);
        if (command.equals("add"))  return addPoint(args, ss, date);
        throw bomb("unknown SeriesControl command " + command);
    }

    private static int addPoint(Arguments args, SeriesSource ss, Date date) {
        if (ss.hasObservation(date)) {
            info("observation already exists: " + observationHuman(ss, date));
            return 1;
        }
        Double value = args.numeric("value");
        ss.write(date, value);
        Db.commit();
        info("observation written: " + observationHuman(ss, date));
        return 0;
    }

    private static String observationHuman(SeriesSource ss, Date date) {
        return ss + " " + ymdHuman(date) + " " + ss.observationValue(date);
    }

    private static int deletePoint(Arguments args, SeriesSource ss, Date date) {
        if (ss.hasObservation(date)) {
            info(observationHuman(ss, date));
            if (args.get("delete", false)) {
                info("deleting point");
                ss.deletePoint(date);
                Db.commit();
            }
            return 0;
        } else if (ss.hasObservationToday(date)) {
            Observations today = ss.observations(date);
            for(Date d : today)
                info(ss + " " + ymdHuman(d) + " " + today.value(d));
            info("provide exact time (in quotes?) in order to delete a specific point.");
            return 1;
        } else { 
            info("no point for " + ss + " on " + ymdHuman(date)); 
            return 2; 
        }
    }

    private static SeriesSource seriesSource(Arguments args) {
        TimeSeries series = null;
        if (args.containsKey("id")) series = series(args.integer("id"));
        else if (args.containsKey("name")) series = series(args.string("name"));
        else bomb("please provide either name or id to look up time series from!");
        DataSource source = source(args.string("source"));
        return new SeriesSource(series, source);
    }

}
