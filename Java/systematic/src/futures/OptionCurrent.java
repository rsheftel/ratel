package futures;

import static futures.BloombergField.*;
import static futures.FuturesOptionTable.*;
import static mail.Email.*;
import static transformations.Constants.*;
import static tsdb.DataSource.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import tsdb.*;
import util.*;
import db.tables.TSDB.*;

public class OptionCurrent extends Option implements BloombergLoadable {

	private ContractCurrent current;

	public OptionCurrent(ContractCurrent futures, String name) {
		super(futures, name);
		this.current = futures;
	}

	@Override public String toString() {
		return name + ": underlying(" + futures + ")";
	}

	int id() {
		return OPTIONS.id(name);
	}	

	@Override protected int monthlies() {
		return OPTIONS.months(name);
	}
	
	@Override protected int quarterlies() {
		return OPTIONS.quarters(name);
	}
	
	@Override public List<BloombergField> fields() {
		return list(FUTURES_PRICE);
	}
	
	class Table extends Expiry {
		@Override public Date expiration(YearMonth yearMonth) {
			FuturesOptionExpiryBase t = FuturesOptionExpiryBase.T_FUTURES_OPTION_EXPIRY;
			int optionId = id();
			try {
				return t.C_EXPIRY.value(t.C_OPTION_ID.is(optionId).and(yearMonth.matches(t.C_YEARMONTH)));
			} catch (RuntimeException e) {
				throw new ExpiryNotFoundException(optionId, yearMonth);
			}
		}
	}
	
	@Override protected void handleMissingExpiry(Date asOf) {
		problem(
			"Missing expiry for " + this, 
			"Missing expiry for " + yearMonth(asOf) + " for " + this + ".\n" + 
				"Please add the expiry data to the futures_option_expiry table in TSDB.  The option_id is " + id() + ".\n" +
				"Skipping...\n"
		).sendTo(FAILURE_ADDRESS);
	}
	
	@Override protected void handleMissingClose(Date asOf, DataSource source, Range yesterday, Exception e) {
		problem(
			"Missing futures price", 
			"Missing price for " + futures.quarterlyTicker(asOf) + ", " + yesterday + " in source " + source + ".\n" +
				"Cannot calculate option strikes for " + this + ".\n" + e.getMessage() + " " + trace(e)
		).sendTo(FAILURE_ADDRESS);
	}
	
	@Override protected StrikeSequence strikeSequence(String frequency, int periodsOut) {
		return OPTIONS.strikeSequence(id(), frequency, periodsOut);
	}


	@Override public void setDetails(int numQuarterly, int numMonthly, String expiryQuarterly, String expiryMonthly, int monthsLag) {
		OPTIONS.insert(current.id(), name, numQuarterly, numMonthly, expiryQuarterly, expiryMonthly, monthsLag);
	}
	
	@Override public void setStrikes(String frequency, Double[] steps, Integer[] number) {
		OPTIONS.setStrikes(name, frequency, steps, number);
	}

    @Override public void setExpiry(YearMonth ym, Date date) {
        OPTIONS.setExpiry(name, ym, date);
    }

    public void setExpiry(String ym, Date date) {
        setExpiry(new YearMonth(ym), date);
    }

	@Override public List<BloombergJobEntry> jobEntries(Date asOf, BloombergField field) {
		List<BloombergJobEntry> result = empty();
		List<OptionTicker> tickers = tickers(asOf, BLOOMBERG);
		List<OptionTicker> tickersWithNoExpiry = empty();
		for (OptionTicker ticker : tickers) {
			try {
				createTimeSeries(ticker);
			} catch (ExpiryNotFoundException e) {
				tickersWithNoExpiry.add(ticker);
				continue;
			}
			for(String callPut : array("call", "put")) {
				TimeSeries series = TimeSeries.series(join("_", ticker.toString(), callPut, field.tsdb()));
				String source = field.source();
				source += isEmpty(source) ? "" : " ";
				result.add(new BloombergJobEntry(ticker.bloomberg(callPut) + " " + source + current.yellowKey(), field.bloomberg(), BLOOMBERG.with(series)));
			}
		}
		emailBadTickers(tickersWithNoExpiry);
		return result ;
	}

	private void emailBadTickers(List<OptionTicker> tickersWithNoExpiry) {
		if(tickersWithNoExpiry.isEmpty()) return;
		List<String> tickerNames = empty();
		for (FuturesTicker t : tickersWithNoExpiry) 
			tickerNames.add(t.name());
		problem(
			"cannot create time series for options - expiry not defined",
			"The following options cannot have time series created because their expiry is not defined.\n" + 
			"Please add their data to the futures_option_expiry table in TSDB.  The option_id is " + id() + "\n" +
			join("\n", tickerNames)
		).sendTo(FAILURE_ADDRESS);
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((futures == null) ? 0 : futures.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final OptionCurrent other = (OptionCurrent) obj;
		if (futures == null) {
			if (other.futures != null) return false;
		} else if (!futures.equals(other.futures)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}

	@Override public BloombergJob job(BloombergField field) {
		return current.optionJob(field);
	}

	@Override public Expiry expiryLookupTable() {
		return new Table();
	}

	@Override protected int monthsLag() {
		return OPTIONS.monthsLag(name);
	}



}
