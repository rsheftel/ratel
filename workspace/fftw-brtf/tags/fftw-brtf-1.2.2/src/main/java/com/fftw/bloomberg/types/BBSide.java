package com.fftw.bloomberg.types;

/**
 *
 */
public enum BBSide {
    Buy('B'), Sell('S');

    private char sideCode;

    private BBSide(char code) {
        this.sideCode = code;
    }

    public static BBSide valueOf(char code) {
        if (code == 'B') {
            return Buy;
        } else if (code == 'S') {
            return Sell;
        } else {
            throw new IllegalArgumentException("No side defined for '"+ code +"'");
        }
    }
    
}
