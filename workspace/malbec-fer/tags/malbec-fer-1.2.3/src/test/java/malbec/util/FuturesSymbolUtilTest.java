package malbec.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FuturesSymbolUtilTest {

    @Test(groups = { "unittest" })
    public void testFuturesSymbolRoot() {
        // Test everything that we might get

        String bbid = "TYM9 Comdty"; // Bloomberg with yellow key
        String ric = "TYM9"; // Bloomberg/RIC

        String root1 = FuturesSymbolUtil.extractSymbolRoot(bbid);
        assertEquals(root1, "TY", "Failed to extract root from Bloomberg with Yellow key");

        String root2 = FuturesSymbolUtil.extractSymbolRoot(ric);
        assertEquals(root2, "TY", "Failed to extract root from Bloomberg/RIC");

        String shortRootSymbol = "P M9";
        String root3 = FuturesSymbolUtil.extractSymbolRoot(shortRootSymbol);
        assertEquals(root3, "P", "Failed to extract root from a short symbol root");

        String shortSymbol = "P";
        String root4 = FuturesSymbolUtil.extractSymbolRoot(shortSymbol);
        assertEquals(root4, "P", "Failed to extract short root");

        String shortSymbol2 = "TU";
        String root4_1 = FuturesSymbolUtil.extractSymbolRoot(shortSymbol2);
        assertEquals(root4_1, "TU", "Failed to extract short root");

        // do some RIC parsing
        String bigRic = "TTAH0";
        
        String root5 = FuturesSymbolUtil.extractSymbolRoot(bigRic);
        assertEquals(root5, "TTA", "Failed to extract big root from RIC");

        String numbers = "6BH0";
        
        String root6 = FuturesSymbolUtil.extractSymbolRoot(numbers);
        assertEquals(root6, "6B", "Failed to extract numeric root");

    }

    @Test(groups = { "unittest" })
    public void testBloombergToMaturityMonthYear() {
        String bbid = "TYM9 Comdty"; // Bloomberg with yellow key
        String ric = "TYM9"; // Bloomberg/RIC

        String my1 = FuturesSymbolUtil.bloombergToMaturityMonthYear(bbid);
        assertEquals(my1, "200906", "Failed to extract month/year from Bloomberg with Yellow key");

        String my2 = FuturesSymbolUtil.bloombergToMaturityMonthYear(ric);
        assertEquals(my2, "200906", "Failed to extract month/year from Bloomberg/RIC");

        String shortRootSymbol = "P M9";
        String my3 = FuturesSymbolUtil.bloombergToMaturityMonthYear(shortRootSymbol);
        assertEquals(my3, "200906", "Failed to extract month/year from a short symbol root");

        String shortSymbol = "P";
        boolean testPasted = false;
        try {
            FuturesSymbolUtil.bloombergToMaturityMonthYear(shortSymbol);
        } catch (IllegalArgumentException e) {
            testPasted = true;
        }
        assertTrue(testPasted, "Failed to throw exception on invalid futures symbol");

        String year1 = "TTH1";
        String root5 = FuturesSymbolUtil.bloombergToMaturityMonthYear(year1);
        assertEquals(root5, "201103", "Failed to extract month/year from big RIC");

        String year2 = "TTH8";
        String root6 = FuturesSymbolUtil.bloombergToMaturityMonthYear(year2);
        assertEquals(root6, "201803", "Failed to extract month/year from big RIC");
        
        // do some RIC parsing
        String bigRic = "TTAH0";
        
        String root7 = FuturesSymbolUtil.bloombergToMaturityMonthYear(bigRic);
        assertEquals(root7, "201003", "Failed to extract month/year from big RIC");

        String numbers = "6BH0";
        
        String root8 = FuturesSymbolUtil.bloombergToMaturityMonthYear(numbers);
        assertEquals(root8, "201003", "Failed to extract month/year from numeric");

        String pp = "NGN8";
        
        String root9 = FuturesSymbolUtil.bloombergToMaturityMonthYear(pp);
        assertEquals(root9, "201807", "Failed to extract month/year from numeric");
    }
    
    @Test(groups = { "unittest" })
    public void testCombineRootMaturityMonthYear() {
        // Test everything that we might get

        String ric = "TY"; // Bloomberg/RIC
        String mmy = "200906";

        String fullSymbol1 = FuturesSymbolUtil.combineRootMaturityMonthYear(ric, mmy);
        assertEquals(fullSymbol1, "TYM9", "Failed to combine symbol");

        String fullSymbol2 = FuturesSymbolUtil.combineRootMaturityMonthYear("P", mmy);
        assertEquals(fullSymbol2, "P M9", "Failed to combine symbol");

        // do some RIC parsing
        
        String fullSymbol3 = FuturesSymbolUtil.combineRootMaturityMonthYear("TTA", "201003");
        assertEquals(fullSymbol3, "TTAH0", "Failed to combine symbol");
        
        String fullSymbol4 = FuturesSymbolUtil.combineRootMaturityMonthYear("6B", "201003");
        assertEquals(fullSymbol4, "6BH0", "Failed to combine symbol");

    }
    
}
