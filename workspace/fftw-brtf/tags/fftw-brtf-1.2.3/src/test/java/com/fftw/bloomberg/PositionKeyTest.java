package com.fftw.bloomberg;

import org.testng.annotations.Test;

import com.fftw.bloomberg.types.BBProductCode;

import static org.testng.Assert.*;

/**
 * PositionKey Tester.
 * 
 */
public class PositionKeyTest {

    @Test(groups = { "unittest" })
    public void testHashCode() {

        int hashCode = "AAPL".hashCode() + (17 * "UnitTest".hashCode());
        hashCode = hashCode + (17 * "Level1".hashCode()) + (17 * "Level2".hashCode());
        hashCode = hashCode + (17 * "Level3".hashCode()) + (17 * "Level4".hashCode());
        hashCode = hashCode + (17 * BBProductCode.Equity.hashCode());

        PositionKey key = new PositionKey("AAPL", BBProductCode.Equity, "UnitTest", "Level1", "Level2", "Level3", "Level4", null, null);

        assertEquals(hashCode, key.hashCode(), "Hashcode value not consistent");
    }

    @Test(groups = { "unittest" })
    public void testEquals() {
        PositionKey key1 = new PositionKey("AAPL", BBProductCode.Equity, "UnitTest", "Level1", "Level2", "Level3", "Level4", null, null);
        PositionKey key2 = new PositionKey("AAPL", BBProductCode.Equity, "UnitTest", "Level1", "Level2", "Level3", "Level4", null, null);

        assertEquals(key1, key2, "Equal objects failed");

        PositionKey key3 = new PositionKey("AAPL", BBProductCode.Equity, "UnitTest", "Level1", "Level2", "Level3", "", null, null);

        assertTrue(!key1.equals(key3), "Non-equal objects equal field level4");

        PositionKey key4 = new PositionKey("AAPL", BBProductCode.Equity, "UnitTest", "Level1", "Level2", "", "Level4", null, null);

        assert !key1.equals(key4) : "Non-equal objects equal field level3";

        PositionKey key5 = new PositionKey("AAPL", BBProductCode.Equity, "UnitTest", "Level1", "", "Level3", "Level4", null, null);

        assert !key1.equals(key5) : "Non-equal objects equal field level2";

        PositionKey key6 = new PositionKey("AAPL", BBProductCode.Equity, "UnitTest", "", "Level2", "Level3", "Level4", null, null);

        assert !key1.equals(key6) : "Non-equal objects equal field level1";

        PositionKey key7 = new PositionKey("AAPL", BBProductCode.Equity, "", "Level1", "Level2", "Level3", "Level4", null, null);

        assert !key1.equals(key7) : "Non-equal objects equal field account";

        PositionKey key8 = new PositionKey("", BBProductCode.Equity, "UnitTest", "Level1", "Level2", "Level3", "Level4", null, null);

        assert !key1.equals(key8) : "Non-equal objects equal field securityId";

    }

}
