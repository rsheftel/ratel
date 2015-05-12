package malbec.bloomberg.types;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class BBYellowKeyTest {

    @Test(groups = { "unittest" })
    public void testShortName() {
        assertEquals(BBYellowKey.fromCmf("CMDT"), BBYellowKey.Comdty);
        assertEquals(BBYellowKey.fromCmf("muni"), BBYellowKey.Muni);
        assertEquals(BBYellowKey.fromCmf("GUESS"), BBYellowKey.Unknown);
    }

    @Test(groups = { "unittest" })
    public void testLongName() {
        assertEquals(BBYellowKey.fromLongName("Commodity"), BBYellowKey.Comdty);
        assertEquals(BBYellowKey.fromLongName("municipals"), BBYellowKey.Muni);
        assertEquals(BBYellowKey.fromLongName("GUESS"), BBYellowKey.Unknown);
    }

    @Test(groups = { "unittest" })
    public void testValueFor() {
        assertEquals(BBYellowKey.valueFor("Comdty"), BBYellowKey.Comdty);
        assertEquals(BBYellowKey.valueFor("muni"), BBYellowKey.Muni);
        assertEquals(BBYellowKey.valueFor("GUESS"), BBYellowKey.Unknown);
    }

    
    @Test(groups = { "unittest" })
    public void testValueOfCode() {
        assertEquals(BBYellowKey.valueOf(1), BBYellowKey.Comdty);
        assertEquals(BBYellowKey.valueOf(3), BBYellowKey.Muni);
        assertEquals(BBYellowKey.valueOf(11), BBYellowKey.Mtge);
        assertEquals(BBYellowKey.valueOf(0), BBYellowKey.Unknown);
        assertEquals(BBYellowKey.valueOf(-1), BBYellowKey.Unknown);
        assertEquals(BBYellowKey.valueOf(12), BBYellowKey.Unknown);
    }

    
    @Test(groups = { "unittest" })
    public void testValueOf() {
        assertEquals(BBYellowKey.valueOf("Comdty"), BBYellowKey.Comdty);

        try {
            BBYellowKey.valueOf("muni");
            fail();
        } catch (IllegalArgumentException e) {

        }
        try {
            BBYellowKey.valueOf("GUESS");
            fail();
        } catch (IllegalArgumentException e) {

        }

    }

}
