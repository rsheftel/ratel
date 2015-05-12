package com.fftw.bloomberg.rtf.messages;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.types.BBFractionIndicator;
import com.fftw.test.BaseTestNG;
import com.fftw.util.Filter;

/**
 * RtfOnlinePosition Tester.
 * 
 * @created February 22, 2008
 * @since 1.0
 */
public class RtfOnlinePositionTest extends BaseTestNG {
    private static String str1 = " 1USD/ZAR1    0          00          0 0               0 0               0 0               0 0               0 0               0   0   0EMF     10USD/ZAR1B2080           20080222            518                                     EM.EEA_ZAR_FX      511                                                FX      510                                               ZAR      239                                          EM_CEMEA                                                                                                                      NNMSPB  ";

    @Test(groups = { "unittest" })
    public void testValueOf() {

        RtfOnlinePosition position = RtfOnlinePosition.valueOf(new LocalDate(), 1, str1);

        assert position != null : "Problem parsing source string";
    }

    @Test(groups = { "unittest" })
    public void testConvertValue() {
        BigDecimal bd = RtfOnlinePosition.convertValue(BBFractionIndicator.One, 4, 2999);
        assert BigDecimal.valueOf(299.9).equals(bd) : "FractionIndicator not converted propery";

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Two, 4, 2999);
        assert BigDecimal.valueOf(29.99).equals(bd) : "FractionIndicator not converted propery";

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Three, 4, 2999);
        assert BigDecimal.valueOf(2.999).equals(bd) : "FractionIndicator not converted propery";

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Four, 4, 2999);
        assert BigDecimal.valueOf(.2999).equals(bd) : "FractionIndicator not converted propery";

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Three, 11, 2999);
        assert BigDecimal.valueOf(2.999).equals(bd) : "FractionIndicator not converted propery";

    }

    @Test(groups = { "unittest" })
    public void testFilter() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
                "Level4", new BigDecimal("1234567890"));
        Map<String, String> filterFields = new HashMap<String, String>();

        filterFields.put("Account", "UnitTest");
        Filter<RtfOnlinePosition> batchFilter = RtfOnlinePosition.createFilter(filterFields);

        assert batchFilter.accept(bp) == true : "Case insensitive field names - wrong!";

        filterFields.clear();
        filterFields.put("account", "unittest");
        batchFilter = RtfOnlinePosition.createFilter(filterFields);

        assert batchFilter.accept(bp) == false : "Case insensitive value - wrong!";

        filterFields.clear();
        filterFields.put("account", "UnitTest");
        batchFilter = RtfOnlinePosition.createFilter(filterFields);

        assert batchFilter.accept(bp) == true : "Filter did not select position based on account=QMF";

    }

    @Test(groups = { "unittest" })
    public void testAggretable() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
                "Level4", new BigDecimal("78"));
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
                "Level4", new BigDecimal("78"));

        assert bp.aggregatable(other) : "Positions are aggregatble";
        assert other.aggregatable(bp) : "Positions are aggregatble";

        RtfOnlinePosition otherFail = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
                "Level9", new BigDecimal("78"));
        assert !bp.aggregatable(otherFail) : "Positions are NOT aggregatble";
        assert !otherFail.aggregatable(bp) : "Positions are NOT aggregatble";
    }

    @Test(groups = { "unittest" })
    public void testAggrete() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
                "Level4", new BigDecimal("78"));
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
                "Level4", new BigDecimal("78"));

        bp.setCurrentPosition(new BigDecimal("12"));
        bp.setTotalBuyVolume(new BigDecimal("6888.0"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        RtfOnlinePosition result = bp.aggregate(other);

        assertComparable(result.getOpenPosition(), new BigDecimal("78"), "OpenPostion");
        assertComparable(result.getCurrentPosition(), new BigDecimal("12"), "CurrentPosition");
        assertComparable(result.getRealizedPL(), new BigDecimal("67690"), "RealizedPL");
        assertComparable(result.getTotalBuyVolume(), new BigDecimal("6888"), "TotalBuyVolume");
        assertComparable(result.getTotalSellVolume(), new BigDecimal("-6888"), "TotalSellVolume");
        assertEquals(result.getTotalNumberOfBuys(), 4, "TotalNumberOfBuys");
        assertEquals(result.getTotalNumberOfSells(), 3, "TotalNumberOfSells");
        assertComparable(result.getCurrentAvgCost(), BigDecimal.ZERO,"CurrentAverCost");
        assertComparable(result.getOpenAverageCost(), BigDecimal.ZERO, "OpenAverageCost");

        result = other.aggregate(bp);
        assertComparable(result.getOpenPosition(), new BigDecimal("78"), "OpenPosition");
        assertComparable(result.getCurrentPosition(), new BigDecimal("12"), "CurrentPosition");
        assertComparable(result.getRealizedPL(), new BigDecimal("67690"), "RealizedPL");
        assertComparable(result.getTotalBuyVolume(), new BigDecimal("6888"), "TotalBuyVolume");
        assertComparable(result.getTotalSellVolume(), new BigDecimal("-6888"), "TotalSellVolume");
        assertEquals(result.getTotalNumberOfBuys(), 4, "TotalNumberOfBuys"); 
        assertEquals(result.getTotalNumberOfSells(), 3, "TotalNUmberOfSells");
        assertComparable(result.getCurrentAvgCost(), BigDecimal.ZERO, "CurrentAvgCost");
        assertComparable(result.getOpenAverageCost(), BigDecimal.ZERO, "OpenAverageCost");

        other.setCurrentPosition(new BigDecimal("12"));
        other.setRealizedPL(new BigDecimal("67960"));
        other.setTotalBuyVolume(new BigDecimal("6887"));
        other.setTotalSellVolume(new BigDecimal("-6878"));

        other.setTotalNumberOfBuys(7);
        other.setTotalNumberOfSells(9);

        result = other.aggregate(bp);
        assertComparable(result.getOpenPosition(), new BigDecimal("78"), "OpenPosition");
        assertComparable(result.getCurrentPosition(), new BigDecimal("24"), "CurrentPosition");
        assertComparable(result.getRealizedPL(), new BigDecimal("135650"), "RealizedPL");
        assertComparable(result.getTotalBuyVolume(), new BigDecimal("13775"), "TotalBuyVolume");
        assertComparable(result.getTotalSellVolume(), new BigDecimal("-13766"), "TotalSellVolume");
        assertEquals(result.getTotalNumberOfBuys(), 11, "TotalNumberOfBuys");
        assertEquals(result.getTotalNumberOfSells(), 12, "TotalNubmerOfSells");
        assertComparable(result.getCurrentAvgCost(), BigDecimal.ZERO, "CurrentAvgCost");
        assertComparable(result.getOpenAverageCost(), BigDecimal.ZERO, "OpenAverageCost");

        // screw up the results
        result.setTotalBuyVolume(new BigDecimal("34"));
        assert !(result.getOpenPosition().compareTo(new BigDecimal("157.807")) == 0
                && result.getCurrentPosition().compareTo(new BigDecimal("24.89")) == 0
                && result.getRealizedPL().compareTo(new BigDecimal("135650")) == 0
                && result.getTotalBuyVolume().compareTo(new BigDecimal("13775")) == 0
                && result.getTotalSellVolume().compareTo(new BigDecimal("-13766")) == 0
                && result.getTotalNumberOfBuys() == 11 && result.getTotalNumberOfSells() == 12
                && result.getCurrentAvgCost().compareTo(BigDecimal.ZERO) == 0 && result.getOpenAverageCost()
                .compareTo(BigDecimal.ZERO) == 0) : "Aggregate compare failed";

        RtfOnlinePosition otherFail = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
                "Level9", new BigDecimal("1234567890"));
        assert !bp.aggregatable(otherFail) : "Positions are NOT aggregatable";
        assert !otherFail.aggregatable(bp) : "Positions are NOT aggregatable";
    }
    
    @Test(groups = { "unittest" })
    public void testReadCommaFormat() throws IOException {
        String filename = "/OnlineMessages-2008-06-04.txt";

        InputStream onlinePositionStream = BatchPositionTest.class.getResourceAsStream(filename);
        BufferedReader onlinePositionReader = new BufferedReader(new InputStreamReader(onlinePositionStream));

        String line = null;
        while ((line = onlinePositionReader.readLine()) != null) {
            String[] pairs = line.split(",");
            RtfOnlinePosition op = RtfOnlinePosition.valueOf(pairs);
            
            assertNotNull(op, "Failed to convert line into position");
            assertNotNull(op.getSecurityId(), "Failed to parse securityId");
        }
    }
}
