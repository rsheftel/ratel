package com.fftw.positions.cache;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePositionTest;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;

public class ExcelPositionCacheTest extends AbstractCacheTest {

    private static final int BATCH_FILE_RECORD_COUNT = 1038;
    @Test(groups = { "unittest" })
    public void testExcelPositionCacheBatchFileLoad() {
        // This file has short and long positions for XLB - the file is has 4 bad records (no SecurityId)
        List<BatchPosition> filePositions = BatchPositionTest.loadFromFile("/ActiveMQAf200-2008-12-09.txt");

        BatchPositionCache bpc = new BatchPositionCache(filePositions);
        assertEquals(bpc.size(), filePositions.size());

        ExcelPositionCache epc = new ExcelPositionCache(filePositions);

//        Collection<PositionKey> aggregatedKeys = epc.getBatchKeysWithAggregates();
//        for (PositionKey key : aggregatedKeys) {
//            if (!"QMF".equals(key.getAccount())) continue;
//            System.out.println(key);
//        }
        
        assertEquals(epc.batchCount(), BATCH_FILE_RECORD_COUNT);

        // Iterate over the batch records
        Collection<BatchPosition> batchValues = epc.batchValues();
        assertEquals(batchValues.size(), BATCH_FILE_RECORD_COUNT);

        // Do some retrievals
        BatchPosition xlb = epc.getBatchPosition(new PositionKey(XLB_CUSIP, BBProductCode.Equity, "QMF", "QF.SporCor",
            "MeanReversion", "General", "QUANTYS", "", BigDecimal.ZERO));
        assertNotNull(xlb);
        assertEquals(xlb.getSecurityId(), XLB_CUSIP);
        assertEquals(xlb.getAccount(), "QMF");
        assertEquals(xlb.getLevel1TagName(), "QF.SporCor");
        assertEquals(xlb.getLevel2TagName(), "MeanReversion");
        assertEquals(xlb.getLevel3TagName(), "General");
        assertEquals(xlb.getLevel4TagName(), "QUANTYS");
        assertEquals(xlb.getPrimeBroker(), "MSPB");
        assertEqualsBD(xlb.getFullCurrentNetPosition(), new BigDecimal(-37123));

        BatchPosition xlu = epc.getBatchPosition(new PositionKey(XLU_CUSIP, BBProductCode.Equity, "QMF", "QF.SporCor",
            "MeanReversion", "General", "QUANTYS", "", BigDecimal.ZERO));
        assertNotNull(xlu);
        assertEquals(xlu.getSecurityId(), XLU_CUSIP);
        assertEquals(xlu.getAccount(), "QMF");
        assertEquals(xlu.getLevel1TagName(), "QF.SporCor");
        assertEquals(xlu.getLevel2TagName(), "MeanReversion");
        assertEquals(xlu.getLevel3TagName(), "General");
        assertEquals(xlu.getLevel4TagName(), "QUANTYS");
        assertEquals(xlu.getPrimeBroker(), "MSPB");
        assertEqualsBD(xlu.getFullCurrentNetPosition(), new BigDecimal(565));

        // A failure
        BatchPosition missing = epc.getBatchPosition(new PositionKey("DOESNOTEXIST", BBProductCode.Equity, "QMF", "QF.SporCor",
            "MeanReversion", "General", "QUANTYS", "", BigDecimal.ZERO));
        assertNull(missing);

        // Add a new entry
        BatchPosition apple = xlu.copy();
        apple.setSecurityId("AAPL");
        apple.setSecurityIdFlag(BBSecurityIDFlag.Equity);
        apple.setTicker("AAPL");
        apple.setName("Apple Inc.");
        BatchPosition addedItem = epc.addBatchPosition(apple);
        assertNotNull(addedItem);
        // get it back
        BatchPosition appleLookup = epc.getBatchPosition(new PositionKey("AAPL", BBProductCode.Equity, "QMF", "QF.SporCor",
            "MeanReversion", "General", "QUANTYS", "", BigDecimal.ZERO));
        assertNotNull(appleLookup);
    }

    @Test(groups = { "unittest" })
    public void testExcelPositionCacheOnlineFileLoad() {
        List<RtfOnlinePosition> filePositions = RtfOnlinePositionTest
            .loadFromFile("/OnlineMessages-2009-03-11.txt");

        OnlinePositionCache opc = new OnlinePositionCache(filePositions);
        // filePositions and opc will not be the same, online records are updates, not unique
        assertEquals(filePositions.size(), 56);
        assertEquals(opc.size(), 26);

        // We can not construct using the list, as we have a type erasure problem
        ExcelPositionCache epc = new ExcelPositionCache();
        epc.addOnlinePositionAll(filePositions);

        // Iterate over the batch records
        Collection<RtfOnlinePosition> onlineValues = epc.onlineValues();
        assertEquals(onlineValues.size(), 26);

        // BPH9
        // Do some retrievals
        RtfOnlinePosition bpm9 = epc.getOnlinePosition(new OnlinePositionKey("BPM9", BBProductCode.Currency, "QMF", "QF.NDayBreak",
            "Breakout", "General", "QUANTYS", "GSFUT", BigDecimal.ZERO));
        assertNotNull(bpm9);
        assertEquals(bpm9.getSecurityId(), "BPM9");
        assertEquals(bpm9.getAccount(), "QMF");
        assertEquals(bpm9.getLevel1TagName(), "QF.NDayBreak");
        assertEquals(bpm9.getLevel2TagName(), "Breakout");
        assertEquals(bpm9.getLevel3TagName(), "General");
        assertEquals(bpm9.getLevel4TagName(), "QUANTYS");
        assertEquals(bpm9.getPrimeBroker(), "GSFUT");
        assertEqualsBD(bpm9.getCurrentPosition(), new BigDecimal(-375000));
        assertEqualsBD(bpm9.getOpenPosition(), new BigDecimal(0));
        assertEqualsBD(bpm9.getTotalBuyVolume(), new BigDecimal(0));
        assertEqualsBD(bpm9.getTotalSellVolume(), new BigDecimal(375000));
        
        // Copy, change and add
        RtfOnlinePosition bph9 = bpm9.copy();
        bph9.setSecurityId("BPH9");
        bph9.setBloombergId("BPH9");
        RtfOnlinePosition bph9Added = epc.addOnlinePosition(bph9);
        assertNotNull(bph9Added);
        
    }

    @Test(groups = { "unittest" })
    public void testExcelPositionCacheFileLoad() {
        List<BatchPosition> batchFilePositions = BatchPositionTest
            .loadFromFile("/ActiveMQAf200-2009-01-13.txt");

        BatchPositionCache bpc = new BatchPositionCache(batchFilePositions);
        assertEquals(bpc.size(), batchFilePositions.size());

        List<RtfOnlinePosition> onlineFilePositions = RtfOnlinePositionTest
            .loadFromFile("/OnlineMessages-2009-01-14.txt");
        OnlinePositionCache opc = new OnlinePositionCache(onlineFilePositions);
        // filePositions and opc will not be the same, online records are updates, not unique entries
        assertEquals(onlineFilePositions.size(), ONLINE_2009_01_14_FILE_COUNT);
        assertEquals(opc.size(), ONLINE_2009_01_14_CACHE_COUNT);

        ExcelPositionCache epc = new ExcelPositionCache(batchFilePositions, onlineFilePositions);

        // We have two entries for TYH9C & TYH9P
        assertEquals(epc.batchCount(), batchFilePositions.size() - (2));

        // Check the OnlineCache
        Collection<RtfOnlinePosition> onlinePositions = opc.values();
        Collection<RtfOnlinePosition> excelOnlinePositions = epc.onlineValues();

        // We have two XLP positions - a short position that is closed, and then a new long position
        assertEquals(excelOnlinePositions.size(), onlinePositions.size() - 1);
        assertEquals(epc.onlineCount(), opc.size() - 1);

        // Get the batch and online record for XLP
        BatchPosition xlpBatch = epc.getBatchPosition(new PositionKey(XLP_CUSIP, BBProductCode.Equity, "QMF", "QF.SporCor",
            "MeanReversion", "General", "QUANTYS", "", BigDecimal.ZERO));
        assertNotNull(xlpBatch);

        RtfOnlinePosition xlpOnline = epc.getOnlinePosition(new OnlinePositionKey(XLP_CUSIP, BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "", BigDecimal.ZERO));
        assertNotNull(xlpOnline);

        assertEqualsBD(xlpBatch.getOnlineOpenPosition(), xlpOnline.getOpenPosition());
        assertEqualsBD(xlpOnline.getCurrentPosition(), new BigDecimal(65576));
        assertEquals(xlpOnline.getShort(), "N");
    }
}
