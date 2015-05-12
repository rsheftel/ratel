package malbec.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MathUtil {

    private MathUtil() {}
    
    public static BigDecimal truncate(BigDecimal value, BigDecimal smallestIncrement) {
        
        MathContext mc = new MathContext(Math.max(value.precision(), smallestIncrement.precision()),
            RoundingMode.FLOOR);

        BigDecimal howManyTimes = value.divide(smallestIncrement, mc);

        if (!hasFraction(howManyTimes)) {
            return value;
        }

        return smallestIncrement.multiply(BigDecimal.valueOf(howManyTimes.longValue()));
    }
    
    public static BigDecimal truncate(BigDecimal value, int smallestValue) {
        
        return truncate(value, BigDecimal.valueOf(smallestValue));
    }
 
    static boolean hasFraction(BigDecimal decimal) {
        long wholePart = decimal.longValue();
        BigDecimal whole = new BigDecimal(wholePart);

        return (decimal.compareTo(whole) != 0);
    }

    public static BigDecimal normalizeDown(BigDecimal value, BigDecimal multiplier, BigDecimal smallestIncrement) {
        return multiplier.multiply(roundDown(value, smallestIncrement));
    }

    public static BigDecimal normalizeDown(BigDecimal value, BigDecimal multiplier, int oneOver) {
        MathContext mc = new MathContext(Math.max(value.precision(), 8), RoundingMode.HALF_UP);
        BigDecimal smallestIncrement = BigDecimal.ONE.divide(BigDecimal.valueOf(oneOver), mc);
    
        return normalizeDown(value, multiplier, smallestIncrement);
    }

    public static BigDecimal normalizeUp(BigDecimal value, BigDecimal multiplier, BigDecimal smallestIncrement) {
        return multiplier.multiply(roundUp(value, smallestIncrement));
    }

    public static BigDecimal normalizeUp(BigDecimal value, BigDecimal multiplier, int oneOver) {
        MathContext mc = new MathContext(Math.max(value.precision(), 8), RoundingMode.HALF_UP);
        BigDecimal smallestIncrement = BigDecimal.ONE.divide(BigDecimal.valueOf(oneOver), mc);
        
        return normalizeUp(value, multiplier, smallestIncrement);
    }

    /**
     * Round the price down based on the smallest allowed increment.
     * 
     * @param value
     * @param smallestIncrement
     * @return
     */
    public static BigDecimal roundDown(BigDecimal value, BigDecimal smallestIncrement) {
        MathContext mc = new MathContext(Math.max(smallestIncrement.precision(), value.precision()),
            RoundingMode.HALF_DOWN);
    
        BigDecimal howManyTimes = value.divide(smallestIncrement, mc);
    
        if (!hasFraction(howManyTimes)) {
            return value;
        }
    
        return smallestIncrement.multiply(BigDecimal.valueOf(howManyTimes.longValue()));
    }

    /**
     * Round the price down based on the smallest allowed increment represented by 1/x.
     * 
     * @param value
     * @param oneOver
     * @return
     */
    public static BigDecimal roundDown(BigDecimal value, int oneOver) {
        MathContext mc = new MathContext(Math.max(value.precision(), 8), RoundingMode.HALF_DOWN);
        BigDecimal smallestIncrement = BigDecimal.ONE.divide(BigDecimal.valueOf(oneOver), mc);
    
        return roundDown(value, smallestIncrement);
    }

    /**
     * Round the price up using the smallest increment.
     * 
     * @param value
     * @param smallestIncrement
     * @return
     */
    public static BigDecimal roundUp(BigDecimal value, BigDecimal smallestIncrement) {
        MathContext mc = new MathContext(Math.max(value.precision(), smallestIncrement.precision()),
            RoundingMode.HALF_UP);
    
        BigDecimal howManyTimes = value.divide(smallestIncrement, mc);
    
        if (!hasFraction(howManyTimes)) {
            return value;
        }
    
        // increase to the next smallest multiple
        return smallestIncrement.multiply(BigDecimal.valueOf(howManyTimes.longValue() + 1));
    }

    /**
     * Round the price up using the smallest increment based on 1/x.
     * 
     * @param value
     * @param oneOver
     * @return
     */
    public static BigDecimal roundUp(BigDecimal value, int oneOver) {
        MathContext mc = new MathContext(Math.max(value.precision(), 8), RoundingMode.HALF_UP);
        BigDecimal smallestIncrement = BigDecimal.ONE.divide(BigDecimal.valueOf(oneOver), mc);
    
        return roundUp(value, smallestIncrement);
    }
    
}
