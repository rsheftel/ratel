package com.fftw.util;

import java.math.BigDecimal;

/**
 *
 */
public class BigMath {

    private BigMath() {
        // prevent
    }

        /**
     * If the two parameters are not null, add them together.
     *
     * If one of them is null, return the non-null value
     *
     * @param augend
     * @param addend
     * @return
     */
    public static BigDecimal add(BigDecimal augend, BigDecimal addend) {
        if (augend != null && addend != null) {
            return augend.add(addend);
        } else if (augend == null) {
            return addend;
        } else {
            return augend;
        }
    }

    /**
     * Returns the value of the first argument raised to the power of the
     * second argument.
     *
     * @param base
     * @param exponent
     * @return
     */
    public static BigDecimal powerOf(int base, int exponent) {
        long result = 1;
        for (int i=0; i < exponent; i++) {
            result = result * base;
        }

        return BigDecimal.valueOf(result);
    }
}
