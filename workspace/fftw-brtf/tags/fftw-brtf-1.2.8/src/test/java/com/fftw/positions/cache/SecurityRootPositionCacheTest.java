package com.fftw.positions.cache;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePositionTest;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.positions.ISecurity;
import com.fftw.positions.Position;

public class SecurityRootPositionCacheTest extends AbstractCacheTest {

    @Test(groups = { "unittest" })
    public void testSecurityRootPositionCacheBatchFileLoad() {
        List<BatchPosition> batchFilePositions = BatchPositionTest
            .loadFromFile("/ActiveMQAf200-2008-12-09.txt");

        BatchPositionCache bpc = new BatchPositionCache(batchFilePositions);
        assertEquals(bpc.size(), batchFilePositions.size());

        PositionAggregationCache srac = new PositionAggregationCache(
            new SecurityRootPositionAggregationStrategy(), batchFilePositions);

        Collection<PositionKey> aggregatedKeys = srac.getKeysWithAggregates();
        assertTrue(aggregatedKeys.size() > 0);
        assertEquals(aggregatedKeys.size(), BATCH_2008_12_09_SR_AK);
     
        assertEquals(srac.count(), BATCH_2008_12_09_SR_CACHE_COUNT);

        assertEquals(srac.values().size(), BATCH_2008_12_09_SR_CACHE_COUNT);

        // Do some retrievals
        Position xlb = srac.getPosition(new PositionKey(XLB_CUSIP, BBProductCode.Equity, "QMF", "QF.SporCor",
            "MeanReversion", "General", "QUANTYS", "MSPB", null));
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
        assertEqualsBD(xlb.getCurrentPosition(), new BigDecimal(-37123));

        Position xlu = srac.getPosition(new PositionKey(XLU_CUSIP, BBProductCode.Equity, "QMF", "QF.SporCor",
            "MeanReversion", "General", "QUANTYS", "MSPB", null));
        assertNotNull(xlu);
        ISecurity xluSecurity = xlu.getSecurity();
        assertNotNull(xluSecurity);

        assertEquals(xluSecurity.getSecurityId(), XLU_CUSIP);
        assertEquals(xlu.getAccount(), "QMF");
        assertEquals(xlu.getLevel1TagName(), "QF.SporCor");
        assertEquals(xlu.getLevel2TagName(), "MeanReversion");
        assertEquals(xlu.getLevel3TagName(), "General");
        assertEquals(xlu.getLevel4TagName(), "QUANTYS");
        assertEquals(xlu.getPrimeBroker(), "MSPB");
        assertEqualsBD(xlu.getCurrentPosition(), new BigDecimal(565));

        Position fv = srac.getPosition(new PositionKey("FV", BBProductCode.Commodity, "TEST", "TEST",
            "TEST_", "TEST..", "TEST.", "MSPB", null));
        assertNotNull(fv);
        ISecurity fvSecurity = fv.getSecurity();
        assertNotNull(fvSecurity);

        assertEquals(fvSecurity.getSecurityId(), "FV");
        assertEquals(fv.getAccount(), "TEST");
        assertEquals(fv.getLevel1TagName(), "TEST");
        assertEquals(fv.getLevel2TagName(), "TEST_");
        assertEquals(fv.getLevel3TagName(), "TEST..");
        assertEquals(fv.getLevel4TagName(), "TEST.");
        assertEquals(fv.getPrimeBroker(), "MSPB");
        assertEqualsBD(fv.getCurrentPosition(), new BigDecimal(-9));

        // A failure
        Position missing = srac.getPosition(new PositionKey("DOESNOTEXIST", BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", null));
        assertNull(missing);

        // Add a new entry
        BatchPosition ibm = BatchPosition.valueOf(new LocalDate(), EQUITY_TEST1);
        Position addedItem = srac.addBatchPosition(ibm);
        assertNotNull(addedItem);
        // get it back
        Position ibmLookup = srac.getPosition(new PositionKey("459200101", BBProductCode.Equity, "TEST",
            "TEST", "TEST_", "TEST..", "TEST.", "MSPB", null));
        assertNotNull(ibmLookup);
    }

    @Test(groups = { "unittest" })
    public void testSecurityRootPositionCacheOnlineFileLoad() throws Exception {

        List<RtfOnlinePosition> onlineFilePositions = RtfOnlinePositionTest
            .loadFromFile("/OnlineMessages-2009-03-11.txt");

        OnlinePositionCache opc = new OnlinePositionCache(onlineFilePositions);
        // filePositions and opc will not be the same, online records are updates, not unique
        assertEquals(onlineFilePositions.size(), 56);
        assertEquals(opc.size(), 26);

        // We can not construct using the list, as we have a type erasure problem
        PositionAggregationCache srac = new PositionAggregationCache(
            new SecurityRootPositionAggregationStrategy());

        srac.addOnlinePositions(onlineFilePositions);

        // Iterate over the batch records
        Collection<Position> positionValues = srac.values();
        assertEquals(positionValues.size(), 19);

        // BPH9
        // Do some retrievals
        Position bpm9 = srac.getPosition(new PositionKey("BP", BBProductCode.Currency, "QMF", "QF.NDayBreak",
            "Breakout", "General", "QUANTYS", "GSFUT", null));
        assertNotNull(bpm9);
        ISecurity bpm9Security = bpm9.getSecurity();
        assertNotNull(bpm9Security);

        assertEquals(bpm9Security.getSecurityId(), "BP");
        assertEquals(bpm9.getAccount(), "QMF");
        assertEquals(bpm9.getLevel1TagName(), "QF.NDayBreak");
        assertEquals(bpm9.getLevel2TagName(), "Breakout");
        assertEquals(bpm9.getLevel3TagName(), "General");
        assertEquals(bpm9.getLevel4TagName(), "QUANTYS");
        assertEquals(bpm9.getPrimeBroker(), "GSFUT");
        assertEqualsBD(bpm9.getCurrentPosition(), new BigDecimal(-8));
        assertEqualsBD(bpm9.getOpenPosition(), new BigDecimal(-8));
        assertEquals(bpm9.getSharesBought(), 6);
        assertEquals(bpm9.getSharesSold(),6);

        // add
        RtfOnlinePosition bph9 = RtfOnlinePosition.valueOfCommaString(CURRENCY_ONLINE_TEST3);
        Position bph9Added = srac.addOnlinePosition(bph9);
        assertNotNull(bph9Added);

    }

    @Test(groups = { "unittest" })
    public void testSecurityRootPositionCacheFileLoad() {

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

        PositionAggregationCache srac = new PositionAggregationCache(
            new SecurityRootPositionAggregationStrategy(), batchFilePositions, onlineFilePositions);

        assertEquals(srac.count(), 63);
        Collection<PositionKey> aggregatedKeys = srac.getKeysWithAggregates();
        
        for (PositionKey key : aggregatedKeys) {
            // if (!"QMF".equals(key.getAccount())) continue;
            System.err.println(key);
        }
        
        assertTrue(aggregatedKeys.size() > 0);
        assertEquals(aggregatedKeys.size(), 6);
        
        // Get the batch and online record for XLP
        Position xlpPosition = srac.getPosition(new PositionKey(XLP_CUSIP, BBProductCode.Equity, "QMF",
            "QF.SporCor", "MeanReversion", "General", "QUANTYS", "MSPB", null));
        assertNotNull(xlpPosition);
        assertEqualsBD(xlpPosition.getOpenPosition(), -52799);
        assertEquals(xlpPosition.getSharesBought(), 118375);
        assertEquals(xlpPosition.getSharesSold(), 0);
        assertEqualsBD(xlpPosition.getCurrentPosition(), 65576);
        
        Position ng = srac.getPosition(new PositionKey("CD", BBProductCode.Currency, "QMF", "QF.NDayBreak",
            "Breakout", "General", "QUANTYS", "GSFUT", null));
        assertNotNull(ng);
        ISecurity ngSecurity = ng.getSecurity();
        assertNotNull(ngSecurity);

        assertEquals(ngSecurity.getSecurityId(), "CD");
        assertEquals(ng.getAccount(), "QMF");
        assertEquals(ng.getLevel1TagName(), "QF.NDayBreak");
        assertEquals(ng.getLevel2TagName(), "Breakout");
        assertEquals(ng.getLevel3TagName(), "General");
        assertEquals(ng.getLevel4TagName(), "QUANTYS");
        assertEquals(ng.getPrimeBroker(), "GSFUT");
        assertEqualsBD(ng.getCurrentPosition(), 0);
        assertEqualsBD(ng.getOpenPosition(),8);
        assertEquals(ng.getSharesBought(),0);
        assertEquals(ng.getSharesSold(),8);

    }
}
