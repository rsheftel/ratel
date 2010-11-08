package org.ratel.tsdb;
import static org.ratel.db.clause.Clause.*;
import static java.lang.Integer.*;
import static org.ratel.tsdb.Attribute.*;
import static org.ratel.tsdb.DataSource.*;
import static org.ratel.tsdb.TSAMTable.*;
import static org.ratel.tsdb.TimeSeriesTable.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Sequence.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import junit.framework.*;
import org.ratel.util.*;
import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.file.*;

public class TimeSeries {

    private final String name;
    private AttributeValues attributes;

    public TimeSeries(String name) {
        this.name = name;
    }
    
    public Observations observations(DataSource source) {
        return TimeSeriesDataTable.observations(source, this);
    }

    public Observations observations(DataSource source, Range range) {
        return TimeSeriesDataTable.observations(source, range, this);
    }
    
    public Observations observations(DataSource source, int count) {
        return TimeSeriesDataTable.observations(source, count, this);
    }

    public Observations observations(DataSource source, Range range, int count) {
        return TimeSeriesDataTable.observations(source, range, count, this);
    }

    public static TimeSeries series(String name) {
        return new TimeSeries(name);
    }
    
    public static List<TimeSeries> series(List<String> names) {
        List<TimeSeries> result = empty();
        for (String name : names) {
            result.add(series(name));
        }
        return result;
    }
    
    public static TimeSeries series(AttributeValues values) {
        return new TimeSeries(TIME_SERIES.name(values));
    }
    public static List<TimeSeries> multiSeries(AttributeValues values) {
        List<TimeSeries> result = empty();
        for (String name : TIME_SERIES.names(values)) 
            result.add(new TimeSeries(name));
        return result ;
    }
    
    public static boolean exists(AttributeValues values) {
        return TSAM.rowExists(TSAM.timeSeriesIdMatches(values));
    }

    @Override public String toString() {
        return name(); 
    }

    public String name() {
        return name;
    }

    public void create(AttributeValues values) {
        try {
            TIME_SERIES.create(name(), values);
        } catch (RuntimeException e) {
            throw bomb("failed to create " + name() + " \n" + values, e);
        }
    }
    
    public void addAttributeValue(AttributeValue value) {
        TIME_SERIES.addAtributeValue(id(), value);
    }

    public void delete() {
        TIME_SERIES.delete(id());
    }
    
    public void purgeAllData() {
        TimeSeriesDataTable.purgeAllData(this);
    }

    public void write(DataSource source, Observations observations) {
        TimeSeriesDataTable.write(id(), source.id(), observations);
    }

    public AttributeValues attributes() {
        if(attributes == null) attributes = TSAM.attributes(this); 
        return attributes;
    }

    public static TimeSeries create(String name, AttributeValues values) {
        TimeSeries result = new TimeSeries(name);
        result.create(values);
        return result;
    }

    public List<Row> observationRows(DataSource source, Observations observations) {
        return TimeSeriesDataTable.observationRows(this.id(), source.id(), observations);
    }

    public boolean exists() {
        return TIME_SERIES.exists(name());
    }

    public int id() { 
        try {
            return TIME_SERIES.id(name());
        } catch (RuntimeException e) {
            throw bomb("time series does not exist: " + name(), e);
        }
    }

    public Clause is(Column<Integer> seriesId) {
        return comment("time series=" + name(), seriesId.is(id()));
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final TimeSeries other = (TimeSeries) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    public static TimeSeries series(Integer id) {
        return series(TIME_SERIES.name(id));
    }

    public void createIfNeeded(AttributeValues attr) {
        if(!exists()) create(attr);
    }
    
    public static void main(String[] args) {
        doNotDebugSqlForever();
        TimeSeries series;
        String usage = "usage: $0 <series name|series id> [<series name|series id> ...]\n";
        usage = usage + " also allows -all and -source-[sourcename]";
        bombUnless(args.length >= 1, usage);
        boolean all = false;
        DataSource oneSource = null;
        for (String arg : args) {
            if (arg.equals("-all")) { all = true; continue; }
            if (arg.startsWith("-source-")) { oneSource = source(arg.substring(8)); continue; }
            try {
                series = series(parseInt(arg));
            } catch (RuntimeException e) {
                series = new TimeSeries(arg);
            }
            System.out.print("series " + series.name() + " " + series.id());
            System.out.println(" attributes: \n" + series.attributes());
            List<DataSource> emptyDSs = empty();
            List<DataSource> sources = oneSource == null ? allSources() : list(oneSource);
            for (DataSource source : sources) {
                SeriesSource ss = source.with(series);
                Observations observations = all ? ss.observations() : ss.observations(10);
                if (observations.isEmpty()) { emptyDSs.add(source); continue; }
                System.out.println(source.name() + paren(source.id() + ""));
                for (Date d : observations.times()) 
                    System.out.println(yyyyMmDdHhMmSs(d) + " " + human(observations.value(d)));
            }
        }
    }

    public SeriesSource with(DataSource source) {
        return source.with(this);
    }

    static class SeriesToCreate {
        List<TimeSeries> serieses = empty();
        List<AttributeValues> valueses = empty();
        public void add(TimeSeries series, AttributeValues values) {
            serieses.add(series);
            valueses.add(values);
        }
    }
    
    public static SeriesToCreate createSpecial(String filename) {
        SeriesToCreate result = new SeriesToCreate();
        Csv csv = new Csv(new QFile(filename));
        List<List<String>> records = csv.records();
        String first = first(first(records));
        bombUnless(first.equals("time_series_name"), "first column must be time_series_name.  is " + first);
        List<String> attributeNames = rest(first(records));
        for (List<String> record : rest(records)) {
            String name = first(record);
            List<String> valueStrings = rest(record);
            AttributeValues values = new AttributeValues();
            for (int i : along(attributeNames))
                values.add(attribute(attributeNames.get(i)).value(valueStrings.get(i)));
            TimeSeries series = series(name);
            result.add(series, values);
        }
        return result;
    }
    
    public static void createFromFile(String filename, boolean ignoreExisting) {
        Csv csv = new Csv(new QFile(filename));
        List<List<String>> records = csv.records();
        String first = first(first(records));
        bombUnless(first.equals("time_series_name"), "first column must be time_series_name.  is " + first);
        List<String> attributeNames = rest(first(records));
        for (List<String> record : rest(records)) {
            String name = first(record);
            List<String> valueStrings = rest(record);
            AttributeValues values = new AttributeValues();
            for (int i : along(attributeNames))
                values.add(attribute(attributeNames.get(i)).value(valueStrings.get(i)));
            TimeSeries series = series(name);
            if(!ignoreExisting && series.exists())
                Assert.assertEquals(
                    "duplicate time series found, but attributes do not match\n" + series + 
                    "\nattributes in TSDB:\n" + series.attributes() + "\nattributes in file:\n" + values, 
                    series.attributes(), values
                );
            else
                series.create(values);
        }
    }
    
    public void createFile(QDirectory directory) {
        Csv csv = new Csv();
        AttributeValues values = attributes();
        Set<Attribute> names = values.attributes();
        List<String> header = empty();
        for (Attribute attribute : names)
            header.add(attribute.name());
        Collections.sort(header);
        header.add(0, "time_series_name");
        List<String> record = empty();
        record.add(name());
        for (String attributeName : rest(header))
            record.add(values.get(attribute(attributeName)).name());
        csv.addHeader(header);
        csv.add(record);
        csv.write(directory.file(name() + ".def.csv"));
    }
    
    public static void createFromFile(String filename) {
        createFromFile(filename, false);
    }

    public void replaceAll(AttributeValues newAttributes) {
        TSAM.deleteAttributes(id());
        TIME_SERIES.createAttributeValues(name(), newAttributes);
    }

    private static final class ByName implements java.util.Comparator<TimeSeries> {
        @Override public int compare(TimeSeries o1, TimeSeries o2) {
            return o1.name().compareTo(o2.name());
        }
    }
    public static final ByName BY_NAME = new ByName();

    public void requireHas(AttributeValue value) {
        attributes().requireHas(value);
    }

    public void requireMissing(Attribute attr) {
        attributes().requireMissing(attr);
    }


}
