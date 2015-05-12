package mortgage;

import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;

import java.util.*;

import db.*;

public class TestJpTbaCopier extends DbTestCase {
	public static final Date TEST_DATE = date("2008/03/04");
	
	public void testJpTbaCopier() throws Exception {
		
		JPMORGAN_TEST.with(series("fncl_6.0_1n_price")).write(TEST_DATE, 100.50);
		JPMORGAN_TEST.with(series("fncl_6.0_1n_settle_date")).write(TEST_DATE, 20080307);
		JPMORGAN_TEST.with(series("fglmc_6.5_2n_price")).write(TEST_DATE, 101.50);
		JPMORGAN_TEST.with(series("fglmc_6.5_2n_settle_date")).write(TEST_DATE, 20080518);
		
		JpTbaCopier copier = new JpTbaCopier(TEST_DATE, JPMORGAN_TEST);
		copier.copy();
		assertObservation(100.50, "fncl_6.0_200803_price");
		assertObservation(20080307, "fncl_6.0_200803_settle_date");
		assertObservation(101.50, "fglmc_6.5_200805_price");
		assertObservation(20080518, "fglmc_6.5_200805_settle_date");
	}
	
	public void testBlowsWithNoData() throws Exception {
		try {
			new JpTbaCopier(TEST_DATE, JPMORGAN_TEST).copy();
			fail();
		} catch(Exception success) {
			assertMatches("no source data", success);
		}
	}

	private void assertObservation(double d, String seriesName) {
		assertEquals(d, JPMORGAN_TEST.with(series(seriesName)).observationValue(TEST_DATE));
	}
}
