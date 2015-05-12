package mortgage;

import static futures.BloombergField.*;
import static mortgage.TbaSettleTable.*;
import static mortgage.TbaTable.*;
import static tsdb.DataSource.*;
import static util.Dates.*;
import static util.Objects.*;

import java.util.*;

import mortgage.TbaTable.*;
import tsdb.*;
import db.*;

public class TestTba extends DbTestCase {
	private static final Date TEST_DATE = date("2008/03/04");
	private static final TbaTicker FNCL_5_0 = new TbaTicker("testfncl", 5.0, TEST_DATE);

	public void testTba() throws Exception {
		Tba testFncl = TBA.insert("testfncl", 5.0, 7.5);
		List<TbaTicker> tickers = testFncl.tickers(TEST_DATE);
		assertSize(30, tickers);
		assertEquals(FNCL_5_0, first(tickers));
		assertEquals(new TbaTicker("testfncl", 5.0, date("2008/04/04")), second(tickers));
		assertEquals(new TbaTicker("testfncl", 7.5, date("2008/07/04")), last(tickers));
	}
	
	public void testTicker() throws Exception {
		assertEquals("testfncl_5.0_200803_price", FNCL_5_0.tsdb(TBA_PRICE));
		assertEquals("TESTFNCL 5.0 03/2008 BBT3 Mtge", FNCL_5_0.bloomberg(TBA_PRICE));
	}
	
	public void testFrontSettle() throws Exception {
		assertEquals(date("2008/04/14"), TBA.frontSettle("fncl", date("2008/04/09")));
		assertEquals(date("2008/04/10"), TBA.frontNotificationDate("fncl", date("2008/04/09")));
		assertEquals(date("2008/05/13"), TBA.frontSettle("fncl", date("2008/04/10")));
		assertEquals(date("2008/05/09"), TBA.frontNotificationDate("fncl", date("2008/04/10")));
		TBA.insert("noSettles", 5.0, 7.5);
		try {
			TBA.frontSettle("noSettles", now());
			fail();
		} catch (Exception success) {
			assertMatches("no settlement date found", success);
		}
	}
	
	public void testSettle() throws Exception {
		TBA_SETTLE.deleteAll();
		TimeSeries settle = new TbaTicker("fncl", 4.5, TEST_DATE).series(SETTLE_DATE);
		Date obsTime = hoursAhead(15, TEST_DATE);
		BLOOMBERG_BBT3.with(settle).write(obsTime, 20080314);
		TBA_SETTLE.populate(BLOOMBERG_BBT3, TEST_DATE, list(4.5));
		assertEquals(date("2008/03/14"), TBA.tba("fncl").settle("200803"));
		BLOOMBERG_BBT3.with(settle).write(obsTime, 20080315);
		TBA_SETTLE.populate(BLOOMBERG_BBT3, TEST_DATE, list(4.5));
		assertEquals(date("2008/03/14"), TBA.tba("fncl").settle("200803"));
		
	}
}
