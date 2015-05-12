package com.fftw.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

import com.fftw.bloomberg.types.BBProductCode;

public class SystematicFacadeTest {

    @Test(groups = { "unittest" })
    public void lookupBloombergContractSize() {
        // Bloomberg provides a shortcut to the front running security
        // We will use that in the tests.  This way we do not need to 
        // include the maturity month and year creation logic
        long tySize = SystematicFacade.lookupContractSize("TYA", BBProductCode.Commodity);
        assertEquals(tySize, 100000);
        
        long uxSize = SystematicFacade.lookupContractSize("UXA", BBProductCode.Index);
        assertEquals(uxSize, 1000);
        
        long ecSize = SystematicFacade.lookupContractSize("ECA", BBProductCode.Currency);
        assertEquals(ecSize, 125000);

        long bpSize = SystematicFacade.lookupContractSize("BPA", BBProductCode.Currency);
        assertEquals(bpSize, 62500);

        long hoSize = SystematicFacade.lookupContractSize("HOA", BBProductCode.Commodity);
        assertEquals(hoSize, 42000);

    }
    
    @Test(groups = { "unittest" })
    public void lookupEquityExchangeTicker() {
        String spiderTicker = SystematicFacade.lookupExchangeTicker("81369Y605");
        assertEquals(spiderTicker, "XLF");

        String cdsTicker = SystematicFacade.lookupExchangeTicker("SPK405TY9");
        assertEquals(cdsTicker, "/IG11");

        String corpTicker = SystematicFacade.lookupExchangeTicker("320517105");
        assertEquals(corpTicker, "FHN");

        String corpTicker2 = SystematicFacade.lookupExchangeTicker("SPR801981");
        assertEquals(corpTicker2, "/ITX11");
        
        // looking up bloomberg symbols as cusip does not work
        String furuesTicker = SystematicFacade.lookupExchangeTicker("TUM9");
        assertEquals(furuesTicker, "");
//
        
        
    }
    
}
