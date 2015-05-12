package futures;

import static db.clause.Clause.*;
import static db.tables.TSDB.FuturesOptionStrikesBase.*;
import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import util.*;

import db.*;
import static db.tables.TSDB.FuturesOptionExpiryBase.*;
import db.clause.*;
import db.tables.TSDB.*;

public class FuturesOptionTable extends FuturesOptionDetailsBase {
    private static final long serialVersionUID = 1L;
	public static final FuturesOptionTable OPTIONS = new FuturesOptionTable();

	public FuturesOptionTable() {
		super("options");
		
	}

	public void insert(int id, String optionName, int numQuarterly, int numMonthly, String expiryQuarterly, String expiryMonthly, int monthsLag) {
		insert(
			C_FUTURES_ID.with(id), 
			C_CONTRACT.with(optionName),
			C_NUM_MONTHLY.with(numMonthly),
			C_NUM_QUARTERLY.with(numQuarterly),
			C_EXPIRY_QUARTERLY.with(expiryQuarterly),
			C_EXPIRY_MONTHLY.with(expiryMonthly), 
			C_MONTHS_LAG.with(monthsLag)
		);
	}
	
	public void setStrikes(String optionName, String frequency, Double[] steps, Integer[] numbers) {
		bombUnless(steps.length == numbers.length, "strike steps count does not match number count!");
		FuturesOptionStrikesBase t = T_FUTURES_OPTION_STRIKES;
		List<Row> strikes = empty();
		int optionId = C_ID.value(nameMatches(optionName));
		for(int i = 1; i <= steps.length; i++) 
			strikes.add(new Row(
				t.C_OPTION_ID.with(optionId), 
				t.C_FREQUENCY.with(frequency),
				t.C_PERIODS_OUT.with(i),
				t.C_NUM_STRIKES.with(numbers[i - 1]),
				t.C_STRIKE_STEP.with(steps[i - 1])
			));
		t.insert(strikes);
	}

	private Clause nameMatches(String optionName) {
		return C_CONTRACT.is(optionName);
	}

	public int quarters(String optionName) {
		return C_NUM_QUARTERLY.value(nameMatches(optionName));
	}

	public boolean has(int futuresId, String optionName) {
		return rowExists(C_FUTURES_ID.is(futuresId).and(nameMatches(optionName)));
	}

	public int id(String name) {
		return C_ID.value(nameMatches(name));
	}

	public StrikeSequence strikeSequence(int id, String frequency, int periodsOut) {
		FuturesOptionStrikesBase t = T_FUTURES_OPTION_STRIKES;
		Row row = t.row(t.C_FREQUENCY.is(frequency).and(t.C_OPTION_ID.is(id)).and(t.C_PERIODS_OUT.is(periodsOut)));
		return new StrikeSequence(row.value(t.C_STRIKE_STEP), row.value(t.C_NUM_STRIKES));
	}

	public int months(String optionName) {
		return C_NUM_MONTHLY.value(nameMatches(optionName));
	}

	public void setExpiry(String name, YearMonth ym, Date date) {
		FuturesOptionExpiryBase t = T_FUTURES_OPTION_EXPIRY;
		t.insert(
			t.C_OPTION_ID.with(id(name)),
			ym.in(t.C_YEARMONTH),
			t.C_EXPIRY.with(date)
		);
	}

	public String expiryQuarterly(String name) {
		return C_EXPIRY_QUARTERLY.value(nameMatches(name));
	}

	public String underlyingContract(String name) {
		return FuturesTable.FUTURES.contract(C_FUTURES_ID.value(nameMatches(name)));
	}

	public List<String> options(int futuresId) {
		return C_CONTRACT.values(C_FUTURES_ID.is(futuresId));
	}

	@Deprecated
	public void deleteAll() {
		T_FUTURES_OPTION_EXPIRY.deleteAll(TRUE);
		T_FUTURES_OPTION_STRIKES.deleteAll(TRUE);
		deleteAll(TRUE);
	}

	public String expiryMonthly(String name) {
		return C_EXPIRY_MONTHLY.value(nameMatches(name));
	}

	public int monthsLag(String optionName) {
		return C_MONTHS_LAG.value(nameMatches(optionName));
	}



}
