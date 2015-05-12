package com.fftw.bloomberg;

import com.fftw.bloomberg.types.BBProductCode;

/**
 * Base class for position records.
 * 
 * <ol>
 * <li>securityId</li> required
 * <li>account</> required
 * <li>level1TagName</li>
 * <li>level2TagName</li>
 * <li>level3TagName</li>
 * <li>level4TagName</li>
 * </ol>
 */
public abstract class AbstractPositionKey {

    protected final String securityId;
    protected final BBProductCode productCode;
    protected final String account;
    protected final String level1TagName;
    protected final String level2TagName;
    protected final String level3TagName;
    protected final String level4TagName;

    public AbstractPositionKey(String securityId, BBProductCode productCode, String account, String level1TagName, String level2TagName,
        String level3TagName, String level4TagName) {

        if (securityId == null) {
            throw new IllegalArgumentException("securityId cannot be null");
        }

        if (account == null) {
            throw new IllegalArgumentException("account cannot be null");
        }

        this.securityId = securityId;
        this.productCode = productCode;
        this.account = account;
        this.level1TagName = level1TagName;
        this.level2TagName = level2TagName;
        this.level3TagName = level3TagName;
        this.level4TagName = level4TagName;
    }

    public String getSecurityId() {
        return securityId;
    }

    public BBProductCode getProductCode() {
        return productCode;
    }
    
    public String getAccount() {
        return account;
    }

    public String getLevel1TagName() {
        return level1TagName;
    }

    public String getLevel2TagName() {
        return level2TagName;
    }

    public String getLevel3TagName() {
        return level3TagName;
    }

    public String getLevel4TagName() {
        return level4TagName;
    }

    @Override
    public int hashCode() {
        int hashCode = securityId.hashCode() + (17 * account.hashCode());
        hashCode = hashCode + (17 * stringHashcode(level1TagName)) + (17 * stringHashcode(level2TagName));
        hashCode = hashCode + (17 * stringHashcode(level3TagName)) + (17 * stringHashcode(level4TagName));
        hashCode = hashCode + (17 * productCode.hashCode());

        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof AbstractPositionKey) {
            AbstractPositionKey other = (AbstractPositionKey) obj;
            return securityId.equals(other.securityId) && account.equals(other.account)
                && stringEquals(level1TagName, other.level1TagName)
                && stringEquals(level2TagName, other.level2TagName)
                && stringEquals(level3TagName, other.level3TagName)
                && stringEquals(level4TagName, other.level4TagName)
                && productCode == other.productCode;
        }

        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(512);

        sb.append("securityId=").append(securityId).append(", ");
        sb.append("productCode=").append(productCode).append(", ");
        sb.append("account=").append(account).append(", ");

        sb.append("level1TagName=").append(level1TagName).append(", ");
        sb.append("level2TagName=").append(level1TagName).append(", ");
        sb.append("level3TagName=").append(level3TagName).append(", ");
        sb.append("level4TagName=").append(level4TagName).append(", ");

        return sb.toString();
    }

    protected int stringHashcode(String str) {
        if (str != null) {
            return str.hashCode();
        }

        return 0;
    }

    protected boolean stringEquals(String a, String b) {
        if (a != null) {
            return a.equals(b);
        }

        return (a == null && b == null);
    }

}
