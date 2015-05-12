package com.fftw.positions.cache;

import static org.testng.Assert.*;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.positions.cache.OnlinePositionCache;

/**
 * OnlinePositionCache Tester.
 * 
 */
public class OnlinePositionCacheTest {

    @Test(groups = { "unittest" })
    public void testAddOnlinePosition() {
        // First Record
        OnlinePositionCache cache = new OnlinePositionCache();
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "PB1", BigDecimal.ZERO);

        bp.setProductCode(BBProductCode.Equity);
        bp.setCurrentPosition(new BigDecimal("12"));
        bp.setTotalBuyVolume(new BigDecimal("6888"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        cache.addOnlinePosition(bp);

        assertEquals(cache.size(), 1, "Failed to add item to cache");
        
        // Change the prime broker
        RtfOnlinePosition pb = new RtfOnlinePosition("IBM", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "PB2", BigDecimal.ZERO);
        
        pb.setProductCode(BBProductCode.Equity);
        pb.setCurrentPosition(new BigDecimal("12"));
        pb.setTotalBuyVolume(new BigDecimal("6888"));
        pb.setTotalSellVolume(new BigDecimal("-6888"));
        pb.setRealizedPL(new BigDecimal("67690"));

        pb.setTotalNumberOfBuys(4);
        pb.setTotalNumberOfSells(3);

        cache.addOnlinePosition(pb);

        assertEquals(cache.size(), 2, "Failed to add second item to cache");
        
        // Change the open position
        RtfOnlinePosition op = new RtfOnlinePosition("IBM", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "PB2", BigDecimal.ONE);
        
        op.setProductCode(BBProductCode.Equity);
        op.setCurrentPosition(new BigDecimal("12"));
        op.setTotalBuyVolume(new BigDecimal("6888"));
        op.setTotalSellVolume(new BigDecimal("-6888"));
        op.setRealizedPL(new BigDecimal("67690"));

        op.setTotalNumberOfBuys(4);
        op.setTotalNumberOfSells(3);

        cache.addOnlinePosition(op);

        assertEquals(cache.size(), 3, "Failed to add third item to cache");

        // Over-write the first
        RtfOnlinePosition over = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3",
            "Level4", "PB1", BigDecimal.ZERO);
        
        over.setProductCode(BBProductCode.Equity);
        over.setCurrentPosition(new BigDecimal("12"));
        over.setTotalBuyVolume(new BigDecimal("6888"));
        over.setTotalSellVolume(new BigDecimal("-6888"));
        over.setRealizedPL(new BigDecimal("67690"));

        over.setTotalNumberOfBuys(4);
        over.setTotalNumberOfSells(3);

        cache.addOnlinePosition(over);

        assertEquals(cache.size(), 3, "Failed to over-write an entry");
    }

}
