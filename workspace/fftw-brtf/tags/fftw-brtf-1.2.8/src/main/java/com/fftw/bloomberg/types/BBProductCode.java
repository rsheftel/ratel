package com.fftw.bloomberg.types;

/**
 *
 */
public enum BBProductCode {
    Commodity(1, "COMDTY"), Equity(2, "EQUITY"), Municipals(3, "MUNI"), Preferred(4, "PFD"), 
    Cient(5, "CLIENT"), MoneyMarket(6, "M-MKT"), Government(7, "GOVT"), Corporate(8, "CORP"), 
    Index(9, "INDEX"), Currency(10, "CURNCY"), Mortgage(11, "MTGE"), Unknown(-1, "UNKWN");

    private int productCode;
    private String shortString;

    private BBProductCode(int code, String shortString) {
        this.productCode = code;
        this.shortString = shortString;
    }
    
    private BBProductCode(int code) {
        this.productCode = code;
        this.shortString = name();
    }

    public int getProductCode() {
        return productCode;
    }

    public String getShortString() {
        return shortString;
    }
    
    public static BBProductCode valueOf(int code) {
        try {
            return values()[code-1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return Unknown;
        }
    }

}
