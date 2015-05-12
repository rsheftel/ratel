package malbec.fer.mapping;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class MarketTickersMapperTest {

    
    @Test(groups = { "unittest" })
    public void testMapping() {
        MarketTickersMapper mtm = new MarketTickersMapper();
        
        String MARKET = "EC.1c";
        String BLOOMBERG = "ECH9";
        String TSDB = "EC200903";
        
        mtm.addMarketMapping(MARKET, BLOOMBERG, TSDB);
        
        String mappedBloombergLC = mtm.lookupBloomberg(MARKET.toLowerCase());
        assertEquals(mappedBloombergLC, BLOOMBERG, "Incorrect bloomberg returned");

        String mappedBloombergUP = mtm.lookupBloomberg(MARKET.toUpperCase());
        assertEquals(mappedBloombergUP, BLOOMBERG, "Incorrect bloomberg returned");
    }
}
