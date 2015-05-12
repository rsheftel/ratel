package bloomberg;

import static util.Range.*;
import tsdb.*;

public class TestBloombergHistoricalData extends BloombergTestCase {
    public void testSomeHistoricalData() throws Exception {
//        BloombergRequest request = new BloombergRequest("OldEquityClose", AAPL, IBM);
//        BloombergObservations values = request.values(date("2008/07/07"));
//        assertMatches(175.16, values.get(AAPL).value());
//        assertMatches(121.50, values.get(IBM).value());
    }
     
    
    public void testOtherStuff() throws Exception {
//        BloombergSecurity security = new BloombergSecurity("FNCL 6.0 07/2008 @BBT3 MTGE");
//        BloombergRequest request = new BloombergRequest("TbaClose", security);
//        Date d = date("2008-07-09");
//        assertEquals(101.0 + 14.0/32.0, request.values(d).only().value());
    }
    
    public void testHistoricalDataDownload()  throws Exception {
    	BloombergSecurity security = new BloombergSecurity("USDEUR Curncy");
    	Observations data = security.observations("LAST_PRICE", range("2009/01/01", "2009/02/01"));
    	assertEquals(0.7187, data.value("2009/01/02"));
    	assertSize(22, data.times());
    }
    
    public void functestHistoricalDataDownload()  throws Exception {
    	BloombergSecurity security = new BloombergSecurity("USDEUR Curncy");
    	Observations data = security.observations("LAST_PRICE", range("2001/01/01", "2009/01/01"));
    	assertSize(2089, data.times());
    }
}
