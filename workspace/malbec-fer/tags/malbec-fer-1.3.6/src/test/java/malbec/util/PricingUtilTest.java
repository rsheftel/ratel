package malbec.util;

import static org.testng.Assert.*;

import java.math.BigDecimal;

import malbec.AbstractBaseTest;

import org.testng.annotations.Test;

public class PricingUtilTest extends AbstractBaseTest {

    private BigDecimal bd14 = new BigDecimal("117.90434696596057");

    @Test(groups = { "unittest" })
    public void testBondPriceToDecimal() {
        String bondPrice = "132-18";

        BigDecimal bondDecimalPrice = PricingUtil.parseBondPricing(bondPrice);

        assertNotNull(bondDecimalPrice);
        assertEqualsBD(bondDecimalPrice, new BigDecimal("132.5625"));

        BigDecimal bondDecimalPrice2 = PricingUtil.parseBondPricing("132-182");

        assertNotNull(bondDecimalPrice2);
        assertEqualsBD(bondDecimalPrice2, new BigDecimal("132.5703125"));

        BigDecimal bondDecimalPrice3 = PricingUtil.parseBondPricing("132-18+");

        assertNotNull(bondDecimalPrice3);
        assertEqualsBD(bondDecimalPrice3, new BigDecimal("132.578125"));

        BigDecimal bondDecimalPrice4 = PricingUtil.parseBondPricing("117-287");

        assertNotNull(bondDecimalPrice4);
        assertEqualsBD(bondDecimalPrice4, new BigDecimal("117.90234375"));

    }

    @Test(groups = { "unittest" })
    public void testDecimalToBondPrice() {
        String bondPrice = PricingUtil.createBondPricing(new BigDecimal("132.5625"));
        assertNotNull(bondPrice);
        assertEquals(bondPrice, "132-18");
        assertTrue(PricingUtil.isExactBondPricing(new BigDecimal("132.5625")));

        String bondPrice2 = PricingUtil.createBondPricing(new BigDecimal("132.5703125"));
        assertNotNull(bondPrice2);
        assertEquals(bondPrice2, "132-182");

        String bondPrice3 = PricingUtil.createBondPricing(new BigDecimal("132.578125"));
        assertNotNull(bondPrice3);
        assertEquals(bondPrice3, "132-18+");

        // new BigDecimal("117.90234375")
        String bondPrice4 = PricingUtil.createBondPricing(bd14);
        assertNotNull(bondPrice4);
        assertEquals(bondPrice4, "117-287");
        assertFalse(PricingUtil.isExactBondPricing(bd14));

        String bondPrice5 = PricingUtil.createBondPricing(new BigDecimal("117.904347"));
        assertNotNull(bondPrice5);
        assertEquals(bondPrice5, "117-287");
        assertFalse(PricingUtil.isExactBondPricing(new BigDecimal("117.904347")));
    }

    @Test(groups = { "unittest" })
    public void testRoundingDown256() {

        // Do some positive values
        BigDecimal rounded256_0 = PricingUtil.roundDown(new BigDecimal("132.5625"), 256);
        assertNotNull(rounded256_0);
        assertEqualsBD(rounded256_0, new BigDecimal("132.5625"));

        BigDecimal rounded256_1 = PricingUtil.roundDown(new BigDecimal("132.5703125"), 256);
        assertNotNull(rounded256_1);
        assertEqualsBD(rounded256_1, new BigDecimal("132.5703125"));

        BigDecimal rounded256_2 = PricingUtil.roundDown(new BigDecimal("132.578125"), 256);
        assertNotNull(rounded256_2);
        assertEqualsBD(rounded256_2, new BigDecimal("132.578125"));

        BigDecimal rounded256_3 = PricingUtil.roundDown(new BigDecimal("117.90234375"), 256);
        assertNotNull(rounded256_3);
        assertEqualsBD(rounded256_3, new BigDecimal("117.90234375"));

        // These values will be rounded down
        BigDecimal rounded256_4 = PricingUtil.roundDown(new BigDecimal("117.90234376"), 256);
        assertNotNull(rounded256_4);
        assertEqualsBD(rounded256_4, new BigDecimal("117.90234375"));

        // 0.921875
        // 0.90625 - 0.91015625
        BigDecimal rounded256_5 = PricingUtil.roundDown(new BigDecimal("117.91015624"), 256);
        assertNotNull(rounded256_5);
        assertEqualsBD(rounded256_5, new BigDecimal("117.90625"));

        BigDecimal rounded256_6 = PricingUtil.roundDown(bd14, 256);
        assertNotNull(rounded256_6);
        assertEqualsBD(rounded256_6, new BigDecimal("117.90234375"));

        BigDecimal normalDown = PricingUtil.normalizeDown(new BigDecimal("117.91015624"), BigDecimal.TEN, 256);
        assertNotNull(normalDown);
        assertEqualsBD(normalDown, new BigDecimal("1179.06250"));

        BigDecimal normalDown2 = PricingUtil.normalizeDown(new BigDecimal("88.6"), new BigDecimal("0.01"), new BigDecimal("0.01"));
        assertNotNull(normalDown2);
        assertEqualsBD(normalDown2, new BigDecimal("0.8860"));
    }

    @Test(groups = { "unittest" })
    public void testRoundingUp256() {

        // Do some positive values
        BigDecimal rounded256_0 = PricingUtil.roundUp(new BigDecimal("132.5625"), 256);
        assertNotNull(rounded256_0);
        assertEqualsBD(rounded256_0, new BigDecimal("132.5625"));

        BigDecimal rounded256_1 = PricingUtil.roundUp(new BigDecimal("132.5703125"), 256);
        assertNotNull(rounded256_1);
        assertEqualsBD(rounded256_1, new BigDecimal("132.5703125"));

        BigDecimal rounded256_2 = PricingUtil.roundUp(new BigDecimal("132.578125"), 256);
        assertNotNull(rounded256_2);
        assertEqualsBD(rounded256_2, new BigDecimal("132.578125"));

        BigDecimal rounded256_3 = PricingUtil.roundUp(new BigDecimal("117.90234375"), 256);
        assertNotNull(rounded256_3);
        assertEqualsBD(rounded256_3, new BigDecimal("117.90234375"));

        // These values will be rounded up
        BigDecimal rounded256_4 = PricingUtil.roundUp(new BigDecimal("117.90234376"), 256);
        assertNotNull(rounded256_4);
        assertEqualsBD(rounded256_4, new BigDecimal("117.90625"));

        // 0.921875
        // 0.90625 - 0.91015625
        BigDecimal rounded256_5 = PricingUtil.roundUp(new BigDecimal("117.91015624"), 256);
        assertNotNull(rounded256_5);
        assertEqualsBD(rounded256_5, new BigDecimal("117.91015625"));

        BigDecimal rounded256_6 = PricingUtil.roundUp(bd14, 256);
        assertNotNull(rounded256_6);
        assertEqualsBD(rounded256_6, new BigDecimal("117.90625"));

        BigDecimal normalUp = PricingUtil.normalizeUp(new BigDecimal("117.91015624"), BigDecimal.TEN, 256);
        assertNotNull(normalUp);
        assertEqualsBD(normalUp, new BigDecimal("1179.10156250")); 

        BigDecimal normalUp2 = PricingUtil.normalizeUp(new BigDecimal("88.6"), new BigDecimal("0.01"), new BigDecimal("0.01"));
        assertNotNull(normalUp2);
        assertEqualsBD(normalUp2, new BigDecimal("0.8860"));
    }

    @Test(groups = { "unittest" })
    public void testRoundingMultiplier() {
        BigDecimal b4Multiple1 = new BigDecimal("132.5703125");
        BigDecimal multiplied1 = b4Multiple1.multiply(BigDecimal.TEN);

        BigDecimal rounded256_1 = PricingUtil.roundUp(multiplied1, 256);
        assertNotNull(rounded256_1);
        assertEqualsBD(rounded256_1, new BigDecimal("1325.703125"));

        BigDecimal b4Multiple2 = new BigDecimal("132.5703125");

        BigDecimal rounded256_2 = PricingUtil.roundUp(b4Multiple2, 256);
        assertNotNull(rounded256_2);
        BigDecimal multiplied2 = rounded256_2.multiply(BigDecimal.TEN);
        assertEqualsBD(multiplied2, new BigDecimal("1325.703125"));
    }
}
