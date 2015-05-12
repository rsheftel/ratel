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
}
