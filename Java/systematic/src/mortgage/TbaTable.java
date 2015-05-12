package mortgage;

import static tsdb.TimeSeries.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Sequence.*;
import static util.Strings.*;

import java.util.*;

import tsdb.*;
import static mortgage.TbaSettleTable.*;
import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.TSDB.*;
import futures.*;
import static futures.BloombergField.*;

public class TbaTable extends TbaDetailsBase {
    private static final long serialVersionUID = 1L;
	public static final TbaTable TBA = new TbaTable();
	
	public TbaTable() {
		super("tba");
	}

	
	public class Tba extends Row implements BloombergLoadable {
	    private static final long serialVersionUID = 1L;
	    public Tba(Row r) {
			super(r);
		}

		public List<TbaTicker> tickers(Date asOf, List<Double> coupons) {
			List<TbaTicker> result = empty();
			for (Double coupon : coupons)
				for(Integer settle : sequence(0, 4))
					result.add(new TbaTicker(program(), coupon, monthsAhead(settle, asOf)));
			return result;
		}
		
		public List<TbaTicker> tickers(Date asOf) {
		    return tickers(asOf, coupons());
		}

		private String program() {
			return value(C_PROGRAM);
		}
		private double low() {
			return value(C_LOW_COUPON);
		}
		private double high() {
			return value(C_HIGH_COUPON);
		}

		@Override public List<BloombergField> fields() {
			return list(TBA_PRICE, SETTLE_DATE);
		}

		@Override public BloombergJob job(BloombergField field) {
			return new BloombergJob("bloomberg_tba_autogen_" + field.bloomberg());
		}

		@Override public List<BloombergJobEntry> jobEntries(Date asOf, BloombergField field) {
			List<BloombergJobEntry> result = empty();
			for (TbaTicker ticker : tickers(asOf)) 
				result.add(ticker.jobEntry(field));
			return result;
		}

		public List<Double> coupons() {
			List<Double> result = empty();
			for(double coupon = low(); coupon <= high(); coupon += 0.5)
				result.add(coupon);
			return result;
		}

		public TimeSeries nthSeries(double coupon, int settleNum, BloombergField field) {
			return series(join("_", program(), nDecimals(1, coupon), settleNum + "n", field.tsdb()));
		}

		public TbaTicker ticker(double coupon, Date settle) {
			return new TbaTicker(program(), coupon, settle);
		}

		public Date settle(String yearMonth) {
			return TBA_SETTLE.settle(id(), yearMonth);
		}

		private int id() {
			return value(C_ID);
		}

		public Clause matches(IntColumn idColumn) {
			return idColumn.is(id());
		}

		public Cell<?> cell(IntColumn idColumn) {
			return idColumn.with(id());
		}

		public Date frontSettle(Date date) {
			return TBA_SETTLE.frontSettle(id(), date);
		}

		public Date frontNotificationDate(Date date) {
			return TBA_SETTLE.frontNotificationDate(id(), date);
		}
		
	}
	
	public List<Tba> tbas() {
		List<Tba> result = empty();
		for(Row row : rows()) result.add(new Tba(row));
		return result;
	}


	public Tba insert(String program, double low, double high) {
		bombUnless(low <= high, "low must be <= high");
		insert(
			C_PROGRAM.with(program),
			C_LOW_COUPON.with(low),
			C_HIGH_COUPON.with(high)
		);
		return tba(program);
	}


	public Tba tba(String program) {
		return new Tba(row(C_PROGRAM.is(program)));
	}


	public Date frontSettle(String program, Date date) {
		return tba(program).frontSettle(date);
	}


	public Date frontNotificationDate(String program, Date date) {
		return tba(program).frontNotificationDate(date);
	}


}
