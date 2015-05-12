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

        String shortSymbolSpace = "P ";
        String root4s = FuturesSymbolUtil.extractSymbolRoot(shortSymbolSpace);
        assertEquals(root4s, "P", "Failed to extract short root");

        
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

        // Try futures options
        String futuresOptions = "TYH9C";
        
        String root7 = FuturesSymbolUtil.extractSymbolRoot(futuresOptions);
        assertEquals(root7, "TY_C", "Failed to extract futures option root");

        // Try futures options
        String futuresOptionsRoot = "TY_P";
        
        String root8 = FuturesSymbolUtil.extractSymbolRoot(futuresOptionsRoot);
        assertEquals(root8, "TY_P", "Failed to extract futures option root");
        
        // Try futures options
        String weirdSymbol = "ESH8 08";
        
        String root9 = FuturesSymbolUtil.extractSymbolRoot(weirdSymbol);
        assertEquals(root9, "ES", "Failed to extract weird root");
        
        String root10 = FuturesSymbolUtil.extractSymbolRoot("VXH0:VE");
        assertEquals(root10, "VX", "Failed to extract weird root");

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
        
        // Test the funny ric root
        String root10 = FuturesSymbolUtil.bloombergToMaturityMonthYear("VXH0:VE");
        assertEquals(root10, "201003", "Failed to extract month/year from numeric");
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
        
        // Test the funny ric root
        String fullSymbol5 = FuturesSymbolUtil.combineRootMaturityMonthYear("VX:VE", "201003");
        assertEquals(fullSymbol5, "VXH0:VE", "Failed to combine symbol");
    }

    @Test(groups = { "unittest" })
    public void TestExtractMaturityMonthYear()
    {
        // Test everything that we might get
        String mmy = "200906";

        String mmy1 = FuturesSymbolUtil.extractMaturityMonthFromSymbol("TYM9");
        assertEquals(mmy, mmy1);

        String mmy2 = FuturesSymbolUtil.extractMaturityMonthFromSymbol("P M9");
        assertEquals(mmy, mmy2);

        // do some RIC parsing
        String mmy3 = FuturesSymbolUtil.extractMaturityMonthFromSymbol("TTAH0");
        assertEquals("201003", mmy3);

        String mmy4 = FuturesSymbolUtil.extractMaturityMonthFromSymbol("6BH0");
        assertEquals("201003", mmy4);
        
        String mmy5 = FuturesSymbolUtil.extractMaturityMonthFromSymbol("VXH0:VE");
        assertEquals("201003", mmy5);

    }

}
