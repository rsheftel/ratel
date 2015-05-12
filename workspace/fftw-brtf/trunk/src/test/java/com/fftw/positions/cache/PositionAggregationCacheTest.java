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
import com.fftw.positions.ISecurity;
import com.fftw.positions.Position;

public class PositionAggregationCacheTest extends AbstractCacheTest {

    private static final int BATCH_FILE_RECORD_COUNT = 1121;
    @Test(groups = { "unittest" })
    public void testPositionCacheBatchFileLoad() {
        // This file has short and long positions for XLB - the file is has 4 bad records (no SecurityId)
        List<BatchPosition> batchFilePositions = BatchPositionTest.loadFromFile("/ActiveMQAf200-2008-12-09.txt");

        BatchPositionCache bpc = new BatchPositionCache(batchFilePositions);
        assertEquals(bpc.size(), batchFilePositions.size());

        PositionAggregationCache pc = new PositionAggregationCache(
            new DefaultPositionAggregationStrategy(), batchFilePositions);

        Collection<PositionKey> aggregatedKeys = pc.getKeysWithAggregates();
        for (PositionKey key : aggregatedKeys) {
            // if (!"QMF".equals(key.getAccount())) continue;
            System.err.println(key);
        }

        assertEquals(pc.count(), BATCH_FILE_RECORD_COUNT);

        Collection<BatchPosition> batchPositions = pc.getBatchPositions();
        assertNotNull(batchPositions);
        assertEquals(batchPositions.size(), batchFilePositions.size());
        
        // Iterate over the batch records
        Collection<Position> batchValues = pc.values();
        assertEquals(batchValues.size(), BATCH_FILE_RECORD_COUNT);
      
        // Do some retrievals
        Position xlb = pc.getPosition(new PositionKey(XLB_CUSIP, BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", new BigDecimal(-37219)));
        assertNotNull(xlb);
        ISecurity xlbSecurity = xlb.getSecurity();
        
        assertNotNull(xlbSecurity);
        
        assertEquals(xlbSecurity.getSecurityId(), XLB_CUSIP);
        assertEquals(xlb.getAccount(), "QMF");
        assertEquals(xlb.getLevel1TagName(), "QF.SporCor");
        assertEquals(xlb.getLevel2TagName(), "MeanReversion");
        assertEquals(xlb.getLevel3TagName(), "General");
        assertEquals(xlb.getLevel4TagName(), "QUANTYS");
        assertEquals(xlb.getPrimeBroker(), "MSPB");
        assertEqualsBD(xlb.getCurrentPosition(), -37219);

        Position xlu = pc.getPosition(new PositionKey(XLU_CUSIP, BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", new BigDecimal(565)));
        assertNotNull(xlu);
        
        assertNotNull(xlu);
        ISecurity xluSecurity = xlu.getSecurity();

        assertEquals(xluSecurity.getSecurityId(), XLU_CUSIP);
        assertEquals(xlu.getAccount(), "QMF");
        assertEquals(xlu.getLevel1TagName(), "QF.SporCor");
        assertEquals(xlu.getLevel2TagName(), "MeanReversion");
        assertEquals(xlu.getLevel3TagName(), "General");
        assertEquals(xlu.getLevel4TagName(), "QUANTYS");
        assertEquals(xlu.getPrimeBroker(), "MSPB");
        assertEqualsBD(xlu.getCurrentPosition(), 565);

        // A failure
        Position missing = pc.getPosition(new PositionKey("DOESNOTEXIST", BBProductCode.Equity,
            "QMF", null, null, null, null, "MSPB", null));
        assertNull(missing);

        
    }

    @Test(groups = { "unittest" })
    public void testPositionCacheOnlineFileLoad() {
        List<RtfOnlinePosition> filePositions = RtfOnlinePositionTest
            .loadFromFile("/OnlineMessages-2009-03-11.txt");

        OnlinePositionCache opc = new OnlinePositionCache(filePositions);
        // filePositions and opc will not be the same, online records are updates, not unique
        assertEquals(filePositions.size(), 56);
        assertEquals(opc.size(), 26);

        // We can not construct using the list, as we have a type erasure problem
        PositionAggregationCache pc = new PositionAggregationCache(
            new DefaultPositionAggregationStrategy());
        pc.addOnlinePositions(filePositions);

        // Iterate over the batch records
        Collection<Position> onlineValues = pc.values();
        assertEquals(onlineValues.size(), 26);

        Collection<RtfOnlinePosition> onlinePositions = pc.getOnlinePositions();
        assertNotNull(onlinePositions);
        assertEquals(onlinePositions.size(), 26);
        
        // BPH9
        // Do some retrievals
        Position bpm9 = pc.getPosition(new PositionKey("BPM9", BBProductCode.Currency,
            "QMF", "QF.FaderClose", "MeanReversion", "General", "QUANTYS", "GSFUT", BigDecimal.ZERO));
        assertNotNull(bpm9);
        
        ISecurity security = bpm9.getSecurity();
        assertNotNull(security);
        
        assertEquals(security.getSecurityId(), "BPM9");
        assertEquals(bpm9.getAccount(), "QMF");
        assertEquals(bpm9.getLevel1TagName(), "QF.FaderClose");
        assertEquals(bpm9.getLevel2TagName(), "MeanReversion");
        assertEquals(bpm9.getLevel3TagName(), "General");
        assertEquals(bpm9.getLevel4TagName(), "QUANTYS");
        assertEquals(bpm9.getPrimeBroker(), "GSFUT");
        assertEqualsBD(bpm9.getCurrentPosition(), 0);
        assertEqualsBD(bpm9.getOpenPosition(), new BigDecimal(0));
        assertEquals(bpm9.getSharesBought(), 7);
        assertEquals(bpm9.getSharesSold(), 7);

        // Copy, change and add
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
        assertEquals(opc.size(), ONLINE_2009_01_14_CACHE_COUNT);

        PositionAggregationCache pc = new PositionAggregationCache(
            new DefaultPositionAggregationStrategy(), batchFilePositions);

        assertEquals(pc.count(), 65);

        // Get the batch and online record for XLP
        Position xlpBatch = pc.getPosition(new PositionKey(XLP_CUSIP, BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", new BigDecimal(-52799)));
        assertNotNull(xlpBatch);
        
        
        Collection<BatchPosition> batchPositions = pc.getBatchPositions();
        assertNotNull(batchPositions);
        assertEquals(batchPositions.size(), batchFilePositions.size());
        
        pc.addOnlinePositions(onlineFilePositions);
        Collection<RtfOnlinePosition> onlinePositions = pc.getOnlinePositions();
        assertNotNull(onlinePositions);
        assertEquals(onlinePositions.size(), ONLINE_2009_01_14_CACHE_COUNT);
    }
}
