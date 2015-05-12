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

public class PrimeBrokerPositionCacheTest extends AbstractCacheTest {

    private static final int BATCH_FILE_RECORD_COUNT = 999;
    @Test(groups = { "unittest" })
    public void testPrimeBrokerPositionCacheBatchFileLoad() {
        // This file has short and long positions for XLB - the file is has 4 bad records (no SecurityId)
        List<BatchPosition> batchFilePositions = BatchPositionTest.loadFromFile("/ActiveMQAf200-2008-12-09.txt");

        BatchPositionCache bpc = new BatchPositionCache(batchFilePositions);
        assertEquals(bpc.size(), batchFilePositions.size());

        PositionAggregationCache pc = new PositionAggregationCache(
            new PrimeBrokerPositionAggregationStrategy(), batchFilePositions);


        Collection<PositionKey> aggregatedKeys = pc.getKeysWithAggregates();
        for (PositionKey key : aggregatedKeys) {
            // if (!"QMF".equals(key.getAccount())) continue;
            System.err.println(key);
        }

        assertEquals(pc.count(), BATCH_FILE_RECORD_COUNT);

        // Iterate over the batch records
        Collection<Position> batchValues = pc.values();
        assertEquals(batchValues.size(), BATCH_FILE_RECORD_COUNT);
      
        // Do some retrievals
        Position xlb = pc.getPosition(new PositionKey(XLB_CUSIP, BBProductCode.Equity, "QMF",
            null, null, null, null, "MSPB", null));
        assertNotNull(xlb);
        ISecurity xlbSecurity = xlb.getSecurity();
        
        assertNotNull(xlbSecurity);
        
        assertEquals(xlbSecurity.getSecurityId(), XLB_CUSIP);
        assertEquals(xlb.getAccount(), "QMF");
        assertNull(xlb.getLevel1TagName());
        assertNull(xlb.getLevel2TagName());
        assertNull(xlb.getLevel3TagName());
        assertNull(xlb.getLevel4TagName());
        assertEquals(xlb.getPrimeBroker(), "MSPB");
        assertEqualsBD(xlb.getCurrentPosition(), -37123);

        Position xlu = pc.getPosition(new PositionKey(XLU_CUSIP, BBProductCode.Equity, "QMF",
            null, null, null, null, "MSPB", null));
        assertNotNull(xlu);
        
        assertNotNull(xlu);
        ISecurity xluSecurity = xlu.getSecurity();

        assertEquals(xluSecurity.getSecurityId(), XLU_CUSIP);
        assertEquals(xlu.getAccount(), "QMF");
        assertNull(xlu.getLevel1TagName());
        assertNull(xlu.getLevel2TagName());
        assertNull(xlu.getLevel3TagName());
        assertNull(xlu.getLevel4TagName());
        assertEquals(xlu.getPrimeBroker(), "MSPB");
        assertEqualsBD(xlu.getCurrentPosition(), 565);

        // A failure
        Position missing = pc.getPosition(new PositionKey("DOESNOTEXIST", BBProductCode.Equity,
            "QMF", null, null, null, null, "MSPB", null));
        assertNull(missing);

        // Add a new entry
    }

    @Test(groups = { "unittest" })
    public void testPrimeBrokerPositionCacheOnlineFileLoad() {
        List<RtfOnlinePosition> filePositions = RtfOnlinePositionTest
            .loadFromFile("/OnlineMessages-2009-03-11.txt");

        OnlinePositionCache opc = new OnlinePositionCache(filePositions);
        // filePositions and opc will not be the same, online records are updates, not unique
        assertEquals(filePositions.size(), 56);
        assertEquals(opc.size(), 26);

        // We can not construct using the list, as we have a type erasure problem
        PositionAggregationCache pc = new PositionAggregationCache(
            new PrimeBrokerPositionAggregationStrategy());
        pc.addOnlinePositions(filePositions);

        // Iterate over the batch records
        Collection<Position> onlineValues = pc.values();
        assertEquals(onlineValues.size(), 24);

        // BPH9
        // Do some retrievals
        Position bpm9 = pc.getPosition(new PositionKey("BPM9", BBProductCode.Currency,
            "QMF", null, null, null, null, "GSFUT", null));
        assertNotNull(bpm9);
        
        ISecurity security = bpm9.getSecurity();
        assertNotNull(security);
        
        assertEquals(security.getSecurityId(), "BPM9");
        assertEquals(bpm9.getAccount(), "QMF");
        assertNull(bpm9.getLevel1TagName());
        assertNull(bpm9.getLevel2TagName());
        assertNull(bpm9.getLevel3TagName());
        assertNull(bpm9.getLevel4TagName());
        assertEquals(bpm9.getPrimeBroker(), "GSFUT");
        assertEqualsBD(bpm9.getCurrentPosition(), -6);
        assertEqualsBD(bpm9.getOpenPosition(), new BigDecimal(0));
        assertEquals(bpm9.getSharesBought(), 7);
        assertEquals(bpm9.getSharesSold(), 13);

        // Copy, change and add
    }

    @Test(groups = { "unittest" })
    public void testPrimeBrokerPositionCacheFileLoad() {
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
            new PrimeBrokerPositionAggregationStrategy(), batchFilePositions);

        assertEquals(pc.count(), 60);

        // Get the batch and online record for XLP
        Position xlpBatch = pc.getPosition(new PositionKey(XLP_CUSIP, BBProductCode.Equity, "QMF",
            null, null, null, null, "MSPB", null));
        assertNotNull(xlpBatch);
    }
}
