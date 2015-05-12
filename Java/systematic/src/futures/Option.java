package futures;

import static futures.FuturesOptionTable.*;
import static tsdb.AttributeValues.*;
import static futures.MonthCode.*;
import static java.util.Collections.*;
import static tsdb.Attribute.*;
import static util.Dates.*;
import static util.Objects.*;
import static util.Range.*;
import static futures.BloombergField.*;
import java.util.*;

import tsdb.*;
import util.*;

public abstract class Option {

	protected final String name;
	protected final Contract futures;

	public Option(Contract futures, String name) {
		this.futures = futures;
		this.name = name;
	}
	
	public abstract void setDetails(int numQuarterly, int numMonthly, String expiryQuarterly, String expiryMonthly, int monthsLag);
	public abstract void setStrikes(String frequency, Double[] steps, Integer[] number);
	

	public void setDetails(int numQuarterly, int numMonthly, String expiryType) {
		setDetails(numQuarterly, numMonthly, expiryType, expiryType);
	}
	
	public void setDetails(int numQuarterly, int numMonthly, String expiryQuarterly, String expiryMonthly) {
		setDetails(numQuarterly, numMonthly, expiryQuarterly, expiryMonthly, 0);
	}
	
	boolean contractExpired(YearMonth ym, Date start) {
		return expiry(ym).isExpired(ym, start);
	}

	public List<OptionTicker> tickers(Date asOf, DataSource source) {
		int numQuarters = quarterlies();
		int numMonthly = monthlies();
		int monthsLag = monthsLag();
		Range yesterday = range(businessDaysAgo(1, midnight(asOf), "nyb"));
		Date start = asOf;
		try {
			while(contractExpired(yearMonth(asOf), start))
				asOf = monthsAhead(1, asOf);
		} catch (ExpiryNotFoundException e) {
			handleMissingExpiry(asOf);
			return empty();
		}
		
		start = asOf;
		List<OptionTicker> result = empty();
		for(int i = 1; i <= numQuarters; i++) {
			FuturesTicker prefix = FuturesTicker.quarterlyTicker(name, asOf);
			Date underlyingAsOf = monthsAhead(monthsLag, asOf);
			double close;
			try {
				close = closingFuturesPrice(source, yesterday, futures.quarterlyTicker(underlyingAsOf));
			} catch (Exception e) {
				handleMissingClose(underlyingAsOf, source, yesterday, e);
				continue;
			}
			result.addAll(strikeSequence("quarterly", i).tickers(close, prefix));
			asOf = monthsAhead(3, asOf);
		}
		
		asOf = start;
		for(int i = 1; i <= numMonthly; i++) {
			if(quarter(asOf).equals(month(asOf)))
				asOf = monthsAhead(1, asOf);				
			FuturesTicker monthly = FuturesTicker.monthlyTicker(name, asOf);
			Date underlyingAsOf = monthsAhead(monthsLag, asOf);
			double close;
			try {
				close = closingFuturesPrice(source, yesterday, futures.quarterlyTicker(underlyingAsOf));
			} catch (Exception e) {
				handleMissingClose(underlyingAsOf, source, yesterday, e);
				continue;
			}
			result.addAll(strikeSequence("monthly", i).tickers(close, monthly));
			asOf = monthsAhead(1, asOf);
		}
		sort(result);
		return result;
	}
	
	protected abstract int monthsLag();

	protected abstract int monthlies();
	
	protected abstract int quarterlies();
	
	protected abstract StrikeSequence strikeSequence(String frequency, int periodsOut);

	double closingFuturesPrice(DataSource source, Range yesterday, FuturesTicker ticker) {
		SeriesSource ss = new SeriesSource(ContractCurrent.series(ticker, FUTURES_PRICE.tsdb()), source);
		return ss.observations(yesterday).value();
	}

	protected abstract void handleMissingClose(Date asOf, DataSource source, Range yesterday, Exception e);

	protected abstract void handleMissingExpiry(Date asOf);


	public abstract void setExpiry(YearMonth ym, Date date);

	public void createTimeSeries(List<OptionTicker> tickers) {
		for (OptionTicker ticker : tickers)
			createTimeSeries(ticker);
	}

	protected void createTimeSeries(OptionTicker ticker) {
		ticker.createIfNeeded();
		Date date = ticker.yearMonth().first();
		date = monthsAhead(monthsLag(), date);
		FuturesTicker future = futures.quarterlyTicker(date);
		future.createIfNeeded();
		String underlyingContract = futures.name;
		MonthCode underlyingMonth = future.month().toQuarter();
		AttributeValue strikeValue = STRIKE.value(ticker.prettyStrike());
		strikeValue.createIfNeeded();
		AttributeValues attributes = values(
			INSTRUMENT.value("futures_option"),
			TICKER.value(ticker.future().name()),
			QUOTE_TYPE.value("close"),
			QUOTE_SIDE.value("mid"),
			QUOTE_CONVENTION.value("replaceMe"),
	
			CONTRACT.value(underlyingContract),
			FUTURE_YEAR.value(future.year()),
			FUTURE_MONTH.value(underlyingMonth.numberString()),
			FUTURE_MONTH_LETTER.value(underlyingMonth.letter()),
			
			OPTION_CONTRACT.value(name),
			OPTION_YEAR.value(ticker.year()),
			OPTION_MONTH.value(ticker.month().numberString()),
			OPTION_MONTH_LETTER.value(ticker.month().letter()),
			strikeValue,
			OPTION_TYPE.value("replaceMe"),
			EXPIRY.value("actual")
		);
		expiry(ticker.yearMonth()).addTo(attributes, ticker.yearMonth());
		for (String optionType : array("call", "put")) {
			for (String quoteConvention : array("price", "vol_ln", "vol_bp", "vol_bp_daily", "delta")) {
				attributes.replace(QUOTE_CONVENTION.value(quoteConvention));
				attributes.replace(OPTION_TYPE.value(optionType));
				TimeSeries ts = new TimeSeries(optionSeries(attributes));
				ts.createIfNeeded(attributes);
			}
		}
	}

	private String optionSeries(AttributeValues attributes) {
		return attributes.join("_", TICKER, STRIKE, OPTION_TYPE, QUOTE_CONVENTION, QUOTE_SIDE);
	}
	
	public Expiry expiry(String yearMonth) { 
	    return expiry(new YearMonth(yearMonth));
	}

	public Expiry expiry(YearMonth yearMonth) {
		String expiry = MonthCode.isQuarter(yearMonth) 
			? OPTIONS.expiryQuarterly(name) 
			: OPTIONS.expiryMonthly(name);
		return Expiry.lookup(expiry, this);
	}

	public abstract Expiry expiryLookupTable();

}