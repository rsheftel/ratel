package systemdb.metadata;

import static systemdb.metadata.MarketHistoryTable.*;
import static util.Dates.*;
import static util.Errors.*;

import java.util.*;

import systemdb.data.*;
import util.*;
import db.*;
public class MarketPeriod {
    private final Symbol market;
    private final Date start;
    private final Date end;
    public MarketPeriod(Row r) {
        this(
            new Symbol(r.value(MARKET_HISTORY.C_MARKET)), 
            r.value(MARKET_HISTORY.C_STARTDATE),
            r.value(MARKET_HISTORY.C_ENDDATE)
        );
    }
    public MarketPeriod(Symbol market, Date start, Date end) {
        this.market = market;
        this.start = start;
        this.end = end;
    }
    public boolean hasStart() {
        return start != null;
    }
    public boolean hasEnd() {
        return end != null;
    }
    public Date end() {
        return end;
    }
    public MarketPeriod before() {
        bombUnless(hasStart(), "can only get period before on started periods!");
        return new MarketPeriod(market, null, daysAgo(1, start));
    }
    public MarketPeriod endUntilStartOf(MarketPeriod period) {
        return new MarketPeriod(market, daysAhead(1, end), daysAgo(1, period.start));
    }
    public MarketPeriod after() {
        return new MarketPeriod(market, daysAhead(1, end), null);
    }
    public Date start() {
        return start;
    }
    
    @Override public String toString() {
        return market + " from " + ymdHuman(start()) + " to " + ymdHuman(end());
    }
    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((market == null) ? 0 : market.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        return result;
    }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final MarketPeriod other = (MarketPeriod) obj;
        if (end == null) {
            if (other.end != null) return false;
        } else if (!end.equals(other.end)) return false;
        if (market == null) {
            if (other.market != null) return false;
        } else if (!market.equals(other.market)) return false;
        if (start == null) {
            if (other.start != null) return false;
        } else if (!start.equals(other.start)) return false;
        return true;
    }
    public boolean overlaps(Range range) {
        return range().overlaps(range);
    }
    public Range range() {
        return Range.range(start, end);
    }
}