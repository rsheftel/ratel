package malbec.bloomberg.types;

/**
 * Represent the Bloomberg Yellow Key (AKA Product code).
 * 
 */
public enum BBYellowKey {
    Unknown(0), Comdty(1, "Commodity"), Equity(2), Muni(3, "Municipals"), 
    Pfd(4,"Preferred"), Cient(5), MMkt(6, "MoneyMarket"), Govt(7, "Government"), 
    Corp(8, "Corporate"), Index(9), Curncy(10, "Currency"), Mtge(11, "Mortgage");

    private int productCode;
    private String longName;

    private BBYellowKey(int code) {
        this.productCode = code;
        this.longName = name();
    }

    private BBYellowKey(int code, String longName) {
        this.productCode = code;
        this.longName = longName;
    }

    public int getProductCode() {
        return productCode;
    }

    public String getLongName() {
        return longName;
    }

    public static BBYellowKey valueOf(int code) {
        return values()[code - 1];
    }

    public static BBYellowKey valueFor(String text) {
        try {
            return valueOf(text);
        } catch (IllegalArgumentException e) {
            return Unknown;
        }
    }
}
