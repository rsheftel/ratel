package com.fftw.bloomberg.types;

/**
 * Bloomberg does not have a Unknown (0) code, but we have been using 0 as unknown, and we cannot 
 * change now.
 */
public enum BBProductCode {
    Uknown(0), Commodity(1), Equity(2), Municipals(3), Preferred(4), Cient(5), MoneyMarket(6), Government(7),
    Corporate(8), Index(9), Currency(10), Mortgage(11);

    private int id;

    private BBProductCode(int code) {
        this.id = code;
    }

    public int getID() {
        return id;
    }

    public static BBProductCode valueOf(int code) {
        return values()[code];
    }

}
