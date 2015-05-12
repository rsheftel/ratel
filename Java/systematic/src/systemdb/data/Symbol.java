package systemdb.data;

import static systemdb.data.Interval.*;
import static systemdb.metadata.MarketHistoryTable.*;
import static systemdb.metadata.SystemTimeSeriesTable.*;
import static tsdb.Attribute.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeriesDataTable.*;
import static util.Arguments.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Index.*;
import static util.Objects.*;
import static util.Range.*;
import static util.Strings.*;

import java.io.*;
import java.util.*;

import systemdb.data.bars.*;
import systemdb.metadata.*;
import tsdb.*;
import util.*;

public class Symbol implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final Double bigPointValue;

    public Symbol(String name, double bigPointValue) {
        this.name = name;
        this.bigPointValue = bigPointValue;
    }
    
    public Symbol(String name) {
        this.name = name;
        this.bigPointValue = null;
    }

    private HistoricalDailyData dailyHistorical() {
        return SYSTEM_TS.dailyHistorical(name);
    }
    
    public List<Bar> bars(Range range, Interval interval) {
        if (interval.isDaily())
            return bars(range);
        return adjusted(SYSTEM_TS.intraday(name).bars(range, interval), interval);
    }
    
    public List<Bar> bars(Interval interval) {
        return bars(allTime(), interval);
    }
    
    public List<Bar> bars(Range range) {
        return adjusted(dailyHistorical().bars(range), DAILY);
    }

    public List<Bar> bars() {
        return bars(allTime());
    }

    public List<Bar> lastBars(int count) {
        if(count == 0) return empty();
        return adjusted(dailyHistorical().lastBars(count), DAILY);
    }

    private List<Bar> adjusted(List<Bar> bars, Interval interval) {
        if (!isAdjusted()) return bars;
        Map<Date, ProtoBar> protos = emptyMap();
        for(Bar b : bars) protos.put(b.date(), new ProtoBar(b));
        TsdbObservations adjustments = adjustments(bars, interval);
        for(SeriesSource ss : adjustments) {
            adjust(protos, adjustments, ss, "open");
            adjust(protos, adjustments, ss, "high");
            adjust(protos, adjustments, ss, "low");
            adjust(protos, adjustments, ss, "close");
        }
        List<Bar> resultAdjusted = empty();
        for(Bar b : bars) resultAdjusted.add(protos.get(b.date()).asBar());
        return resultAdjusted;
    }

    private void adjust(Map<Date, ProtoBar> protos, TsdbObservations adjustments, SeriesSource ss, String quoteType) {
        if (!ss.series().attributes().has(QUOTE_TYPE, quoteType)) return; 
        Observations modifiers = adjustments.get(ss);
        for(Date d : modifiers) {
            ProtoBar protoBar = protos.get(d);
            if (protoBar == null) 
                throw bomb("missing underlying date " + ymdHuman(d) + " for adjustment of " + this + " with " + modifiers.value(d) + " " + quoteType);
            protoBar.addTo(quoteType, modifiers.value(d));
        }
    }

    private TsdbObservations adjustments(List<Bar> bars, Interval interval) {
        Range barRange = range(first(bars).date(), last(bars).date());
        AttributeValues values = AttributeValues.values(
            INSTRUMENT.value("adjustment"),
            MARKET.value(name()),
            INTERVAL.value(interval.name())
        );
        return observationsMap(INTERNAL, barRange, values);
    }

    private boolean isAdjusted() {
        return SYSTEM_TS.isUseAdjustment(name());
    }

    public RBarData rBars(Range range) { 
        return Bars.rBars(bars(range));
    }
    
    public RBarData rBars() { 
    	return Bars.rBars(bars());
    }

	public String name() {
        return name;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bigPointValue == null) ? 0 : bigPointValue.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Symbol other = (Symbol) obj;
        if (bigPointValue == null) {
            if (other.bigPointValue != null) return false;
        } else if (!bigPointValue.equals(other.bigPointValue)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    public double bigPointValue() {
        return bombNull(bigPointValue, "contract size not defined");
    }

    @Override public String toString() {
        return paren(commaSep(name, "" + bigPointValue));
    }
    
	public void subscribe(final ObservationListener listener) {
	    live().subscribe(listener);
	}

    public void subscribe(final TickListener listener) {
		live().subscribe(listener);
	}

	public static void main(String[] args) {
	    Arguments arguments = arguments(args, list("symbol", "price"));
	    Symbol s = new Symbol(arguments.get("symbol"));
	    double price = Double.valueOf(arguments.get("price"));
        s.jmsLive().publish(new Tick(price, 1, price, price, price, now()));
    }

    public JmsLiveDescription jmsLive() {
        return (JmsLiveDescription) live();
    }

    public LiveDataSource live() {
        return SYSTEM_TS.live(name);
    }

    public Observations observations(Range range) {
        return Observations.closes(bars(range)) ;
    }

    public Observations observations() {
        return Observations.closes(bars());
    }

    public String type() {
        return SYSTEM_TS.type(name);
    }

	public int barCount() {
		return bars().size();
	}

	public Date firstBarDate() {
		return first(bars()).date();
	}
	
	public String currency() {
	    return 
	    SYSTEM_TS.currency(name());
	}

	public Object lastBarDate() {
		return last(bars()).date();
	}
	
	public List<MarketPeriod> activePeriods() {
        return MARKET_HISTORY.activePeriods(name());
    }

    public void addPeriod(String start, String end) {
        addPeriod(dateMaybe(start), dateMaybe(end));
    }

    public void addPeriod(Date start, Date end) {
        MARKET_HISTORY.insert(this, start, end);
    }

    public List<MarketPeriod> inactivePeriods() {
        return MARKET_HISTORY.inactivePeriods(activePeriods());
    }

    public Date[] starts() {
        List<MarketPeriod> active = activePeriods();
        Date[] result = new Date[active.size()];
        for (Index<MarketPeriod> i : indexing(active))
            result[i.num] = i.value.start();
        return result;
    }

    public Date[] ends() {
        List<MarketPeriod> active = activePeriods();
        Date[] result = new Date[active.size()];
        for (Index<MarketPeriod> i : indexing(active))
            result[i.num] = i.value.end();
        return result;
    }

    public boolean hasPeriods() {
        return !activePeriods().isEmpty();
    }

    
}
