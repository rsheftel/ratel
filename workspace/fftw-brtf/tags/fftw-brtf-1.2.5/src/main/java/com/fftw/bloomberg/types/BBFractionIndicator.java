package com.fftw.bloomberg.types;

/**
 * Bloomberg uses a fraction indicator that indicates how the price/quote/etc field is formatted.
 */
public enum BBFractionIndicator {
    Zero('0'), One('1'), Two('2'), Three('3'), Four('4'), Five('5'), Six('6'), Seven('7'),
    Eight('8'), Nine('9'), Half('H'), Quarter('Q'), Eighth('E'), Sixteenth('S'), Thirtysecondth('T'),
    Sixtyfourth('X'), Onetwentyeighth('O'), Twofiftysixth('F'), Half32('U');

    private char indicatorCode;

    private BBFractionIndicator(char code) {
        this.indicatorCode = code;
    }

    public char getCode() {
        return indicatorCode;
    }

    public int getDecimals() {
        if (ordinal() < 10) {
            return ordinal();
        } else {
            throw new UnsupportedOperationException("Cannot return number of decimals for " + this.name());
        }
    }

    /**
     * Return the <code>BBFranctionIndicator</code> represented by the number of decimals.
     *
     * This only supports 0-9.  All other values are illegal.
     *
     * @throws IllegalArgumentException
     * @param code
     * @return
     */
    public static BBFractionIndicator valueOf(int code) {
        if (code >= 0 && code <= 9) {
            return values()[code];
        } else {
            throw new IllegalArgumentException("No type for '" + code + "'");
        }
    }

    public static BBFractionIndicator valueOf(char code) {
        if (code >= '0' && code <= '9') {
            return values()[((int) code) - 48];
        } else {
            switch (code) {
                case 'H':
                    return Half;
                case 'Q':
                    return Quarter;
                case 'E':
                    return Eighth;
                case 'S':
                    return Sixteenth;
                case 'T':
                    return Thirtysecondth;
                case 'X':
                    return Sixtyfourth;
                case 'O':
                    return Onetwentyeighth;
                case 'F':
                    return Twofiftysixth;
                case 'U':
                    return Half32;
                default:
                    throw new IllegalArgumentException("No type for '" + code + "'");
            }
        }
    }
}
