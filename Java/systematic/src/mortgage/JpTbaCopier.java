package mortgage;

import static futures.BloombergField.*;
import static mortgage.TbaTable.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Range.*;
import static util.Sequence.*;

import java.util.*;

import mortgage.TbaTable.*;
import tsdb.*;

public class JpTbaCopier {

	private final Date date;
	private final DataSource source;

	public JpTbaCopier(Date testDate, DataSource source) {
		this.date = testDate;
		this.source = source;
	}

	public void copy() {
		int count = 0;
		for (Tba tba : TBA.tbas())
			for (double coupon : tba.coupons())
				for (int nth : sequence(1,4))
					count += copy(tba, coupon, nth);
		bombIf(count == 0, "no source data to copy");
	}

	private int copy(Tba tba, double coupon, int nth) {
		TimeSeries price = tba.nthSeries(coupon, nth, TBA_PRICE);
		TimeSeries settleDate = tba.nthSeries(coupon, nth, SETTLE_DATE);
		Observations sdObs = source.with(settleDate).observations(onDayOf(date));
		Observations priceObs = source.with(price).observations(onDayOf(date));
		if(sdObs.isEmpty()) return 0;
		Date settle = yyyyMmDd(sdObs.value());
		TbaTicker ticker = tba.ticker(coupon, settle);
		source.with(ticker.series(TBA_PRICE)).write(priceObs);
		source.with(ticker.series(SETTLE_DATE)).write(sdObs);
		return 1;
	}
}
