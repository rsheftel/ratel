package com.fftw.bloomberg.types;

/**
 *
 */
public enum BBProductCode {
    Commodity(1), Equity(2), Municipals(3), Preferred(4), Cient(5), MoneyMarket(6), Government(7),
    Corporate(8), Index(9), Currency(10), Mortgage(11);

    private int productCode;

    private BBProductCode(int code) {
        this.productCode = code;
    }

    public int getProductCode() {
        return productCode;
    }

    public static BBProductCode valueOf(int code) {
        return values()[code-1];
    }

}
