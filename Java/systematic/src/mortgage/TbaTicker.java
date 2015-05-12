package mortgage;

import static tsdb.DataSource.*;
import static util.Dates.*;
import static util.Strings.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.columns.*;

import tsdb.*;
import util.*;
import futures.*;

public class TbaTicker {

	private final String program;
	private final double coupon;
	private final Date settleMonth;

	public TbaTicker(String program, double coupon, Date settleMonth) {
		this.program = program;
		this.coupon = coupon;
		this.settleMonth = settleMonth;
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(coupon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((program == null) ? 0 : program.hashCode());
		result = prime * result + ((settleMonth == null) ? 0 : settleMonth.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final TbaTicker other = (TbaTicker) obj;
		if (Double.doubleToLongBits(coupon) != Double.doubleToLongBits(other.coupon)) return false;
		if (program == null) {
			if (other.program != null) return false;
		} else if (!program.equals(other.program)) return false;
		if (settleMonth == null) {
			if (other.settleMonth != null) return false;
		} else if (!settleMonth.equals(other.settleMonth)) return false;
		return true;
	}

	public String tsdb(BloombergField field) {
		return join("_", program, nDecimals(1, coupon), yearMonth().string(), field.tsdb());
	}
	
	public TimeSeries series(BloombergField field) {
		return TimeSeries.series(tsdb(field));
	}

	public String bloomberg(BloombergField field) {
		return join(" ", 
			program.toUpperCase(), 
			nDecimals(1, coupon), 
			mmYyyy(settleMonth), 
			field.source(), 
			"Mtge"
		);
	}

	public BloombergJobEntry jobEntry(BloombergField field) {
		return new BloombergJobEntry(bloomberg(field), field.bloomberg(), BLOOMBERG_BBT3.with(series(field)));
	}

	public Clause matches(NcharColumn yearMonthColumn) {
		return yearMonth().matches(yearMonthColumn);
	}

	private YearMonth yearMonth() {
		return Dates.yearMonth(settleMonth);
	}

	public Cell<?> cell(NcharColumn yearMonthColumn) {
		return yearMonth().in(yearMonthColumn);
	}
	
	

}
