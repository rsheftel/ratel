package malbec.fix;

/**
 * The version of FIX.
 */
public enum FixVersion {

    F40("FIX.4.0"), F41("FIX.4.1"), F42("FIX.4.2"), F43("FIX.4.3"), F44("FIX.4.4"),
    Unknown("FIX.X.X");
    
    private String text;
    
    private FixVersion(String version) {
        text = version;
    }
    
    public String toString() {
        return text;
    }
    
    public static FixVersion fromString(String beginString) {
        
        for (FixVersion version : values()) {
            if (version.text.equals(beginString)) {
                return version;
            }
        }
        
        return Unknown;
    }
}
