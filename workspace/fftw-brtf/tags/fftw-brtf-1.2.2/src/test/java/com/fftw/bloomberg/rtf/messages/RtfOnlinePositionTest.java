package com.fftw.bloomberg.rtf.messages;

import com.fftw.bloomberg.types.BBFractionIndicator;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.util.Filter;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * RtfOnlinePosition Tester.
 * 
 */
public class RtfOnlinePositionTest {
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
            "Level4");
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
            "Level4");
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4");

        RtfOnlinePositionAggregationStrategy as = new RtfOnlinePositionAggregationStrategy();

        assertTrue(as.canAggregate(bp, other), "Positions are aggregatble");
        assertTrue(as.canAggregate(other, bp), "Positions are aggregatble");

        RtfOnlinePosition otherFail = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level9");
        assertFalse(as.canAggregate(bp, otherFail), "Positions are NOT aggregatble");
        assertFalse(as.canAggregate(otherFail, bp), "Positions are NOT aggregatble");
    }

    @Test(groups = { "unittest" })
    public void testAggrete() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4");
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4");

        bp.setOpenPosition(new BigDecimal("78.907"));
        bp.setCurrentPosition(new BigDecimal("12.5"));
        bp.setTotalBuyVolume(new BigDecimal("6888.0"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        RtfOnlinePositionAggregationStrategy as = new RtfOnlinePositionAggregationStrategy();

        RtfOnlinePosition result = as.aggregate(bp, other);

        assertEquals(result.getOpenPosition().compareTo(new BigDecimal("78.907")), 0);
        assertEquals(result.getCurrentPosition().compareTo(new BigDecimal("12.5")), 0);
        assertEquals(result.getRealizedPL().compareTo(new BigDecimal("67690")), 0);
        assertEquals(result.getTotalBuyVolume().compareTo(new BigDecimal("6888")), 0);
        assertEquals(result.getTotalSellVolume().compareTo(new BigDecimal("-6888")), 0);
        assertEquals(result.getTotalNumberOfBuys(), 4);
        assertEquals(result.getTotalNumberOfSells(), 3);
        assertEquals(result.getCurrentAvgCost().compareTo(BigDecimal.ZERO), 0);
        assertEquals(result.getOpenAverageCost().compareTo(BigDecimal.ZERO), 0);

        result = as.aggregate(other, bp);
        assertEquals(result.getOpenPosition().compareTo(new BigDecimal("78.907")), 0);
        assertEquals(result.getCurrentPosition().compareTo(new BigDecimal("12.5")), 0);
        assertEquals(result.getRealizedPL().compareTo(new BigDecimal("67690")), 0);
        assertEquals(result.getTotalBuyVolume().compareTo(new BigDecimal("6888")), 0);
        assertEquals(result.getTotalSellVolume().compareTo(new BigDecimal("-6888")), 0);
        assertEquals(result.getTotalNumberOfBuys(), 4);
        assertEquals(result.getTotalNumberOfSells(), 3);
        assertEquals(result.getCurrentAvgCost().compareTo(BigDecimal.ZERO), 0);
        assertEquals(result.getOpenAverageCost().compareTo(BigDecimal.ZERO), 0);

        other.setOpenPosition(new BigDecimal("78.900"));
        other.setCurrentPosition(new BigDecimal("12.39"));
        other.setRealizedPL(new BigDecimal("67960"));
        other.setTotalBuyVolume(new BigDecimal("6887"));
        other.setTotalSellVolume(new BigDecimal("-6878"));

        other.setTotalNumberOfBuys(7);
        other.setTotalNumberOfSells(9);

        result = as.aggregate(other, bp);
        assertEquals(result.getOpenPosition().compareTo(new BigDecimal("157.807")), 0);
        assertEquals(result.getCurrentPosition().compareTo(new BigDecimal("24.89")), 0);
        assertEquals(result.getRealizedPL().compareTo(new BigDecimal("135650")), 0);
        assertEquals(result.getTotalBuyVolume().compareTo(new BigDecimal("13775")), 0);
        assertEquals(result.getTotalSellVolume().compareTo(new BigDecimal("-13766")), 0);
        assertEquals(result.getTotalNumberOfBuys(), 11);
        assertEquals(result.getTotalNumberOfSells(), 12);
        assertEquals(result.getCurrentAvgCost().compareTo(BigDecimal.ZERO), 0);
        assertEquals(result.getOpenAverageCost().compareTo(BigDecimal.ZERO), 0);

        // screw up the results
        result.setTotalBuyVolume(new BigDecimal("34"));
        assertEquals(result.getOpenPosition().compareTo(new BigDecimal("157.807")), 0);
        assertEquals(result.getCurrentPosition().compareTo(new BigDecimal("24.89")), 0);
        assertEquals(result.getRealizedPL().compareTo(new BigDecimal("135650")), 0);
        assertEquals(result.getTotalBuyVolume().compareTo(new BigDecimal("13775")), -1);
        assertEquals(result.getTotalSellVolume().compareTo(new BigDecimal("-13766")), 0);
        assertEquals(result.getTotalNumberOfBuys(), 11);
        assertEquals(result.getTotalNumberOfSells(), 12);
        assertEquals(result.getCurrentAvgCost().compareTo(BigDecimal.ZERO), 0);
        assertEquals(result.getOpenAverageCost().compareTo(BigDecimal.ZERO), 0);

        RtfOnlinePosition otherFail = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level9");
        assertFalse(as.canAggregate(bp, otherFail), "Positions are NOT aggregatble");
        assertFalse(as.canAggregate(otherFail, bp), "Positions are NOT aggregatble");
    }

    @Test(groups = { "unittest" })
    public void testValueOfCommaString() throws Exception {
      String testStr = "messageDate=2009-03-12, messageSequenceNumber=0, securityIdFlag=Cusip, securityId=JYM9," + 
      "exchangeTicker=null, currentAvgCost=10303, openAvgCost=10303, realizedPL=-2925," + 
      "currentPosition=-125000000, totalBuyVolume=37500000, totalSellVolume=0, openPosition=-162500000," + 
      "totalNumberOfBuys=1, totalNumberOfSells=0, account=QMF, productCode=Currency, bloombergId=JYM9,"+ 
      "strikePrice=null, tradeDate=2009-03-12, programType=null, daysToSettle=null, level1TagId=51,"+ 
      "level1TagName=QF.NDayBreak, level2TagId=50, level2TagName=Breakout, level3TagId=46,"+ 
      "level3TagName=General, level4TagId=5, level4TagName=QUANTYS, level5TagId=null, level5TagName=,"+ 
      "level6TagId=null, level6TagName=, short=N, cfd=N, primeBroker=GSFUT";

      RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(testStr);
      
      assertNotNull(op);
    }
    
    @Test(groups = { "unittest" })
    public void testToReconString() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4");

        bp.setBloombergId("TTTT");
        bp.setProductCode(BBProductCode.Commodity);
        bp.setOpenPosition(new BigDecimal("78.907"));
        bp.setCurrentPosition(new BigDecimal("12.5"));
        bp.setTotalBuyVolume(new BigDecimal("6888.0"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        String reconString = bp.toReconString();
        System.out.println(reconString);
    }
}
