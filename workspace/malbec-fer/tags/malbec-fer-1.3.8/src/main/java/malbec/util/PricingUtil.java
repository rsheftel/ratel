package malbec.util;

import java.math.BigDecimal;
import java.math.MathContext;

public class PricingUtil {

    private static final BigDecimal THIRTY_TWO = new BigDecimal(32);

    private static final BigDecimal TWO_FIFTY_SIX = new BigDecimal(256);

    // private static final MathContext THIRTY_TWO_CONTEXT = new MathContext(5);
    private static final MathContext CONTEXT = new MathContext(8);

    /**
     * This parses a string that is in bond pricing format.
     * 
     * Bond pricing format is defined as:
     * 
     * <pre>
     * YYY-XXQ
     * 
     * where:
     *  YYY - is the whole number portion
     *  XX - is the 32ndth part of the decimal portion (XX/32)
     *  Q - is the 256th part of the decimal portion (Q/8/32).
     *      when a '+' is in this position, replace it with a '4'
     * </pre>
     * 
     * @param bondPricing
     * @return
     */
    public static BigDecimal parseBondPricing(String bondPricing) {
        // A '+' sign in the decimal portion (last place) is a half -- 4
        String[] bondPricingParts = bondPricing.replaceAll("\\+", "4").split("-");
        BigDecimal wholePart = BigDecimal.valueOf(Long.parseLong(bondPricingParts[0]));

        // ensure we have at least 3 characters
        String decimalPart = bondPricingParts[1] + "000";
        long xxPart = Long.parseLong(decimalPart.substring(0, 2));
        long qPart = Long.parseLong(decimalPart.substring(2, 3));
        BigDecimal xx = BigDecimal.valueOf(xxPart).divide(THIRTY_TWO);
        BigDecimal q = BigDecimal.valueOf(qPart).divide(TWO_FIFTY_SIX);

        return wholePart.add(xx).add(q);
    }

    /**
     * Create a bond price from a decimal value.
     * 
     * Bond pricing format is defined as:
     * 
     * <pre>
     * YYY-XXQ
     * 
     * where:
     *  YYY - is the whole number portion
     *  XX - is the 32ndth part of the decimal portion (XX/32)
     *  Q - is the 256th part of the decimal portion (Q/8/32).
     *      when a '+' is in this position, replace it with a '4'
     * </pre>
     * 
     * The algorithm used is:
     * 
     * <pre>
     * YYY - the whole decimal part of the price
     * XX - the whole decimal part of the fraction * 32 - the remainder is used for Q
     * Q - is the (fraction - XX/32) * 256.  If Q == 4, a '+' is used instead
     * </pre>
     * 
     * @param bondPrice
     * @return
     */
    public static String createBondPricing(BigDecimal bondPrice) {

        BigDecimal wholePart = BigDecimal.valueOf(bondPrice.longValue());
        BigDecimal fractionPart = bondPrice.subtract(wholePart);

        BigDecimal xxPart = fractionPart.multiply(THIRTY_TWO, CONTEXT);
        long wholeXxPart = xxPart.longValue();

        BigDecimal qPartFraction = fractionPart.subtract(BigDecimal.valueOf(wholeXxPart).divide(THIRTY_TWO));
        BigDecimal qPart = qPartFraction.multiply(TWO_FIFTY_SIX, CONTEXT);
        long wholeQPart = qPart.longValue();

        StringBuilder sb = new StringBuilder();
        sb.append(wholePart).append("-").append(wholeXxPart);

        if (wholeQPart > 0) {
            if (wholeQPart == 4) {
                sb.append("+");
            } else {
                sb.append(wholeQPart);
            }
        }

        return sb.toString();
    }

    /**
     * Checks whether this bond price represent an exact bond price.
     * 
     * @param bondPrice
     * @return
     */
    public static boolean isExactBondPricing(BigDecimal bondPrice) {
        BigDecimal wholePart = BigDecimal.valueOf(bondPrice.longValue());
        BigDecimal fractionPart = bondPrice.subtract(wholePart);

        BigDecimal xxPart = fractionPart.multiply(THIRTY_TWO, CONTEXT);
        long wholeXxPart = xxPart.longValue();

        BigDecimal qPartFraction = fractionPart.subtract(BigDecimal.valueOf(wholeXxPart).divide(THIRTY_TWO));
        BigDecimal qPart = qPartFraction.multiply(TWO_FIFTY_SIX, CONTEXT);

        return !MathUtil.hasFraction(qPart);
    }

}
