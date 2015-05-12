package systemdb.data.bars;

import static java.lang.Math.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Strings.*;

import java.util.*;

import systemdb.data.*;

public class ProtoBar {
    Date date;
    Double open;
    Double high;
    Double low;
    Double close;
    Long volume;
    Long openInterest;
    
    public ProtoBar() {}
    
    public ProtoBar(Bar bar) {
        date = bar.date();
        open = bar.open();
        high = bar.high();
        low = bar.low();
        close = bar.close();
        volume = bar.volume();
        openInterest = bar.openInterest();
    }

    public void addTo(String quoteType, double value) {
        if (quoteType.equals("open")) open += value;
        else if (quoteType.equals("high")) high += value;
        else if (quoteType.equals("low")) low += value;
        else if (quoteType.equals("close")) close += value;
        else bomb("unknown quoteType " + quoteType);
    }
    
    public Bar asBar() {
        try {
            return new Bar(date, open, high, low, close, volume, openInterest);
        } catch (RuntimeException e) {
            throw bomb("could not create bar from protobar \n" + this, e);
        }
    }
    
    @Override public String toString() {
        return ymdHuman(date) + " OHLC:" + commaSep(open, high, low, close) + " V:" + volume + " I:" + openInterest;
    }

    public Bar fromClose(double priorClose) {
        try {
            return new Bar(
                date,
                priorClose, max(close, priorClose), min(close, priorClose), close,
                volume,
                openInterest
            );
        } catch (RuntimeException e) {
            throw bomb("could not create bar from protobar prior CLOSE " + priorClose + "\n" + this, e);
        }
    }

    public void setDate(Date newDate) {
        date = newDate;
    }

    public void update(Bar bar) {
        updateHLCV(bar);
        setDate(bar.date());
    }

	public void updateHLCV(Bar bar) {
		close = bar.close();
        high = Math.max(high, bar.high());
        low = Math.min(low, bar.low());
        volume += bar.volume();
	}

    public Date date() {
        return date;
    }
}