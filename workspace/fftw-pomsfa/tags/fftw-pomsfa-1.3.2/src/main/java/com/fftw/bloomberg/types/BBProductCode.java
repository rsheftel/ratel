package com.fftw.bloomberg.types;

/**
 *
 */
public enum BBProductCode {
    Commodity(1), Equity(2), Municipals(3), Preferred(4), Cient(5), MoneyMarket(6), Government(7),
    Corporate(8), Index(9), Currency(10), Mortgage(11);

    private int id;

    private BBProductCode(int code) {
        this.id = code;
    }

    public int getID() {
        return id;
    }

    public static BBProductCode valueOf(int code) {
        return values()[code-1];
    }

}
