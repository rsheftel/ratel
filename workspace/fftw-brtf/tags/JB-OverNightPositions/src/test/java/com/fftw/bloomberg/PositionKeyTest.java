package com.fftw.bloomberg;

import java.math.BigDecimal;

import org.testng.annotations.*;

/**
 * PositionKey Tester.
 * 
 * @created March 7, 2008
 * @since 1.0
 */
public class PositionKeyTest {

    @Test(groups = { "unittest" })
    public void testHashCode() {

        int hashCode = "AAPL".hashCode() + (17 * "UnitTest".hashCode());
        hashCode = hashCode + (17 * "Level1".hashCode()) + (17 * "Level2".hashCode());
        hashCode = hashCode + (17 * "Level3".hashCode()) + (17 * "Level4".hashCode());
        hashCode = hashCode + (17 * 10);

        PositionKey key = new PositionKey("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4", BigDecimal.TEN);

        assert hashCode == key.hashCode() : "Hashcode value not consistent";
    }

    @Test(groups = { "unittest" })
    public void testEquals() {
        PositionKey key1 = new PositionKey("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4", new BigDecimal(98));
        PositionKey key2 = new PositionKey("AAPL", "UnitTest", "Level1", "Level2", "Level3", "Level4", new BigDecimal("98.00"));

        assert key1.equals(key2) : "Equal objects failed";

        PositionKey key3 = new PositionKey("AAPL", "UnitTest", "Level1", "Level2", "Level3", "", BigDecimal.TEN);

        assert !key1.equals(key3) : "Non-equal objects equal field level4";

        PositionKey key4 = new PositionKey("AAPL", "UnitTest", "Level1", "Level2", "", "Level4", BigDecimal.TEN);

        assert !key1.equals(key4) : "Non-equal objects equal field level3";

        PositionKey key5 = new PositionKey("AAPL", "UnitTest", "Level1", "", "Level3", "Level4", BigDecimal.TEN);

        assert !key1.equals(key5) : "Non-equal objects equal field level2";

        PositionKey key6 = new PositionKey("AAPL", "UnitTest", "", "Level2", "Level3", "Level4", BigDecimal.TEN);

        assert !key1.equals(key6) : "Non-equal objects equal field level1";

        PositionKey key7 = new PositionKey("AAPL", "", "Level1", "Level2", "Level3", "Level4", BigDecimal.TEN);

        assert !key1.equals(key7) : "Non-equal objects equal field account";

        PositionKey key8 = new PositionKey("", "UnitTest", "Level1", "Level2", "Level3", "Level4", BigDecimal.TEN);

        assert !key1.equals(key8) : "Non-equal objects equal field securityId";

    }

}
