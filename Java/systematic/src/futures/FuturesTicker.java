package futures;

import static futures.MonthCode.*;
import static tsdb.Attribute.*;
import static util.Dates.*;
import static util.Strings.*;

import java.util.*;

import tsdb.*;
import util.*;
import db.tables.TSDB.*;

public class FuturesTicker implements Comparable<FuturesTicker> {

	private final String name;

	public FuturesTicker(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}
	
	@Override public String toString() {
		return name();
	}

	public void createIfNeeded() {
		AttributeValue me = TICKER.value(name);
		if (me.exists()) return;
		me.create(TickerBase.T_TICKER.C_TICKER_DESCRIPTION.with(name  + " future"));
	}

	public String year() {
		return name.substring(name.length()-6, name.length()-2);
	}

	public MonthCode month() {
		return MonthCode.fromNumber(name.substring(name.length()-2, name.length()));
	}
	
	public String contract() {
		return name.substring(0, name.length()-6);
	}

	public String bloomberg() {
		return contract().toUpperCase() + month().name() + year().charAt(3);
	}

	private static FuturesTicker ticker(String name, Date asOf, MonthCode month) {
		return new FuturesTicker(name + calendar(asOf).get(Calendar.YEAR) + leftZeroPad(month.ordinal() + 1, 2));
	}
	
	public static FuturesTicker quarterlyTicker(String name, Date asOf) {
		return ticker(name, asOf, quarter(asOf));
	}

	public static FuturesTicker monthlyTicker(String name, Date asOf) {
		return ticker(name, asOf, MonthCode.month(asOf));
	}

	public OptionTicker optionTicker(double strike) {
		return new OptionTicker(this, strike);
	}

	@Override public int compareTo(FuturesTicker o) {
		return sortableName().compareTo(o.sortableName());
	}

	protected String sortableName() {
		return name;
	}

	public YearMonth yearMonth() {
		return new YearMonth(name.substring(name.length()-6, name.length()));
	}

}
