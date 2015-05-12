package com.fftw.bloomberg;

/**
 * Represent the key that makes a position unique.
 * <p/>
 * Currently each line is unique based on:
 * <ol><li>securityId</li>
 * <li>account</>
 * <li>level1TagName</li>
 * <li>level2TagName</li>
 * <li>level2TagName</li>
 * <li>level2TagName</li>
 * </ol>
 */
public class PositionKey {

    private final String securityId;
    private final String account;
    private final String level1TagName;
    private final String level2TagName;
    private final String level3TagName;
    private final String level4TagName;


    private PositionKey() {
        this("","","","","",""); // fulfill the 'final' requirement
    }

    public PositionKey(String securityId, String account, String level1TagName, String level2TagName,
                       String level3TagName, String level4TagName) {
        
        this.securityId = securityId;
        this.account = account;
        this.level1TagName = level1TagName;
        this.level2TagName = level2TagName;
        this.level3TagName = level3TagName;
        this.level4TagName = level4TagName;
    }

    @Override
    public int hashCode() {
        int hashCode = securityId.hashCode() + (17 * account.hashCode());
        hashCode = hashCode + (17 * level1TagName.hashCode()) + (17 * level2TagName.hashCode());
        hashCode = hashCode + (17 * level3TagName.hashCode()) + (17 * level4TagName.hashCode());

        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof PositionKey) {
            PositionKey other = (PositionKey) obj;
            return securityId.equals(other.securityId) && account.equals(other.account)
                    && level1TagName.equals(other.level1TagName) && level2TagName.equals(other.level2TagName)
                    && level3TagName.equals(other.level3TagName) && level4TagName.equals(other.level4TagName);
        }

        return false;
    }
}
