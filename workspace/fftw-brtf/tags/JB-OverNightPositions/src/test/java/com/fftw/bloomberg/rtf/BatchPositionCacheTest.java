package com.fftw.bloomberg.rtf;

import static org.testng.Assert.*;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.test.BaseTestNG;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

/**
 * BatchPositionCache Tester.
 * 
 * @created June 6, 2008
 * @since 1.0
 */
public class BatchPositionCacheTest extends BaseTestNG {

    @Test(groups = { "unittest" })
    public void testAddBatchPosition() {

        BatchPositionCache cache = new BatchPositionCache();
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");

        bp.setFullCurrentNetPosition(new BigDecimal("100.23"));
        bp.setFullCurrentNetPositionWithoutComma(new BigDecimal("100.23"));
        bp.setCurrentLongPosition(new BigDecimal("90"));
        bp.setCurrentShortPosition(new BigDecimal("89"));

        cache.addBatchPosition(bp);

        assertEquals(cache.size(), 1, "Failed to add item to cache");
        BatchPosition other = new BatchPosition(new LocalDate(), "IBM", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");

        other.setFullCurrentNetPosition(new BigDecimal("100.23"));
        other.setFullCurrentNetPositionWithoutComma(new BigDecimal("100.23"));
        other.setCurrentLongPosition(new BigDecimal("90"));
        other.setCurrentShortPosition(new BigDecimal("89"));

        cache.addBatchPosition(other);

        assertEquals(cache.size(), 2, "Failed to add second item to cache");
        ;
    }

    @Test(groups = { "unittest" })
    public void testAggregateBatchPosition() {
        BatchPositionCache cache = new BatchPositionCache();
        BatchPosition bp = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");

        bp.setFullCurrentNetPosition(new BigDecimal("100"));
        bp.setFullCurrentNetPositionWithoutComma(new BigDecimal("100"));
        bp.setCurrentLongPosition(new BigDecimal("90"));
        bp.setCurrentShortPosition(new BigDecimal("89"));

        cache.aggregateBatchPosition(bp);

        BatchPosition cachedBp = cache.getCacheItem(new PositionKey(bp.getSecurityId(), bp.getAccount(), bp
                .getLevel1TagName(), bp.getLevel2TagName(), bp.getLevel3TagName(), bp.getLevel4TagName(), bp
                .getOnlineCurrentPosition()));

        assertComparable(cachedBp.getFullCurrentNetPosition(), new BigDecimal("100"),
                "FullCurrentNetPosition");
        assertComparable(cachedBp.getFullCurrentNetPositionWithoutComma(), new BigDecimal("100"),
                "FullCurrentNetPositionWithoutComma");
        assertComparable(cachedBp.getCurrentLongPosition(), new BigDecimal("90"), "CurrentLongPosition");
        assertComparable(cachedBp.getCurrentShortPosition(), new BigDecimal("89"), "CurrentShortPosition");

        BatchPosition other = new BatchPosition(new LocalDate(), "AAPL", "UnitTest", "Level1", "Level2",
                "Level3", "Level4");

        other.setFullCurrentNetPosition(new BigDecimal("100.23"));
        other.setFullCurrentNetPositionWithoutComma(new BigDecimal("100.23"));
        other.setCurrentLongPosition(new BigDecimal("90"));
        other.setCurrentShortPosition(new BigDecimal("89"));

        cache.aggregateBatchPosition(other);

        cachedBp = cache.getCacheItem(new PositionKey(bp.getSecurityId(), bp.getAccount(), bp
                .getLevel1TagName(), bp.getLevel2TagName(), bp.getLevel3TagName(), bp.getLevel4TagName(), bp
                .getOnlineCurrentPosition()));

        assertComparable(cachedBp.getFullCurrentNetPosition(), new BigDecimal("100"),
                "FullCurrentNetPosition");
        assertComparable(cachedBp.getFullCurrentNetPositionWithoutComma(), new BigDecimal("100"),
                "FullCurrentNetPositionWithoutComma");
        assertComparable(cachedBp.getCurrentLongPosition(), new BigDecimal("180"), "CurrentLongPosition");
        assertComparable(cachedBp.getCurrentShortPosition(), new BigDecimal("178"), "CurrentShortPosition");
    }

    @Test(groups = { "unittest" })
    public void testBatchPostionCaching() throws Exception {
        BatchPositionCache batchCache = loadTestBatchFile();

        assertTrue(batchCache.size() > 0, "Failed to read batch records");
        
        // check that we have the records we need
        BatchPosition bp1 = batchCache.getCacheItem(new PositionKey("TUU8", "QMF", "QF.NDayBreak",
                "Breakout", "General", "QUANTYS", new BigDecimal(96)));
        assertNotNull(bp1, "Failed to find TUU8 for NDayBreak position 96");
        
        BatchPosition bp2 = batchCache.getCacheItem(new PositionKey("TUU8", "QMF", "QF.NDayBreak",
                "Breakout", "General", "QUANTYS", new BigDecimal(-96)));
        assertNotNull(bp2, "Failed to find TUU8 for NDayBreak position -96");

        // check that TBA/Equity and others are correctly stored
        BatchPosition tba = batchCache.getCacheItem(new PositionKey("01F052664", "QMF", "QF.CpnSwap_FN",
                "TBA CpnSwap", "Mortgage", "QUANTYS", new BigDecimal(59000000)));
        assertNotNull(tba, "Failed to find TBA");

        BatchPosition equity = batchCache.getCacheItem(new PositionKey("548661107", "QMF", "QF.CDSvEQ_500",
                "Capital Structure", "Credit", "QUANTYS", new BigDecimal(-24273)));
        assertNotNull(equity, "Failed to find Equity");

        BatchPosition cds = batchCache.getCacheItem(new PositionKey("SPQ603FL6", "QMF", "QF.DTD_USD",
                "Capital Structure", "Credit", "QUANTYS", new BigDecimal(5000000)));
        assertNotNull(cds, "Failed to find CDS");
    }
    
    private BatchPositionCache loadTestBatchFile() throws IOException {
        BatchPositionCache batchCache = new BatchPositionCache();

        String filename = "/ActiveMQAf200-2008-06-03.txt";
        LocalDate fileDate = new LocalDate(2008, 6, 3);

        InputStream batchPositionStream = BatchPositionTest.class.getResourceAsStream(filename);
        BufferedReader batchPositionReader = new BufferedReader(new InputStreamReader(batchPositionStream));

        String line = null;
        while ((line = batchPositionReader.readLine()) != null) {
            BatchPosition bp = null;
            bp = BatchPosition.valueOf(fileDate, line);
            batchCache.addBatchPosition(bp);
            System.out.println(batchCache.getKeyForItem(bp));
        }
        return batchCache;
    }
    
}
