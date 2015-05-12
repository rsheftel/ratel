package malbec.fer.mapping;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FuturesSymbolMapperTest {

    
    @Test(groups = { "unittest" })
    public void testMapping() {
        FuturesSymbolMapper fsm = new FuturesSymbolMapper();
        String PLATFORM = "TEST";
        String BLOOMBERG_ROOT = "DI";
        String PLATFORM_ROOT = "SR";
        String RIC_ROOT = "NI";
        
        
        fsm.addBloombergMapping(PLATFORM, BLOOMBERG_ROOT, PLATFORM_ROOT);
        fsm.addBloombergToRicMapping(PLATFORM, BLOOMBERG_ROOT, RIC_ROOT);
        
        String mappedPlatform = fsm.mapBloombergRootToPlatformRoot(PLATFORM, BLOOMBERG_ROOT, null);
        assertEquals(mappedPlatform, PLATFORM_ROOT, "Incorrect platform root returned");

        String mappedRic = fsm.mapBloombergRootToRicRoot(PLATFORM, BLOOMBERG_ROOT);
        assertEquals(mappedRic, RIC_ROOT, "Incorrect ric root returned");
        
        String mappedBloomberg = fsm.mapPlatformRootToBloombergRoot(PLATFORM, PLATFORM_ROOT, false);
        assertEquals(mappedBloomberg, BLOOMBERG_ROOT, "Incorrect bloomberg root returned");
        
    }
}
