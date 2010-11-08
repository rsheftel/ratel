package org.ratel.tsdb;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Range.*;
import static org.ratel.util.Sequence.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.systemdb.data.*;
import org.ratel.tsdb.TimeSeriesDataTable.*;
import org.ratel.util.*;
public class Observations implements Sizable, Iterable<Date> {

    class DataStore {
        TreeMap<Date, Double> data = new TreeMap<Date, Double>();
        Set<Date> existence = new HashSet<Date>(); 
        boolean dirty;
        List<Date> times;
        Range range;

        public void put(Date date, double value) {
            dirty = true;
            data.put(date, value);
            existence.add(date);
        }

        public void remove(Date date) {
            dirty = true;
            data.remove(date);
            existence.remove(date);
        }

        public List<Date> times() {
            if (times == null || dirty) recache();
            return Collections.unmodifiableList(times);
        }
        
        void recache() {
            times = list(data.keySet());
            range = null;
            dirty = false;
        }
        
        public double get(Date date) {
            Double result = data.get(date);
            if (result == null) 
                throw bomb("no value matching " + date + "\n observation range: " + dateRange());
            return result;
        }
        
        public Range dateRange() {
            if (range == null) 
                range = range(yyyyMmDd(first(times())), yyyyMmDd(last(times()))); 
            return range;
        }

        public boolean containsKey(Date date) {
            return existence.contains(date);
        }

        public Collection<Double> values() {
            return data.values();
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((data == null) ? 0 : data.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final DataStore other = (DataStore) obj;
            if (data == null) {
                if (other.data != null) return false;
            } else if (!data.equals(other.data)) return false;
            return true;
        }

        public Double valueOrNull(Date date) {
            return data.get(date);
        }
        
        @Override
        public String toString() {
            return toHumanString(data);
        }
    }
    private final DataStore values = new DataStore();

    
    public Observations(List<ObservationRow> rows) {
        for (ObservationRow row : rows) add(row);
    }

    void add(ObservationRow row) {
        row.addInto(values);
    }

    public Observations() {}
    public Observations(Date date, Double value) {
        set(date, value);
    }
    public Observations(long[] datesMillis, double[] values) {
        for(int i : along(values))
            set(new Date(datesMillis[i]), values[i]);
    }
    
    @Override public String toString() {
        return values.toString();
    }

    public int size() {
        return values.times().size();
    }
    
    public Date time() { 
        return the(times());
    }

    public double value(String date) {
        return values.get(date(date));
    }
    
    public double value(Date date) {
        return values.get(date);
    }
    
    public boolean has(Date date) {
        return values.containsKey(date);
    }
    
    public void set(String date, double d) {
        set(Dates.date(date), d);
    }

    public void set(Date date, double value) {
        values.put(date, value);
    }

    public List<Date> times() {
        return values.times();
    }
    
    public long[] timesMillis() {
        List<Date> times = times();
        long[] result = new long[times.size()];
        for (int i : along(times))
            result[i] = times.get(i).getTime();
        return result;
    }
    
    public double[] values() {
        double[] result = new double[size()];
        int count = 0;
        for (double d : values.values())
            result[count++] = d;
        return result;
    }

    @Override public int hashCode() {
        return 31 * 1 + ((values == null) ? 0 : values.hashCode());
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Observations other = (Observations) obj;
        if (values == null) {
            if (other.values != null) return false;
        } else if (!values.equals(other.values)) return false;
        return true;
    }

    public static void write(TsdbObservations writeThese) {
        for (SeriesSource ss : writeThese) 
            ss.write(writeThese.get(ss));
    }

    @Override public boolean isEmpty() {
        return size() == 0;
    }

    public double value() {
        return value(time());
    }

    public boolean hasContent() {
        return !isEmpty();
    }

    public void write(SeriesSource seriesSource) {
        seriesSource.write(this);
    }

    @Override public Iterator<Date> iterator() {
        return times().iterator();
    }

    public Range dateRange() {
        return values.dateRange();
    }

    public Double valueOrNull(Date date) {
        return isEmpty() ? null : values.valueOrNull(date);
    }
    
    public Double valueOrNull() {
        if(isEmpty())
            return null;
        return value();
    }

    public static Observations closes(List<Bar> bars) {
        Observations result = new Observations();
        for (Bar bar : bars)
            result.set(bar.date(), bar.close());
        return result;
    }

    public void remove(String string) {
        remove(date(string));
    }

    public void remove(Date date) {
        values.remove(date);
    }

    public double mostRecentValue() {
        return value(last(times()));
    }
    
    public Double mostRecentValueMaybe() {
        return isEmpty() ? null : mostRecentValue();
    }

}
