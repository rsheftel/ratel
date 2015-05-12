package systemdb.metadata;

import static util.Dates.*;

import java.util.*;

import util.*;

public class MarketSession {

    private final String open;
    private final String close;
    private final int closeOffset;
    private final QTimeZone timeZone;

    public MarketSession(String open, String close, int closeOffset, QTimeZone timeZone) {
        this.open = open;
        this.close = close;
        this.closeOffset = closeOffset;
        this.timeZone = timeZone;
    }
    
    private Date timeOn(String time, Date midnight) {
        return timeZone.toLocalTime(Dates.timeOn(time, midnight));
    }

    public Date openOn(Date day) {
        return timeOn(open, day);
    }
    
    public Date processCloseAt(Date day) {
        return secondsAgo(closeOffsetSeconds(), closeOn(day));
    }

    public Date closeOn(Date day) {
        return timeOn(close, day);
    }
    
    public Date processCloseAt() {
        return processCloseAt(midnight());
    }
    
    public Date open() {
        return openOn(midnight());
    }

    public Date close() {
        return closeOn(midnight());
    }

    public int closeOffsetSeconds() {
        return closeOffset;
    }

    public boolean isOpen() {
        return !now().before(open()) && now().before(close());
    }
}