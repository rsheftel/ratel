package malbec.util;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;

import malbec.AbstractBaseTest;
import malbec.bloomberg.types.BBYellowKey;

import org.testng.annotations.Test;

public class SystematicFacadeTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void lookupBloombergContractSize() {
        // Bloomberg provides a shortcut to the front running security
        // We will use that in the tests. This way we do not need to
        // include the maturity month and year creation logic
        long tySize = SystematicFacade.lookupContractSize("TYA", BBYellowKey.Comdty);
        assertEquals(tySize, 100000);

        long uxSize = SystematicFacade.lookupContractSize("UXA", BBYellowKey.Index);
        assertEquals(uxSize, 1000);

        long ecSize = SystematicFacade.lookupContractSize("ECA", BBYellowKey.Curncy);
        assertEquals(ecSize, 125000);

        long bpSize = SystematicFacade.lookupContractSize("BPA", BBYellowKey.Curncy);
        assertEquals(bpSize, 62500);

        long hoSize = SystematicFacade.lookupContractSize("HOA", BBYellowKey.Comdty);
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
    }

    @Test(groups = { "unittest" })
    public void lookupFuturesTickSize() {
        BigDecimal equityTickSize = SystematicFacade.lookupFuturesTickSize("81369Y605", BBYellowKey.Equity);
        assertEqualsBD(equityTickSize, 0.015625);

        BigDecimal cdsTickSize = SystematicFacade.lookupFuturesTickSize("SPK405TY9", BBYellowKey.Corp);
        assertEqualsBD(cdsTickSize, 0.015625);

        BigDecimal corpTickSize = SystematicFacade.lookupFuturesTickSize("320517105", BBYellowKey.Corp);
        assertEqualsBD(corpTickSize, 0.015625);

        BigDecimal corpTickSize2 = SystematicFacade.lookupFuturesTickSize("SPR801981", BBYellowKey.Corp);
        assertEqualsBD(corpTickSize2, 0.015625);

        BigDecimal furuesTickSize = SystematicFacade.lookupFuturesTickSize("TUM9",  BBYellowKey.Comdty);
        assertEqualsBD(furuesTickSize, 0.0078125);
        
        BigDecimal furuesTickSize2 = SystematicFacade.lookupFuturesTickSize("TYM9",  BBYellowKey.Comdty);
        assertEqualsBD(furuesTickSize2, 0.015625);

        // This changes as the security ages, using ED1 instead of EDM9
        BigDecimal furuesTickSize3 = SystematicFacade.lookupFuturesTickSize("ED1",  BBYellowKey.Curncy);
        //assertEqualsBD(furuesTickSize3, 0.005);
        assertEqualsBD(furuesTickSize3, 0.0025);
        
        BigDecimal furuesTickSize4 = SystematicFacade.lookupFuturesTickSize("USM9",  BBYellowKey.Curncy);
        assertEqualsBD(furuesTickSize4, 0.015625);

    }

    @Test(groups = { "unittest" })
    public void lookupTickerPrimaryExchange() {
        String ibmExchange = SystematicFacade.lookupTickerPrimaryExchange("IBM");
        assertEquals(ibmExchange, "NYSE");
        
        String xlfExchange = SystematicFacade.lookupTickerPrimaryExchange("XLF");
        assertEquals(xlfExchange, "ARCA");

        String msftExchange = SystematicFacade.lookupTickerPrimaryExchange("MSFT");
        assertEquals(msftExchange, "INET");

        String aaplExchange = SystematicFacade.lookupTickerPrimaryExchange("AAPL");
        assertEquals(aaplExchange, "INET");

        String eemExchange = SystematicFacade.lookupTickerPrimaryExchange("EEM");
        assertEquals(eemExchange, "ARCA");
    }
}
