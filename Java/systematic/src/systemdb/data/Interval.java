package systemdb.data;

import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;

import java.io.*;
import java.util.*;

import systemdb.data.bars.*;
import util.*;



public class Interval implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Map<String, Interval> lookup = emptyMap();
    public static final Interval SECOND = new Interval(1, "second");
    public static final Interval MINUTE = new Interval(60, "minute");
    public static final Interval FIVE_MINUTES = new Interval(300, "5minute");
    public static final Interval TEN_MINUTES = new Interval(600, "10minute");
    public static final Interval FIFTEEN_MINUTES = new Interval(900, "15minute");
    public static final Interval HALF_HOURLY = new Interval(1800, "30minute");
    public static final Interval HOURLY = new Interval(3600, "hourly");
    public static final Interval DAILY = new Interval(86400, "daily");

    private final String name;
    private final int seconds;

    private Interval(int seconds, String name) {
        this.seconds = seconds;
        this.name = name;
        lookup.put(name, this);
    }

    public int seconds() {
        return seconds;
    }

    public long millis() {
        return seconds * 1000;
    }

    public static Interval lookup(String value) {
        return bombNull(lookup.get(value), "unknown interval: " + value);
    }

    public Date advance(Date date) {
        return secondsAhead(seconds, date);
    }

    private Date rewind(Date date) {
        return secondsAgo(seconds, date);
    }

    public Date nextBoundary() {
        return nextBoundary(now());
    }

    public Date nextBoundary(Date date) {
        Date midnight = midnight(date);
        int secondsSinceMidnight = secondsBetween(midnight, date);
        int periodsSinceMidnight = secondsSinceMidnight / seconds;
        int midnightOffset = (periodsSinceMidnight + 1) * seconds;
        return secondsAhead(midnightOffset, midnight);
    }

    public boolean isOnBoundary(Date date) {
        return secondsSinceMidnight(date) % seconds == 0;
    }

    private int secondsSinceMidnight(Date date) {
        return secondsBetween(midnight(date), date);
    }

    public boolean isDaily() {
        return equals(DAILY);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + seconds;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Interval other = (Interval) obj;
        if (seconds != other.seconds) return false;
        return true;
    }

    public int minutes() {
        bombUnless(seconds % 60 == 0, "cannot do fractional minutes.  seconds=" + seconds);
        return seconds / 60;
    }

    public List<Bar> aggregate(List<Bar> bars) {
        List<Bar> result = empty();
        ProtoBar current = null;
        for(Bar bar : bars) {
            if(current == null || bar.date().after(current.date())) {
                if(current != null) result.add(current.asBar());
                current = new ProtoBar(bar);
                Date time = isOnBoundary(bar.date()) ? bar.date() : nextBoundary(bar.date());
                current.setDate(time);
                continue;
            }
            current.updateHLCV(bar);
        }
        if (current != null) result.add(current.asBar());
        return result;
    }

    public String name() {
        return name;
    }

    public Range range(String date) {
        return range(date(date));
    }

    public Range range(Date date) {
        Date next = nextBoundary(date);
        return new Range(rewind(next), next);
    }



}
