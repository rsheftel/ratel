package malbec.util;

import static org.testng.Assert.*;

import java.math.BigDecimal;

import malbec.AbstractBaseTest;

import org.testng.annotations.Test;

public class MathUtilTest extends AbstractBaseTest {

    private BigDecimal bd14 = new BigDecimal("117.90434696596057");
    
    @Test(groups = { "unittest" })
    public void testTruncate() {

        BigDecimal truncated_1 = MathUtil.truncate(new BigDecimal("7550"), 100);
        assertNotNull(truncated_1);
        assertEqualsBD(truncated_1, new BigDecimal("7500"));

        BigDecimal truncated_2 = MathUtil.truncate(new BigDecimal("7551"), 100);
        assertNotNull(truncated_2);
        assertEqualsBD(truncated_2, new BigDecimal("7500"));

        BigDecimal truncated_3 = MathUtil.truncate(new BigDecimal("7549"), 100);
        assertNotNull(truncated_3);
        assertEqualsBD(truncated_3, new BigDecimal("7500"));

        BigDecimal truncated_4 = MathUtil.truncate(new BigDecimal("7599"), 100);
        assertNotNull(truncated_4);
        assertEqualsBD(truncated_4, new BigDecimal("7500"));
        
        BigDecimal truncated_5 = MathUtil.truncate(new BigDecimal("99"), 100);
        assertNotNull(truncated_5);
        assertEqualsBD(truncated_5, new BigDecimal("0"));

        BigDecimal truncated_6 = MathUtil.truncate(new BigDecimal("101"), 100);
        assertNotNull(truncated_6);
        assertEqualsBD(truncated_6, new BigDecimal("100"));

    }

    @Test(groups = { "unittest" })
    public void testRoundingDown256() {
    
        // Do some positive values
        BigDecimal rounded256_0 = MathUtil.roundDown(new BigDecimal("132.5625"), 256);
        assertNotNull(rounded256_0);
        assertEqualsBD(rounded256_0, new BigDecimal("132.5625"));
    
        BigDecimal rounded256_1 = MathUtil.roundDown(new BigDecimal("132.5703125"), 256);
        assertNotNull(rounded256_1);
        assertEqualsBD(rounded256_1, new BigDecimal("132.5703125"));
    
        BigDecimal rounded256_2 = MathUtil.roundDown(new BigDecimal("132.578125"), 256);
        assertNotNull(rounded256_2);
        assertEqualsBD(rounded256_2, new BigDecimal("132.578125"));
    
        BigDecimal rounded256_3 = MathUtil.roundDown(new BigDecimal("117.90234375"), 256);
        assertNotNull(rounded256_3);
        assertEqualsBD(rounded256_3, new BigDecimal("117.90234375"));
    
        // These values will be rounded down
        BigDecimal rounded256_4 = MathUtil.roundDown(new BigDecimal("117.90234376"), 256);
        assertNotNull(rounded256_4);
        assertEqualsBD(rounded256_4, new BigDecimal("117.90234375"));
    
        // 0.921875
        // 0.90625 - 0.91015625
        BigDecimal rounded256_5 = MathUtil.roundDown(new BigDecimal("117.91015624"), 256);
        assertNotNull(rounded256_5);
        assertEqualsBD(rounded256_5, new BigDecimal("117.90625"));
    
        BigDecimal rounded256_6 = MathUtil.roundDown(bd14, 256);
        assertNotNull(rounded256_6);
        assertEqualsBD(rounded256_6, new BigDecimal("117.90234375"));
    
        BigDecimal normalDown = MathUtil.normalizeDown(new BigDecimal("117.91015624"), BigDecimal.TEN, 256);
        assertNotNull(normalDown);
        assertEqualsBD(normalDown, new BigDecimal("1179.06250"));
    
        BigDecimal normalDown2 = MathUtil.normalizeDown(new BigDecimal("88.6"), new BigDecimal("0.01"), new BigDecimal("0.01"));
        assertNotNull(normalDown2);
        assertEqualsBD(normalDown2, new BigDecimal("0.8860"));
    }

    @Test(groups = { "unittest" })
    public void testRoundingMultiplier() {
        BigDecimal b4Multiple1 = new BigDecimal("132.5703125");
        BigDecimal multiplied1 = b4Multiple1.multiply(BigDecimal.TEN);
    
        BigDecimal rounded256_1 = MathUtil.roundUp(multiplied1, 256);
        assertNotNull(rounded256_1);
        assertEqualsBD(rounded256_1, new BigDecimal("1325.703125"));
    
        BigDecimal b4Multiple2 = new BigDecimal("132.5703125");
    
        BigDecimal rounded256_2 = MathUtil.roundUp(b4Multiple2, 256);
        assertNotNull(rounded256_2);
        BigDecimal multiplied2 = rounded256_2.multiply(BigDecimal.TEN);
        assertEqualsBD(multiplied2, new BigDecimal("1325.703125"));
    }

    @Test(groups = { "unittest" })
    public void testRoundingUp256() {
    
        // Do some positive values
        BigDecimal rounded256_0 = MathUtil.roundUp(new BigDecimal("132.5625"), 256);
        assertNotNull(rounded256_0);
        assertEqualsBD(rounded256_0, new BigDecimal("132.5625"));
    
        BigDecimal rounded256_1 = MathUtil.roundUp(new BigDecimal("132.5703125"), 256);
        assertNotNull(rounded256_1);
        assertEqualsBD(rounded256_1, new BigDecimal("132.5703125"));
    
        BigDecimal rounded256_2 = MathUtil.roundUp(new BigDecimal("132.578125"), 256);
        assertNotNull(rounded256_2);
        assertEqualsBD(rounded256_2, new BigDecimal("132.578125"));
    
        BigDecimal rounded256_3 = MathUtil.roundUp(new BigDecimal("117.90234375"), 256);
        assertNotNull(rounded256_3);
        assertEqualsBD(rounded256_3, new BigDecimal("117.90234375"));
    
        // These values will be rounded up
        BigDecimal rounded256_4 = MathUtil.roundUp(new BigDecimal("117.90234376"), 256);
        assertNotNull(rounded256_4);
        assertEqualsBD(rounded256_4, new BigDecimal("117.90625"));
    
        // 0.921875
        // 0.90625 - 0.91015625
        BigDecimal rounded256_5 = MathUtil.roundUp(new BigDecimal("117.91015624"), 256);
        assertNotNull(rounded256_5);
        assertEqualsBD(rounded256_5, new BigDecimal("117.91015625"));
    
        BigDecimal rounded256_6 = MathUtil.roundUp(bd14, 256);
        assertNotNull(rounded256_6);
        assertEqualsBD(rounded256_6, new BigDecimal("117.90625"));
    
        BigDecimal normalUp = MathUtil.normalizeUp(new BigDecimal("117.91015624"), BigDecimal.TEN, 256);
        assertNotNull(normalUp);
        assertEqualsBD(normalUp, new BigDecimal("1179.10156250")); 
    
        BigDecimal normalUp2 = MathUtil.normalizeUp(new BigDecimal("88.6"), new BigDecimal("0.01"), new BigDecimal("0.01"));
        assertNotNull(normalUp2);
        assertEqualsBD(normalUp2, new BigDecimal("0.8860"));
    }

}
