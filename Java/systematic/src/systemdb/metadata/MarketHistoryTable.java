package systemdb.metadata;

import static util.Errors.*;
import static util.Objects.*;
import static util.Range.*;

import java.util.*;

import systemdb.data.*;
import util.*;
import db.*;
import db.clause.*;
import db.tables.SystemDB.*;

public class MarketHistoryTable extends MarketHistoryBase {
    private static final long serialVersionUID = 1L;
    public static MarketHistoryTable MARKET_HISTORY = new MarketHistoryTable();

    public MarketHistoryTable() {
        super("mkt_hist");
    }
    
    public List<MarketPeriod> activePeriods(String name) {
        List<MarketPeriod> result = empty();
        Clause matches = C_MARKET.is(name);
        SelectMultiple select = select(matches);
        select.orderBy(C_STARTDATE.ascending());
        for(Row r : select.rows())
            result.add(new MarketPeriod(r));
        List<Range> ranges = empty();
        for(MarketPeriod period : result) ranges.add(period.range());
        Range.requireNoOverlaps(ranges);
        return result;
    }
    
    public void insert(Symbol test, Date start, Date end) {
        requireOrdered(start, end);
        requireNoOverlap(test.name(), range(start, end));
        insertUnchecked(start, end, test.name());
    }

    void insertUnchecked(Date start, Date end, String name) {
        insert(periodRow(start, end, name));
    }

    private Row periodRow(Date start, Date end, String name) {
        return new Row(C_MARKET.with(name), C_STARTDATE.withMaybe(start), C_ENDDATE.withMaybe(end));
    }

    private void requireNoOverlap(String marketName, Range range) {
        for (MarketPeriod period : activePeriods(marketName)) 
            bombIf(period.overlaps(range), 
                "period exists at \n" + period + "\noverlaps \n" + range);
    }

    public List<MarketPeriod> inactivePeriods(List<MarketPeriod> active) {
        List<MarketPeriod> result = empty();
        bombIf(active.isEmpty(), "can't convert empty active list to inactive list.");
        MarketPeriod current = first(active);
        if (current.hasStart()) result.add(current.before());
        for (MarketPeriod period : rest(active)) {
            bombUnless(current.hasEnd(), "can't have period after ended period!");
            result.add(current.endUntilStartOf(period));
            if (period.hasEnd()) current = period;
            else return result;
        }
        if (current.hasEnd()) result.add(current.after());
        return result;
    }

    public void delete(Date start, Date end, String name) {
        deleteOne(periodRow(start, end, name).allMatch());
    }

}
