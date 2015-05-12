package malbec.fer.mapping;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;

import malbec.AbstractBaseTest;

import org.testng.annotations.Test;

public class FuturesSymbolMapperTest extends AbstractBaseTest {
    
    @Test(groups = { "unittest" })
    public void testMapping() {
        FuturesSymbolMapper fsm = new FuturesSymbolMapper();
        String PLATFORM = "TEST";
        String BLOOMBERG_ROOT = "SF";
        String PLATFORM_RECEIVE_ROOT = "6S";
        String PLATFORM_SEND_ROOT = "SFSS";  // RIC root
        
        // Setup our test data
        fsm.addBloombergMapping(PLATFORM, BLOOMBERG_ROOT, PLATFORM_RECEIVE_ROOT, PLATFORM_SEND_ROOT, BigDecimal.TEN);
        
        String mappedBloomberg = fsm.lookupBloombergRoot(PLATFORM, PLATFORM_RECEIVE_ROOT, false);
        assertEquals(mappedBloomberg, BLOOMBERG_ROOT, "Incorrect bloomberg root returned");

        String ricRoot = fsm.lookupPlatformSendingRoot(PLATFORM, BLOOMBERG_ROOT, false);
        assertEquals(ricRoot, PLATFORM_SEND_ROOT);

        String mappedPlatform = fsm.lookupPlatformRoot(PLATFORM, BLOOMBERG_ROOT, null);
        assertEquals(mappedPlatform, PLATFORM_RECEIVE_ROOT, "Incorrect platform root returned");

        BigDecimal toBB = fsm.lookupToBloombergPriceMultiplier(PLATFORM, BLOOMBERG_ROOT);
        assertEquals(toBB, BigDecimal.TEN);
        
        BigDecimal toPlatform = fsm.lookupToPlatformPriceMultiplier(PLATFORM, BLOOMBERG_ROOT);
        assertEqualsBD(toPlatform, BigDecimal.ONE.divide(BigDecimal.TEN));

        // Test logic for converting exchange to bloomberg pricing and vice versa
        // multiply by 10
        BigDecimal bbPrice = new BigDecimal("0.7268").multiply(toBB);
        assertEqualsBD(bbPrice, "7.268");
        
        // multiply by 1/10
        BigDecimal platformPrice = new BigDecimal("72.68").multiply(toPlatform);
        assertEqualsBD(platformPrice, "7.268");
        
        
        String bbSymbol = fsm.mapPlatformRootToBloombergSymbol(PLATFORM, PLATFORM_RECEIVE_ROOT, "200906");
        assertEquals(bbSymbol, "SFM9");
    }
}
