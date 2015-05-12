package systemdb.data;

import static util.Strings.*;

import java.io.*;
import java.util.*;

import util.*;

public class Bar implements Comparable<Bar>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final long NA = Long.MIN_VALUE + 123;
    private final Date date;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final Long volume;
    private final Long openInterest;

    public Bar(Date date, double open, double high, double low, double close, Long volume, Long openInterest) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.openInterest = openInterest;
    }

    @SuppressWarnings("unchecked") @Override public String toString() {
        return paren(commaSep(strings(date, open, high, low, close, volume, openInterest)));
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(close);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        temp = Double.doubleToLongBits(high);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(low);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(open);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((openInterest == null) ? 0 : openInterest.hashCode());
        result = prime * result + ((volume == null) ? 0 : volume.hashCode());
        return result;
    }



    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Bar other = (Bar) obj;
        if (Double.doubleToLongBits(close) != Double.doubleToLongBits(other.close)) return false;
        if (date == null) {
            if (other.date != null) return false;
        } else if (!date.equals(other.date)) return false;
        if (Double.doubleToLongBits(high) != Double.doubleToLongBits(other.high)) return false;
        if (Double.doubleToLongBits(low) != Double.doubleToLongBits(other.low)) return false;
        if (Double.doubleToLongBits(open) != Double.doubleToLongBits(other.open)) return false;
        if (openInterest == null) {
            if (other.openInterest != null) return false;
        } else if (!openInterest.equals(other.openInterest)) return false;
        if (volume == null) {
            if (other.volume != null) return false;
        } else if (!volume.equals(other.volume)) return false;
        return true;
    }



    public boolean in(Range range) {
        return range.containsInclusive(date);
    }

    public Date date() {
        return date;
    }
    
    public double open() {
        return open;
    }

    public double close() {
        return close;
    }
    
    public double high() {
        return high;
    }
    
    public double low() {
        return low;
    }
    
    public Long openInterest() {
        return openInterest;
    }
    
    public Long volume() {
        return volume;
    }

    public long rOpenInterest() {
        return openInterest == null ? NA : openInterest;
    }
    
    public long rVolume() {
        return openInterest == null ? NA : volume;
    }
    
    
    public static long na() {
        return NA;
    }

    @Override public int compareTo(Bar o) {
        return date.compareTo(o.date);
    }
}
