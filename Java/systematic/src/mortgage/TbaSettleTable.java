package mortgage;

import static db.clause.Clause.*;
import static futures.BloombergField.*;
import static util.Dates.*;
import static util.Errors.*;

import java.util.*;

import mortgage.TbaTable.*;
import tsdb.*;
import db.clause.*;
import db.columns.*;
import db.tables.TSDB.*;

public class TbaSettleTable extends TbaSettlementBase {
	
    private static final long serialVersionUID = 1L;
    public static final TbaSettleTable TBA_SETTLE  = new TbaSettleTable();

	public TbaSettleTable() {
		super("tba_settle");
	}

	public void deleteAll() {
	    deleteAll(TRUE);
	}
	
	public void populate(DataSource source, Date asOf) {
	    populate(source, asOf, null);
	}

	public void populate(DataSource source, Date asOf, List<Double> coupons) {
		for (Tba tba : TbaTable.TBA.tbas()) {
		    List<TbaTicker> tickers = coupons == null 
		        ? tba.tickers(asOf) 
	            : tba.tickers(asOf, coupons);
            for (TbaTicker ticker : tickers) {
				if(has(tba, ticker)) continue;
				Observations obs = source.with(ticker.series(SETTLE_DATE)).observations(asOf);
				if(obs.hasContent())
					set(tba, ticker, obs.value());
			}
		}
	}

	private void set(Tba tba, TbaTicker ticker, double value) {
		Date settle = yyyyMmDd(value);
		Date notification = businessDaysAgo(2, settle, "nyb");
		insert(
			tba.cell(C_TBA_ID), 
			ticker.cell(C_YEARMONTH), 
			C_SETTLEMENT_DATE.with(settle), 
			C_NOTIFICATION_DATE.with(notification)
		);
	}

	private boolean has(Tba tba, TbaTicker ticker) {
		return rowExists(tba.matches(C_TBA_ID).and(ticker.matches(C_YEARMONTH)));
	}

	public Date settle(int tbaId, String yearMonth) {
		return C_SETTLEMENT_DATE.value(C_TBA_ID.is(tbaId).and(C_YEARMONTH.is(yearMonth)));
	}

	public Date frontSettle(int id, Date date) {
		return frontContractValue(id, date, C_SETTLEMENT_DATE);
	}

	private Date frontContractValue(int id, Date date, DatetimeColumn column) {
		Clause notificationMatches = C_NOTIFICATION_DATE.greaterThan(date);
		Clause tbaMatches = C_TBA_ID.is(id);
		Date settle = column.min().value(tbaMatches.and(notificationMatches));
		return bombNull(settle, "no settlement date found for date " + date);
	}

	public Date frontNotificationDate(int id, Date date) {
		return frontContractValue(id, date, C_NOTIFICATION_DATE);
	}

}
