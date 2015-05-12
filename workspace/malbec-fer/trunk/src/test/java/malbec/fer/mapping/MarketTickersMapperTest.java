package malbec.fer.mapping;

import static org.testng.Assert.*;
import malbec.bloomberg.types.BBYellowKey;

import org.testng.annotations.Test;

public class MarketTickersMapperTest {

    
    @Test(groups = { "unittest" })
    public void testMapping() {
        MarketTickersMapper mtm = new MarketTickersMapper();
        
        String MARKET = "ED.1c";
        String BLOOMBERG = "EDCH9";
        String TSDB = "ED200903";
        String BBROOT = "ED";
        BBYellowKey YELLOW_KEY = BBYellowKey.Curncy;
        
        mtm.addMarketMapping(MARKET, BLOOMBERG, YELLOW_KEY.toString(), TSDB, BBROOT);
        
        String mappedBloombergLC = mtm.lookupBloomberg(MARKET.toLowerCase());
        assertEquals(mappedBloombergLC, BLOOMBERG, "Incorrect bloomberg returned");

        String mappedBloombergUP = mtm.lookupBloomberg(MARKET.toUpperCase());
        assertEquals(mappedBloombergUP, BLOOMBERG, "Incorrect bloomberg returned");

        BBYellowKey mappedMYK = mtm.lookupMarketYellowKey(MARKET);
        assertEquals(mappedMYK, YELLOW_KEY);

        BBYellowKey mappedYK = mtm.lookupYellowKey(BBROOT);
        assertEquals(mappedYK, YELLOW_KEY);
    }
}
