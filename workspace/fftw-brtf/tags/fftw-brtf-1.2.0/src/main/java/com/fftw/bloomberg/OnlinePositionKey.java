package com.fftw.bloomberg;

/**
 * Represent the key that makes a position unique.
 * <p/>
 * Currently each line is unique based on:
 * <ol>
 * <li>securityId</li>
 * <li>account</>
 * <li>level1TagName</li>
 * <li>level2TagName</li>
 * <li>level2TagName</li>
 * <li>level2TagName</li>
 * </ol>
 */
public class OnlinePositionKey {

    private final String securityId;
    private final String account;
    private final String level1TagName;
    private final String level2TagName;
    private final String level3TagName;
    private final String level4TagName;
    private final String primeBroker;

    public OnlinePositionKey(String securityId, String account, String level1TagName, String level2TagName,
        String level3TagName, String level4TagName, String primeBroker) {

        this.securityId = securityId;
        this.account = account;
        this.level1TagName = level1TagName;
        this.level2TagName = level2TagName;
        this.level3TagName = level3TagName;
        this.level4TagName = level4TagName;
        this.primeBroker = primeBroker;
    }

    public OnlinePositionKey(String securityId, String account, String level1TagName, String level2TagName,
        String level3TagName, String level4TagName) {
        this(securityId, account, level1TagName, level2TagName, level3TagName, level4TagName, null);
    }

    public OnlinePositionKey(String securityId, String account) {
        this(securityId, account, null, null, null, null);
    }

    @Override
    public int hashCode() {
        int hashCode = securityId.hashCode() + (17 * account.hashCode());
        hashCode = hashCode + (17 * stringHashcode(level1TagName)) + (17 * stringHashcode(level2TagName));
        hashCode = hashCode + (17 * stringHashcode(level3TagName)) + (17 * stringHashcode(level4TagName));
        hashCode = hashCode + (17 * stringHashcode(primeBroker));

        return hashCode;
    }

    private int stringHashcode(String str) {
        if (str != null) {
            return str.hashCode();
        }

        return 27;
    }

    private boolean stringEquals(String a, String b) {
        if (a != null) {
            return a.equals(b);
        }

        return (a == null && b == null);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof OnlinePositionKey) {
            OnlinePositionKey other = (OnlinePositionKey) obj;
            return securityId.equals(other.securityId) && account.equals(other.account)
                && stringEquals(level1TagName, other.level1TagName)
                && stringEquals(level2TagName, other.level2TagName)
                && stringEquals(level3TagName, other.level3TagName)
                && stringEquals(level4TagName, other.level4TagName)
                && stringEquals(primeBroker, other.primeBroker);
        }

        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("securityId=").append(securityId).append(", ");
        sb.append("account=").append(account).append(", ");
        sb.append("level1TagName=").append(level1TagName).append(", ");
        sb.append("level2TagName=").append(level2TagName).append(", ");
        sb.append("level3TagName=").append(level3TagName).append(", ");
        sb.append("level4TagName=").append(level4TagName).append(", ");
        sb.append("primeBroker=").append(primeBroker).append(", ");

        return sb.toString();
    }
}