package com.fftw.bloomberg.util;

import java.math.BigDecimal;
import java.math.MathContext;

public class FixedWidthFormatter
{

    private FixedWidthFormatter ()
    {
        // prevent
    }

    /**
     * Format a number using leading zeros (0) right justified of the specified
     * length.
     * 
     * @param value
     *            number to be formatted
     * @param length
     *            final length including leading zeros
     * @return
     */
    public static String formatNumber (int value, int length)
    {

        StringBuilder sb = new StringBuilder(10);
        sb.append("%0").append(length).append("d");

        return String.format(sb.toString(), value).substring(0, length);
    }

    /**
     * Format a number using leading zeros (0) right justified of the specified
     * length accounting for the specified number of decimal places.
     * 
     * The returned string will be a total length of 'length + decimal'.
     * 
     * If the specified value results in a string that is longer than the
     * total_length the string will be truncated.
     * 
     * <code>
     *   // N4.2
     *   formatNumber(123.456, 4, 2);
     *   // result will be '012345'
     * </code>
     * 
     * We use <code>BigDecimal</code> with the specified precision to ensure that we
     * do not have any binary rounding errors when moving the decimal.
     * 
     * @param value
     * @param wholeLength
     *            number of whole numbers to display
     * @param precision
     *            how many decimal place to display
     * @return
     */
    public static String formatNumber (double value, int wholeLength, int precision)
    {
        //MathContext mc = new MathContext(precision);
        MathContext mc = new MathContext(wholeLength + precision);
        BigDecimal bd = new BigDecimal(value, mc);
        
        StringBuilder sb = new StringBuilder(10);
        sb.append("%0").append(wholeLength + precision).append("d");

        bd = bd.movePointRight(precision);
        
        return String.format(sb.toString(), bd.longValue()).substring(0, wholeLength + precision);
    }

    public static String formatNumber(BigDecimal value, int wholeLength, int precision){
//        MathContext mc = new MathContext(precision);
        MathContext mc = new MathContext(wholeLength + precision);
        BigDecimal bd =  BigDecimal.ZERO.add(value, mc);
        
        StringBuilder sb = new StringBuilder(10);
        sb.append("%0").append(wholeLength + precision).append("d");

        bd = bd.movePointRight(precision);
        
        return String.format(sb.toString(), bd.longValue()).substring(0, wholeLength + precision);
    }
    
    /**
     * Parse a number that has leading zeros and an implied decimal place.
     * 
     * <code>
     *   // N4.2
     *   parseNumber("12398", 2);
     *   // result 123.98
     * </code>
     * 
     * @param value number with implied decimal place
     * @param decimal how many decimals
     * @return
     */
    public static double parseNumber(String value, int decimal) {
        MathContext mc = new MathContext(value.length());
        BigDecimal bd = new BigDecimal(value, mc);

        bd = bd.movePointLeft(decimal);
        
        return bd.doubleValue();
    }
    
    public static String formatString (String value, int length)
    {
        StringBuilder sb = new StringBuilder(length);
        sb.append(value == null ? "" : value);
        
        // Fill the rest of the string with spaces.
        int strLength = sb.length();
        for (int i = strLength; i < length; i++)
        {
            sb.append(" ");
        }

        // Ensure we do not return a bigger string
        sb.setLength(length);
        return sb.toString();
    }

}
