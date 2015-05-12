package com.fftw.positions.cache;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePositionTest;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.positions.ISecurity;
import com.fftw.positions.Position;

/**
 * BatchPositionCache Tester.
 */
public class BatchPositionCacheTest extends AbstractCacheTest {

    @Test(groups = { "unittest" })
    public void testBatchPositionCacheBatchFileLoad() {
        // This file has short and long positions for XLB - the file is has 4 bad records (no SecurityId)
        List<BatchPosition> filePositions = BatchPositionTest.loadFromFile("/ActiveMQAf200-2008-12-09.txt");

        BatchPositionCache bpc = new BatchPositionCache(filePositions);
        assertEquals(bpc.size(), filePositions.size());

        // Iterate over the batch records
        Collection<BatchPosition> batchPositions = bpc.values();
        assertEquals(batchPositions.size(), bpc.size());
        
        for (BatchPosition batchPosition : batchPositions) {
            Position position = batchPosition.getPosition();
            assertNotNull(position);
        }

        // Do some retrievals
        BatchPosition xlb = bpc.getBatchPosition(new PositionKey(XLB_CUSIP, BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", new BigDecimal(96)));
        assertNotNull(xlb);
        assertEquals(xlb.getSecurityId(), XLB_CUSIP);
        assertEquals(xlb.getAccount(), "QMF");
        assertEquals(xlb.getLevel1TagName(), "QF.SporCor");
        assertEquals(xlb.getLevel2TagName(), "MeanReversion");
        assertEquals(xlb.getLevel3TagName(), "General");
        assertEquals(xlb.getLevel4TagName(), "QUANTYS");
        assertEquals(xlb.getPrimeBroker(), "MSPB");
        assertEqualsBD(xlb.getFullCurrentNetPosition(), 96);

        BatchPosition xlu = bpc.getBatchPosition(new PositionKey(XLU_CUSIP, BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", new BigDecimal(565)));
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
        BatchPosition missing = bpc.getBatchPosition(new PositionKey("DOESNOTEXIST", BBProductCode.Equity,
            "QMF", null, null, null, null, "MSPB", null));
        assertNull(missing);

        // Add a new entry
        BatchPosition apple = xlu.copy();
        apple.setSecurityId("AAPL");
        apple.setSecurityIdFlag(BBSecurityIDFlag.Equity);
        apple.setTicker("AAPL");
        apple.setName("Apple Inc.");
        BatchPosition addedItem = bpc.addBatchPosition(apple);
        // This a non-aggregating cache, and this is a new item - works like a normal map
        assertNull(addedItem);
        // get it back
        BatchPosition appleLookup = bpc.getBatchPosition(new PositionKey("AAPL", BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", new BigDecimal(565)));
        assertNotNull(appleLookup);
    }

    @Test(groups = { "unittest" })
    public void testBatchPositionCacheOnlineFileLoad() {
        List<RtfOnlinePosition> filePositions = RtfOnlinePositionTest
            .loadFromFile("/OnlineMessages-2009-03-11.txt");

        OnlinePositionCache opc = new OnlinePositionCache(filePositions);
        // filePositions and opc will not be the same, online records are updates, not unique
        assertEquals(filePositions.size(), 56);
        assertEquals(opc.size(), 26);

        // We can not construct using the list, as we have a type erasure problem
        BatchPositionCache rpc = new BatchPositionCache();
        rpc.addOnlinePositionAll(filePositions);

        // Iterate over the batch records
        Collection<BatchPosition> onlineValues = rpc.values();
        assertEquals(onlineValues.size(), 26);

        for (BatchPosition batchPosition : onlineValues) {
            if (batchPosition.getProductCode() == BBProductCode.Equity) {
                assertNotNull(batchPosition.getTicker());
            }
        }
        // BPH9
        // Do some retrievals
        BatchPosition bpm9 = rpc.getCacheItem(new PositionKey("BPM9", BBProductCode.Currency,
            "QMF", "QF.FaderClose", "MeanReversion", "General", "QUANTYS", "GSFUT", BigDecimal.ZERO));
        assertNotNull(bpm9);
        assertEquals(bpm9.getSecurityId(), "BPM9");
        assertEquals(bpm9.getAccount(), "QMF");
        assertEquals(bpm9.getLevel1TagName(), "QF.FaderClose");
        assertEquals(bpm9.getLevel2TagName(), "MeanReversion");
        assertEquals(bpm9.getLevel3TagName(), "General");
        assertEquals(bpm9.getLevel4TagName(), "QUANTYS");
        assertEquals(bpm9.getPrimeBroker(), "GSFUT");
        assertEqualsBD(bpm9.getFullCurrentNetPositionWithoutComma(), 0);
        assertEqualsBD(bpm9.getOnlineOpenPosition(), 0);
        
        bpm9.setContractSize(new BigDecimal(62500));
        Position position = bpm9.getPosition();
        assertNotNull(position);
        
        ISecurity security = position.getSecurity();
        assertNotNull(security);
        
        // we open and closed this position in one day
        assertEqualsBD(position.getOpenPosition(), 0);
        assertEqualsBD(position.getCurrentPosition(), 0);
        assertEquals(position.getSharesBought(), 7);
        assertEquals(position.getSharesSold(), 7);
        assertEquals(position.getIntradayPosition(), 0);
    }

    @Test(groups = { "unittest" })
    public void testPositionCacheFileLoad() {
        List<BatchPosition> batchFilePositions = BatchPositionTest
            .loadFromFile("/ActiveMQAf200-2009-01-13.txt");

        BatchPositionCache bpc = new BatchPositionCache(batchFilePositions);
        assertEquals(bpc.size(), batchFilePositions.size());

        List<RtfOnlinePosition> onlineFilePositions = RtfOnlinePositionTest
            .loadFromFile("/OnlineMessages-2009-01-14.txt");
        OnlinePositionCache opc = new OnlinePositionCache(onlineFilePositions);

        // filePositions and opc will not be the same, online records are updates, not unique entries
        assertEquals(onlineFilePositions.size(), ONLINE_2009_01_14_FILE_COUNT);
        assertEquals(opc.size(), 16);

        bpc.addOnlinePositionAll(onlineFilePositions);

        // We have 4 new positions for the day
        assertEquals(bpc.size(), batchFilePositions.size() + 4);

        // iterate the BatchRecords and get Positions from them
        Collection<BatchPosition> batchPositions = bpc.values();
        
        assertEquals(batchPositions.size(), bpc.size());
        for (BatchPosition batchPosition : batchPositions) {
            Position position = batchPosition.getPosition();
            assertNotNull(position);
            System.out.println(batchPosition);
        }
        // -37219
        
        // We have two XLP positions - a short position that is closed, and then a new long position
        // and four FVH9 positions
        // Get the batch and online record for XLP
        BatchPosition xlpBatch1 = bpc.getBatchPosition(new PositionKey(XLP_CUSIP, BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", new BigDecimal(-52799)));
        assertNotNull(xlpBatch1);
        Position xlpPosition1 = xlpBatch1.getPosition();
        assertNotNull(xlpPosition1);
        assertEqualsBD(xlpPosition1.getOpenPosition(), -52799);
        assertEqualsBD(xlpPosition1.getCurrentPosition(), 0);

        BatchPosition xlpBatch2 = bpc.getBatchPosition(new PositionKey(XLP_CUSIP, BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", new BigDecimal(0)));
        assertNotNull(xlpBatch2);

        Position xlpPosition2 = xlpBatch2.getPosition();
        assertNotNull(xlpPosition2);
        assertEquals(xlpPosition2.getOpenPosition(), new BigDecimal(0));
        assertEqualsBD(xlpPosition2.getCurrentPosition(), 65576);

        
        // FVH9 
        BatchPosition fvh9Batch1 = bpc.getBatchPosition(new PositionKey("FVH9", BBProductCode.Commodity, "QMF",
            "QF.CpnSwap_FN", "TBA CpnSwap", "Mortgage", "QUANTYS", "GSFUT", new BigDecimal(6)));
        assertNotNull(fvh9Batch1);
        Position fvh9Position1 = fvh9Batch1.getPosition();
        assertNotNull(fvh9Position1);
        assertEqualsBD(fvh9Position1.getOpenPosition(), 6);
        assertEqualsBD(fvh9Position1.getCurrentPosition(), 6);

        BatchPosition fvh9Batch2 = bpc.getBatchPosition(new PositionKey("FVH9", BBProductCode.Commodity, "QMF",
            "QF.NDayBreak", "Breakout", "General", "QUANTYS", "GSFUT", new BigDecimal(6)));
        assertNotNull(fvh9Batch2);

        Position fvh9Position2 = fvh9Batch2.getPosition();
        assertNotNull(fvh9Position2);
        assertEquals(fvh9Position2.getOpenPosition(), new BigDecimal(6));
        assertEqualsBD(fvh9Position2.getCurrentPosition(), 6);
        
        
        // Online
        BatchPosition fvh9Online1 = bpc.getBatchPosition(new PositionKey("FVH9", BBProductCode.Commodity, "QMF",
            "QF.CpnSwap_FN", "TBA CpnSwap", "Mortgage", "QUANTYS", "GSFUT", new BigDecimal(-21)));
        assertNotNull(fvh9Online1);
        Position fvh9Position11 = fvh9Online1.getPosition();
        assertNotNull(fvh9Position11);
        assertEqualsBD(fvh9Position11.getOpenPosition(), -21);
        assertEqualsBD(fvh9Position11.getCurrentPosition(), -8);

        BatchPosition fvh9Online2 = bpc.getBatchPosition(new PositionKey("FVH9", BBProductCode.Commodity, "QMF",
            "QF.NDayBreak", "Breakout", "General", "QUANTYS", "GSFUT", new BigDecimal(27)));
        assertNotNull(fvh9Online2);

        Position fvh9Position22 = fvh9Online2.getPosition();
        assertNotNull(fvh9Position22);
        assertEquals(fvh9Position22.getOpenPosition(), new BigDecimal(27));
        assertEqualsBD(fvh9Position22.getCurrentPosition(), 37);
    }

}
