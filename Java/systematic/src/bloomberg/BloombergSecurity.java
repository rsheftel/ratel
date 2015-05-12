package bloomberg;

import static bloomberg.BloombergSession.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Range.*;
import static util.Strings.*;

import java.util.*;

import systemdb.data.*;
import tsdb.*;
import util.*;

import com.bloomberglp.blpapi.*;

public class BloombergSecurity {
    public static final Date BBG_START_HISTORICAL = date("1861/01/01");
    public static final Date BBG_END_HISTORICAL = date("2099/12/31");
    public static final Date BBG_START_INTRADAY = date("1970/01/01");
    public static final Date BBG_END_INTRADAY = date("2038/01/17");
    private final String key;

    public BloombergSecurity(String key) {
        this.key = key.toUpperCase();
    }

    public static BloombergSecurity security(String key) {
        return new BloombergSecurity(key);
    }

    public void addTo(Request request) {
        request.append("securities", key);
    }


    public void setOn(Request request) {
        request.set("security", key);
    }

    @Override public String toString() {
        return key;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final BloombergSecurity other = (BloombergSecurity) obj;
        if (key == null) {
            if (other.key != null) return false;
        } else if (!key.equals(other.key)) return false;
        return true;
    }

    public String string(String fieldName) {
        BloombergSession session = BloombergSession.session();
        Service refDataService = session.dataService();
        Request request = refDataService.createRequest("ReferenceDataRequest");
        request.append("securities", key);
        request.append("fields", fieldName);
        Message response = BloombergSession.the(the(session.responses(list(request))));
        try {
            Element data = fieldData(response);
            return BloombergSession.string(data, fieldName);
        } catch(Exception e) {
            throw handleError(request, response);
        }
    }

    private Element fieldData(Message response) {
        List<Element> dataItems = elements(element(response, "securityData"));
        Element data = element(the(dataItems), "fieldData");
        return data;
    }

    public double numeric(String fieldName) {
        String value = string(fieldName);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw bomb("couldn't parse \n" + sQuote(value) + " as a double for field name \n" + fieldName + " on security \n" + this, e);
        }
    }

    public double numeric(String fieldName, Date date) {
    	return observations(fieldName, range(date, date)).value();
    }

    private RuntimeException handleError(Request request, Message response) {
        return bomb("exception processing request " + request + "\nresponse:\n" + response);
    }

    private String bbgDate(Date d) {
        return String.valueOf(asLong(midnight(d)));
    }


	public Observations observations(String fieldName, Range range) {
        BloombergSession session = BloombergSession.session();
        Service refDataService = session.dataService();
        Request request = refDataService.createRequest("HistoricalDataRequest");
        request.set("startDate", bbgDate(range.hasStart() && range.start().after(BBG_START_HISTORICAL) ? range.start() : BBG_START_HISTORICAL));
        request.set("endDate", bbgDate(range.hasEnd() && range.end().before(BBG_END_HISTORICAL) ? range.end() : BBG_END_HISTORICAL));
        request.append("securities", key);
        request.append("fields", fieldName);
        Message response = BloombergSession.the(the(session.responses(list(request))));
        try {
            Element securityData = element(response, "securityData");
            List<Element> fieldDatas = elements(element(securityData, "fieldData"));
            Observations result = new Observations();
            for (Element fieldData : fieldDatas) {
            	double value = BloombergSession.numeric(fieldData, fieldName);
            	Date date = BloombergSession.date(fieldData, "date");
            	result.set(date, value);
			}
            return result;
        } catch(Exception e) {
            throw handleError(request, response);
        }
	}


	public List<Bar> bars(Range range, Interval interval) {
		BloombergSession session = BloombergSession.session();
        Service refDataService = session.dataService();
        Request request = refDataService.createRequest("IntradayBarRequest");
		request.set("security", key);
        request.set("eventType", "TRADE");
        request.set("interval", interval.minutes());// bar interval in minutes
        request.set("startDateTime", datetime(range.hasStart() && range.start().after(BBG_START_INTRADAY) ? range.start() : BBG_START_INTRADAY));
        request.set("endDateTime", datetime(range.hasEnd() && range.end().before(BBG_END_INTRADAY) ? range.end() : BBG_END_INTRADAY));
        List<Bar> result = empty();
        List<Event> responses = session.responses(list(request));
        for (Event event : responses) {
            try {
                Message message = BloombergSession.the(event);
                result.addAll(bars(message, interval));
            } catch(Exception e) {
                throw bomb("exception caught processing request:\n" + request, e);
            }
        }
		return result;
	}

    private static List<Bar> bars(Message message, Interval interval) {
        Element barData = element(element(message, "barData"), "barTickData");
        List<Bar> result = empty();
        for (int i = 0; i < barData.numValues(); i++)
            result.add(bar(barData.getValueAsElement(i), interval));
        Collections.sort(result);
        return result;
    }

    private static Bar bar(Element bar, Interval interval) {
        Date date = time(bar, "time");
        double open = BloombergSession.numeric(bar, "open");
        double high = BloombergSession.numeric(bar, "high");
        double low = BloombergSession.numeric(bar, "low");
        double close = BloombergSession.numeric(bar, "close");
        Long volume = number(bar, "volume");
        date = interval.advance(date);
        return new Bar(date, open, high, low, close, volume, null);
    }




}
