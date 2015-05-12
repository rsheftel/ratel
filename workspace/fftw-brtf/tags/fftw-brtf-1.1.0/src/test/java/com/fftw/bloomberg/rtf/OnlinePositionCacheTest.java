package com.fftw.bloomberg.rtf;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * OnlinePositionCache Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created June 6, 2008
 * @since 1.0
 */
public class OnlinePositionCacheTest {

    @Test(groups =
            {
                    "unittest"
                    })
    public void testAddOnlinePosition() {
        OnlinePositionCache cache = new OnlinePositionCache();
        RtfOnlinePosition bp = new RtfOnlinePosition("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4");
        bp.setPrimeBroker("PB1");

        bp.setOpenPosition(new BigDecimal("78.907"));
        bp.setCurrentPosition(new BigDecimal("12.5"));
        bp.setTotalBuyVolume(new BigDecimal("6888.0"));
        bp.setTotalSellVolume(new BigDecimal("-6888"));
        bp.setRealizedPL(new BigDecimal("67690"));

        bp.setTotalNumberOfBuys(4);
        bp.setTotalNumberOfSells(3);

        cache.addOnlinePosition(bp);

        assert cache.size() == 1 : "Failed to add item to cache";
        RtfOnlinePosition other = new RtfOnlinePosition("IBM", "UnitTest", "Level1", "Level2", "Level3", "Level4");

        other.setPrimeBroker("PB2");
        other.setOpenPosition(new BigDecimal("78.907"));
        other.setCurrentPosition(new BigDecimal("12.5"));
        other.setTotalBuyVolume(new BigDecimal("6888.0"));
        other.setTotalSellVolume(new BigDecimal("-6888"));
        other.setRealizedPL(new BigDecimal("67690"));

        other.setTotalNumberOfBuys(4);
        other.setTotalNumberOfSells(3);

        cache.addOnlinePosition(other);

        assert cache.size() == 2 : "Failed to add second item to cache";
    }

   

}
