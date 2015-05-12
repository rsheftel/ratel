package com.fftw.positions.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;

import org.testng.annotations.Test;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.util.SystematicFacade;

public class OnlineAccountStrategyAggregationStrategyTest {
    private String[] BATCHFILES = { "/OnlineMessages-2009-03-11.txt" };

    @Test(groups = { "unittest" })
    public void testAddingItem() {

        OnlinePositionAggregationCache<OnlinePositionKey> cache = new OnlinePositionAggregationCache<OnlinePositionKey>(new OnlineAccountStrategyAggregationStrategy());

        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "PB1", BigDecimal.ZERO);

        bp.setProductCode(BBProductCode.Equity);

        bp.setOpenPosition(new BigDecimal("0"));
        bp.setCurrentPosition(new BigDecimal("9"));
        bp.setTotalBuyVolume(new BigDecimal("10"));
        bp.setTotalSellVolume(new BigDecimal("1"));
        bp.setRealizedPL(new BigDecimal("0"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);
        cache.addOnlinePosition(bp);
        assertEquals(cache.count(), 1);

        RtfOnlinePosition other = new RtfOnlinePosition("AAPL", "UnitTest", "Level1");
        other.setProductCode(BBProductCode.Equity);

        // The PBs must be different to aggregate
        other.setPrimeBroker("PB2");
        other.setCurrentPosition(new BigDecimal("-3"));
        other.setRealizedPL(new BigDecimal("0"));
        other.setTotalBuyVolume(new BigDecimal("5"));
        other.setTotalSellVolume(new BigDecimal("8"));

        other.setTotalNumberOfBuys(7);
        other.setTotalNumberOfSells(9);

        RtfOnlinePosition firstAggregate = cache.addOnlinePosition(other);
        assertEquals(cache.count(), 1);

        assertEquals(firstAggregate.getTotalBuyVolume().longValue(), 15);
        assertEquals(firstAggregate.getTotalSellVolume().longValue(), 9);
        assertEquals(firstAggregate.getCurrentPosition().longValue(), 6);

        RtfOnlinePosition bp2 = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "PB1", BigDecimal.ZERO);
        bp2.setProductCode(BBProductCode.Equity);

        bp2.setCurrentPosition(new BigDecimal("8"));
        bp2.setTotalBuyVolume(new BigDecimal("12"));
        bp2.setTotalSellVolume(new BigDecimal("4"));
        bp2.setRealizedPL(new BigDecimal("0"));

        bp2.setTotalNumberOfBuys(4);
        bp2.setTotalNumberOfSells(3);

        RtfOnlinePosition secondAggregate = cache.addOnlinePosition(bp2);
        assertEquals(cache.count(), 1);

        assertEquals(secondAggregate.getTotalBuyVolume().longValue(), 17);
        assertEquals(secondAggregate.getTotalSellVolume().longValue(), 12);
        assertEquals(secondAggregate.getCurrentPosition().longValue(), 5);

        
        RtfOnlinePosition open = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "PB1", BigDecimal.TEN);
        open.setProductCode(BBProductCode.Equity);

        open.setCurrentPosition(new BigDecimal("10"));
        open.setTotalBuyVolume(new BigDecimal("5"));
        open.setTotalSellVolume(new BigDecimal("2"));
        open.setRealizedPL(new BigDecimal("0"));

        open.setTotalNumberOfBuys(3);
        open.setTotalNumberOfSells(2);

        RtfOnlinePosition thirdAggregate = cache.addOnlinePosition(open);
        assertEquals(cache.count(), 1);

        assertEquals(thirdAggregate.getTotalBuyVolume().longValue(), 22);
        assertEquals(thirdAggregate.getTotalSellVolume().longValue(), 14);
        assertEquals(thirdAggregate.getCurrentPosition().longValue(), 15);

        
        RtfOnlinePosition unique = new RtfOnlinePosition("IBM", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "PB1", BigDecimal.TEN);
        unique.setProductCode(BBProductCode.Equity);

        unique.setCurrentPosition(new BigDecimal("10"));
        unique.setTotalBuyVolume(new BigDecimal("5"));
        unique.setTotalSellVolume(new BigDecimal("2"));
        unique.setRealizedPL(new BigDecimal("0"));

        unique.setTotalNumberOfBuys(3);
        unique.setTotalNumberOfSells(2);
        
        RtfOnlinePosition uniqueAggregate = cache.addOnlinePosition(unique);
        assertEquals(cache.count(), 2);
        
        assertEquals(uniqueAggregate.getTotalBuyVolume().longValue(), 5);
        assertEquals(uniqueAggregate.getTotalSellVolume().longValue(), 2);
        assertEquals(uniqueAggregate.getCurrentPosition().longValue(), 10);
    }

    @Test(groups = { "unittest" })
    public void testFileReplay() {
        int lineCount = 0;
        String line = null;
        String globlalFilenameStr = null;
        OnlinePositionAggregationCache<OnlinePositionKey> cache = new OnlinePositionAggregationCache<OnlinePositionKey>(new OnlineAccountStrategyAggregationStrategy());

        try {
            for (String filename : BATCHFILES) {
                globlalFilenameStr = filename;
                URL location = BatchPositionTest.class.getResource(filename);
                System.out.println(location);
                InputStream onlinePositionStream = OnlineAccountStrategyAggregationStrategyTest.class
                    .getResourceAsStream(filename);
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
                    cache.addOnlinePosition(op);
                }
                onlinePositionReader.close();
            }

            System.out.println("What should we have?");
            //QF.FaderClose
            RtfOnlinePosition bph9 = cache.getOnlinePosition(new OnlinePositionKey("BPH9", BBProductCode.Currency, "QMF", "QF.NDayBreak", null, null, null, null, null));
            //RtfOnlinePosition bph9 = cache.getOnlinePosition(new OnlinePositionKey("BPH9", BBProductCode.Currency, "QMF", "QF.FaderClose", null, null, null, null, null));
            assertNotNull(bph9);
            assertEquals(bph9.getTotalBuyVolume().longValue(), 375000);
            assertEquals(bph9.getTotalSellVolume().longValue(), 0);

            for (RtfOnlinePosition op : cache.values()) {
                System.out.println(op);
            }
        } catch (IOException e) {
            assert false : "Unable to read test file: " + e.getMessage();
        } catch (NumberFormatException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + globlalFilenameStr);
            System.out.println(line);
            e.printStackTrace();
            assert false : "Unable to parse numeric field: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            System.out.println("Parsing Line #" + lineCount + " of file " + globlalFilenameStr);
            System.out.println(line);
            e.printStackTrace();
            assert false : "Unable to parse field: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
