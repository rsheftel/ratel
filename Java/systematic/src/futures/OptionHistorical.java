package futures;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import tsdb.*;
import util.*;

public class OptionHistorical extends Option {

	private int numQuarterly;
	private int numMonthly;
	private String expiryQuarterly;
	private ExpiryTable expiryTable = new ExpiryTable();
	Map<String, List<StrikeSequence>> strikes = emptyMap();
	private int monthsLag;

	class ExpiryTable extends Expiry {
		private Map<YearMonth, Date> expiries = new HashMap<YearMonth, Date>();
		
		@Override public Date expiration(YearMonth yearMonth) {
			return bombNull(expiries.get(yearMonth), "no expiration defined for " + yearMonth);
		}
		
		public void put(YearMonth yearMonth, Date date) {
			expiries.put(yearMonth, date);
		}
	}
	
	public OptionHistorical(Contract futures, String name) {
		super(futures, name);
	}

	@Override protected void handleMissingClose(Date asOf, DataSource source, Range yesterday, Exception e) {
		bomb("missing close for date " + asOf + " source " + source + " " + yesterday, e);
	}

	@Override protected void handleMissingExpiry(Date asOf) {
		bomb("missing expiry for date " + asOf);
	}

	@Override public void setDetails(int numQuarterly, int numMonthly, String expiryQuarterly, String expiryMonthly, int monthsLag) {
		this.numQuarterly = numQuarterly;
		this.numMonthly = numMonthly;
		this.expiryQuarterly = expiryQuarterly;
		this.monthsLag = monthsLag;
	}
	
	@Override public Expiry expiry(YearMonth yearMonth) {
		return Expiry.lookup(expiryQuarterly, this);
	}

	@Override public void setStrikes(String frequency, Double[] steps, Integer[] number) {
		bombUnless(steps.length == number.length, "steps length does not match number length");
		List<StrikeSequence> freqStrikes = empty();
		strikes.put(frequency, freqStrikes);
		for(int i = 0; i < steps.length; i++) 
			freqStrikes.add(new StrikeSequence(steps[i], number[i]));
	}

	@Override protected StrikeSequence strikeSequence(String frequency, int periodsOut) {
		List<StrikeSequence> freqStrikes = bombNull(strikes.get(frequency), "no strikes defined for " + frequency);
		bombIf(periodsOut > freqStrikes.size(), "not enough periods to get strikes for " + frequency + " " + periodsOut);
		return freqStrikes.get(periodsOut - 1);
	}

	@Override public void setExpiry(YearMonth ym, Date date) {
		expiryTable.put(ym, date);
	}

	@Override protected int monthlies() {
		return numMonthly;
	}

	@Override protected int quarterlies() {
		return numQuarterly;
	}

	@Override public Expiry expiryLookupTable() {
		return expiryTable;
	}

	@Override protected int monthsLag() {
		return monthsLag;
	}

}
