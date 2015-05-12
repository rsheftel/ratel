package com.fftw.bloomberg.rtf.messages;

import static org.testng.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.types.BBFractionIndicator;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.positions.cache.AbstractOnlinePositionAggregationStrategy;
import com.fftw.positions.cache.ExcelOnlineAggregationStrategy;
import com.fftw.util.AbstractBaseTest;
import com.fftw.util.Filter;
import com.fftw.util.SystematicFacade;

/**
 * RtfOnlinePosition Tester.
 * 
 */
public class RtfOnlinePositionTest extends AbstractBaseTest {
    private static String HOH9 = " 1HOH9        0          02      11754 1          -12138 0               0 0           42000 0               0 0              -1   1   0QMF      1HOH9        0           20090224            852                                     QF.FaderClose      789                                     MeanReversion       46                                           General        5                                           QUANTYS                                                                                                                      NNGSFUT ";
    private static String TYH9 = " 1TYH9        4    12285944    1228594 2          -68755 0               3 0               0 0          200000 0               5   0   1QMF      1TYH9        0           20090225             22                                     QF.CpnSwap_FN       21                                       TBA CpnSwap        8                                          Mortgage        5                                           QUANTYS                                                                                                                      NNGSFUT ";
    private static String TYM9 = " 1TYM9        3     1218754    1219688 0            -469 0              -3 0               0 0          800000 0               5   0   3QMF      1TYM9        0           20090406             41                                      QF.StrStg_TY       40                                        Rate Gamma       39                                        Volatility        5                                           QUANTYS                                                                                                                      NNGSFUT ";

    @Test(groups = { "unittest" })
    public void testValueOfTym9() {

        RtfOnlinePosition position = RtfOnlinePosition.valueOf(new LocalDate(), 1, TYM9);

        assertNotNull(position, "Problem parsing source string");

        position.setContractSize(new BigDecimal(100000));

        assertEquals(position.getCurrentPosition(), new BigDecimal(-3));
        assertEquals(position.getOpenPosition(), new BigDecimal(5));
        assertEquals(position.getTotalBuyVolume(), BigDecimal.ZERO);
        assertEquals(position.getTotalSellVolume(), new BigDecimal(800000));

        // check the normalization
        assertEquals(position.normTotalBuyVolume, BigDecimal.ZERO);
        assertEquals(position.normTotalSellVolume, new BigDecimal(8));
    }

    @Test(groups = { "unittest" })
    public void testValueOfTyh9() {

        RtfOnlinePosition position = RtfOnlinePosition.valueOf(new LocalDate(), 1, TYH9);

        assertNotNull(position, "Problem parsing source string");

        position.setContractSize(new BigDecimal(100000));

        assertEquals(position.getCurrentPosition(), new BigDecimal(3));
        assertEquals(position.getOpenPosition(), new BigDecimal(5));
        assertEquals(position.getTotalBuyVolume(), BigDecimal.ZERO);
        assertEquals(position.getTotalSellVolume(), new BigDecimal(200000));

        // check the normalization
        assertEquals(position.normTotalBuyVolume, BigDecimal.ZERO);
        assertEquals(position.normTotalSellVolume, new BigDecimal(2));
    }

    @Test(groups = { "unittest" })
    public void testValueOfHoh9() {

        RtfOnlinePosition position = RtfOnlinePosition.valueOf(new LocalDate(), 1, HOH9);

        assertNotNull(position, "Problem parsing source string");

        position.setContractSize(new BigDecimal(42000));

        assertEquals(position.getCurrentPosition(), BigDecimal.ZERO);
        assertEquals(position.getOpenPosition(), new BigDecimal(-1));
        assertEquals(position.getTotalBuyVolume(), new BigDecimal(42000));
        assertEquals(position.getTotalSellVolume(), BigDecimal.ZERO);

        // check the normalization
        assertEquals(position.normTotalBuyVolume, new BigDecimal(1));
        assertEquals(position.normTotalSellVolume, BigDecimal.ZERO);
    }

    @Test(groups = { "unittest" })
    public void testConvertValue() {
        BigDecimal bd = RtfOnlinePosition.convertValue(BBFractionIndicator.One, 4, 2999);
        assertEquals(BigDecimal.valueOf(299.9), bd, "FractionIndicator not converted propery");

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Two, 4, 2999);
        assertEquals(BigDecimal.valueOf(29.99), bd, "FractionIndicator not converted propery");

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Three, 4, 2999);
        assertEquals(BigDecimal.valueOf(2.999), bd, "FractionIndicator not converted propery");

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Four, 4, 2999);
        assertEquals(BigDecimal.valueOf(.2999), bd, "FractionIndicator not converted propery");

        bd = RtfOnlinePosition.convertValue(BBFractionIndicator.Three, 11, 2999);
        assertEquals(BigDecimal.valueOf(2.999), bd, "FractionIndicator not converted propery");
    }

    @Test(groups = { "unittest" })
    public void testFilter() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "BP", BigDecimal.ZERO);
        Map<String, String> filterFields = new HashMap<String, String>();

        filterFields.put("Account", "UnitTest");
        Filter<RtfOnlinePosition> batchFilter = RtfOnlinePosition.createFilter(filterFields);

        assertTrue(batchFilter.accept(bp), "Case insensitive field names - wrong!");

        filterFields.clear();
        filterFields.put("account", "unittest");
        batchFilter = RtfOnlinePosition.createFilter(filterFields);

        assertFalse(batchFilter.accept(bp), "Case insensitive value - wrong!");

        filterFields.clear();
        filterFields.put("account", "UnitTest");
        batchFilter = RtfOnlinePosition.createFilter(filterFields);

        assertTrue(batchFilter.accept(bp), "Filter did not select position based on account=QMF");

    }

    @Test(groups = { "unittest" })
    public void testAggretable() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "BP", BigDecimal.ZERO);
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "BP", BigDecimal.ZERO);

        AbstractOnlinePositionAggregationStrategy as = new ExcelOnlineAggregationStrategy();

        assertTrue(as.canAggregate(bp, other), "Positions are aggregatble");
        assertTrue(as.canAggregate(other, bp), "Positions are aggregatble");

        RtfOnlinePosition otherFail = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level9", "BP", BigDecimal.ZERO);
        assertFalse(as.canAggregate(bp, otherFail), "Positions are NOT aggregatble");
        assertFalse(as.canAggregate(otherFail, bp), "Positions are NOT aggregatble");
    }

    @Test(groups = { "unittest" })
    public void testAggrete() {
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "BP", BigDecimal.ZERO);
        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "BP", BigDecimal.ZERO);

        bp.setProductCode(BBProductCode.Equity);
        other.setProductCode(BBProductCode.Equity);

        bp.setOpenPosition(new BigDecimal("78.907"));
        bp.setCurrentPosition(new BigDecimal("12.5"));
        bp.setTotalBuyVolume(new BigDecimal("6888.0"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        AbstractOnlinePositionAggregationStrategy as = new ExcelOnlineAggregationStrategy();

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
            "Level9", "BP", BigDecimal.ZERO);
        assertFalse(as.canAggregate(bp, otherFail), "Positions are NOT aggregatble");
        assertFalse(as.canAggregate(otherFail, bp), "Positions are NOT aggregatble");
    }

    @Test(groups = { "unittest" })
    public void testValueOfCommaString() throws Exception {
        String testStr = "messageDate=2009-03-12, messageSequenceNumber=0, securityIdFlag=Cusip, securityId=JYM9,"
            + "exchangeTicker=null, currentAvgCost=10303, openAvgCost=10303, realizedPL=-2925,"
            + "currentPosition=-125000000, totalBuyVolume=37500000, totalSellVolume=0, openPosition=-162500000,"
            + "totalNumberOfBuys=1, totalNumberOfSells=0, account=QMF, productCode=Currency, bloombergId=JYM9,"
            + "strikePrice=null, tradeDate=2009-03-12, programType=null, daysToSettle=null, level1TagId=51,"
            + "level1TagName=QF.NDayBreak, level2TagId=50, level2TagName=Breakout, level3TagId=46,"
            + "level3TagName=General, level4TagId=5, level4TagName=QUANTYS, level5TagId=null, level5TagName=,"
            + "level6TagId=null, level6TagName=, short=N, cfd=N, primeBroker=GSFUT";

        RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(testStr);

        assertNotNull(op);
    }

    @Test(groups = { "unittest" })
    public void testToReconString() {
        String expected = "ACCOUNT=UnitTest|BID=TYM9|SECURITYIDFLAG=BBID|SHARESBOUGHT=6888|SHARESSOLD=-6888|OPENSHARES=78|RIC=|CURRENTPOSITION=12|POSITION=13776|YELLOWKEY=COMDTY|AVERAGEPRICE=0|SOURCE=AIM";
        RtfOnlinePosition bp = new RtfOnlinePosition("TYM9", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "BP", BigDecimal.ZERO);
        bp.setSecurityIdFlag("BBID");
        bp.setBloombergId("TYM9");

        bp.setProductCode(BBProductCode.Commodity);
        bp.setOpenPosition(new BigDecimal("78"));
        bp.setCurrentPosition(new BigDecimal("12"));
        bp.setTotalBuyVolume(new BigDecimal("688800000"));
        bp.setTotalSellVolume(new BigDecimal("-688800000"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        bp.setContractSize(new BigDecimal(100000));

        String reconString = bp.toReconString();

        System.out.println();
        System.out.println(reconString);
        assertEquals(reconString, expected);
    }

    public static List<RtfOnlinePosition> loadFromFile(String filename) {
        int lineCount = 0;
        String line = null;
        String globlalFilenameStr = filename;

        List<RtfOnlinePosition> filePositions = new LinkedList<RtfOnlinePosition>();
        try {
            URL location = BatchPositionTest.class.getResource(filename);
            System.out.println(location);
            InputStream onlinePositionStream = RtfOnlinePositionTest.class.getResourceAsStream(filename);
            BufferedReader onlinePositionReader = new BufferedReader(new InputStreamReader(
                onlinePositionStream));

            while ((line = onlinePositionReader.readLine()) != null) {
                lineCount++;
                RtfOnlinePosition op = RtfOnlinePosition.valueOfCommaString(line);
                if (op.isCommodity()) {
                    op.setContractSize(BigDecimal.valueOf(SystematicFacade.lookupContractSize(op
                        .getSecurityId(), op.getProductCode())));
                }
                if (op.isEquity() || op.isCorporate()) {
                    op.setExchangeTicker(SystematicFacade.lookupExchangeTicker(op.getSecurityId()));
                }
                op.setContractSize(new BigDecimal(SystematicFacade.lookupContractSize(op.getSecurityId(), op.getProductCode())));
                
                filePositions.add(op);
            }
            onlinePositionReader.close();

        } catch (IOException e) {
            fail("Unable to read test file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + globlalFilenameStr);
            System.out.println(line);
            e.printStackTrace();
            fail("Unable to parse numeric field: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + globlalFilenameStr);
            System.out.println(line);
            e.printStackTrace();
            fail("Unable to parse field: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unknown exception: " + e.getMessage());
        }
        return filePositions;
    }
}
