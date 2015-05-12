package futures;

import static futures.MonthCode.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import db.*;

import tsdb.*;

public abstract class Contract {

	protected final String name;

	public Contract(String name) {
		this.name = name;
	}
	
	public List<FuturesTicker> futuresTickers(Date asOf) {
		int count = quarterlies();
		List<FuturesTicker> result = empty();
		if(noExpiry()) {
			result.add(FuturesTicker.quarterlyTicker(name, asOf));
			if(!quarter(asOf).equals(month(asOf)))
				count--;
		} else {
		    if(!expiry().isExpired(quarter(asOf).yearMonth(asOf), asOf)) {
				result.add(FuturesTicker.quarterlyTicker(name, asOf));
				count--;
			}
		}
		for(int i = 0; i < count; i++) {
			asOf = monthsAhead(3, asOf);
			result.add(FuturesTicker.quarterlyTicker(name, asOf));
		}
		return result;
	}
	
	private boolean noExpiry() {
		return expiryType().equals("None");
	}

	public Expiry expiry() {
		return Expiry.lookup(expiryType());
	}

	protected abstract String expiryType();

	public List<TimeSeries> priceSeries(Date asOf) {
		List<FuturesTicker> tickers = futuresTickers(asOf);
		createTimeSeries(tickers);
		List<TimeSeries> result = empty();
		for (FuturesTicker ticker : tickers)
			result.add(series(ticker + "_price_mid"));
		return result;
	}

	protected abstract int quarterlies();

	public void createTimeSeries(List<FuturesTicker> tickers) {
		for (FuturesTicker ticker : tickers)
			createTimeSeries(ticker);
	}

	public void createTimeSeries(FuturesTicker ticker) {
		ticker.createIfNeeded();
		AttributeValue year = FUTURE_YEAR.value(ticker.year());
		if(!year.exists()) year.create();
		AttributeValues attributes = values(
			INSTRUMENT.value("futures"),
			TICKER.value(ticker.name()),
			QUOTE_TYPE.value("close"),
			QUOTE_SIDE.value("mid"),
			QUOTE_CONVENTION.value("price"),
			CONTRACT.value(name),
			year,
			FUTURE_MONTH.value(ticker.month().numberString()),
			FUTURE_MONTH_LETTER.value(ticker.month().letter()),
	        EXPIRY.value("actual")
		);
		expiry().addTo(attributes, ticker.yearMonth());
		TimeSeries price = new TimeSeries(attributes.join("_", TICKER, QUOTE_CONVENTION, QUOTE_SIDE));
		price.createIfNeeded(attributes);
		attributes.remove(QUOTE_SIDE);
		attributes.replace(QUOTE_CONVENTION.value("volume"));
		createTimeSeries(attributes);
		attributes.replace(QUOTE_CONVENTION.value("convexity"));
		createTimeSeries(attributes);
		attributes.replace(QUOTE_CONVENTION.value("dv01"));
		createTimeSeries(attributes);
		
	}
	
	public static void main(String[] args) {
	    Date date = date("1995/01/01");
	    Contract di = new ContractCurrent("di", "Comdty");
	    while(date.before(now())) {
	        di.createTimeSeries(di.futuresTickers(date));
	        Db.commit();
	        date = monthsAhead(1, date);
	    }
    }

	private void createTimeSeries(AttributeValues attributes) {
		TimeSeries ts = new TimeSeries(attributes.join("_", TICKER, QUOTE_CONVENTION));
		ts.createIfNeeded(attributes);
	}

	public FuturesTicker quarterlyTicker(Date asOf) {
		return FuturesTicker.quarterlyTicker(name, asOf);
	}

}