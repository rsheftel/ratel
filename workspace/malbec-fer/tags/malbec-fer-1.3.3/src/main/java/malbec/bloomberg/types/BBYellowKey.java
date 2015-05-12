package malbec.bloomberg.types;

/**
 * Represent the Bloomberg Yellow Key (AKA Product code).
 * 
 */
public enum BBYellowKey {
    Unknown(0), Comdty(1, "Commodity", "CMDT"), Equity(2, "Equity", "EQTY"), Muni(3, "Municipals", "MUNI"), 
    Pfd(4, "Preferred", "PRFD"), Cient(5, "Client", "CLNT"), MMkt(6, "MoneyMarket", "MMKT"), 
    Govt(7, "Government", "GOVT"), Corp(8, "Corporate", "CORP"), Index(9, "Index", "INDX"), 
    Curncy(10, "Currency", "CURR"), Mtge(11, "Mortgage", "MTGE");

    private int code;
    private String longName;
    private String shortName;

    private BBYellowKey(int code) {
        this.code = code;
        this.longName = name();
        this.shortName = name();
    }

    private BBYellowKey(int code, String longName, String shortName) {
        this.code = code;
        this.longName = longName;
        this.shortName = shortName;
    }

    public int getCode() {
        return code;
    }

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }
    
    /**
     * Determine the yellow key based on the numeric code.
     * 
     * 0 is 
     * @param code
     * @return
     */
    public static BBYellowKey valueOf(int code) {
        BBYellowKey[] localValues = values();
        
        if (localValues.length <= code || code < 0) {
            return Unknown;
        }
        
        return localValues[code];
    }

    /**
     * Determine the yellow key based on the enum name ignoring case and 
     * returning Unknown for anything that does not match.
     * 
     * @param text
     * @return
     */
    public static BBYellowKey valueFor(String text) {
        for (BBYellowKey yk : BBYellowKey.values()) {
            if (yk.name().equalsIgnoreCase(text)) {
                return yk;
            }
        }
        return BBYellowKey.Unknown;
    }

    public static BBYellowKey fromCmf(String cmfCode) {
        for (BBYellowKey yk : BBYellowKey.values()) {
            if (yk.shortName.equalsIgnoreCase(cmfCode)) {
                return yk;
            }
        }
        return BBYellowKey.Unknown;
    }
    
    public static BBYellowKey fromLongName(String longName) {
        for (BBYellowKey yk : BBYellowKey.values()) {
            if (yk.longName.equalsIgnoreCase(longName)) {
                return yk;
            }
        }
        return BBYellowKey.Unknown;
    }

}
