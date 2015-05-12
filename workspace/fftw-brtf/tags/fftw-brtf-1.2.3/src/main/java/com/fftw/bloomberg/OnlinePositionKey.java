package com.fftw.bloomberg;

import java.math.BigDecimal;

import com.fftw.bloomberg.types.BBProductCode;

/**
 * Represent the key that makes a position unique.
 * 
 * An online position record is unique based on:
 * <ol>
 * <li>securityId</li>
 * <li>account</>
 * <li>level1TagName</li>
 * <li>level2TagName</li>
 * <li>level2TagName</li>
 * <li>level2TagName</li>
 * <li>primeBroker</li>
 * <li>openPosition</li>
 * </ol>
 */
public class OnlinePositionKey extends AbstractPositionKey {

    private final String primeBroker;
    private final BigDecimal openPosition;

    /**
     * The full position key constructor.
     * 
     * @param securityId
     * @param account
     * @param level1TagName
     * @param level2TagName
     * @param level3TagName
     * @param level4TagName
     * @param primeBroker
     * @param openPosition
     */
    public OnlinePositionKey(String securityId, BBProductCode productCode, String account, String level1TagName, String level2TagName,
        String level3TagName, String level4TagName, String primeBroker, BigDecimal openPosition) {
        super(securityId, productCode, account, level1TagName, level2TagName, level3TagName, level4TagName);

        this.primeBroker = primeBroker;
        this.openPosition = openPosition;

    }

    @Override
    public int hashCode() {
        int parentHashCode = super.hashCode();

        int hashCode = 17 * stringHashcode(primeBroker)
            + (openPosition != null ? openPosition.intValue() * 17 : 0);

        return hashCode + parentHashCode;
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
                && stringEquals(primeBroker, other.primeBroker)
                && productCode == other.productCode
                && compareBigDecimal(openPosition, other.openPosition);
        }

        return false;
    }

    private static boolean compareBigDecimal(BigDecimal a, BigDecimal b) {
        if (a != null && b != null) {
            return a.longValue() == b.longValue();
        }

        if (a != null) {
            return a.equals(b);
        }

        return (a == null && b == null);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("securityId=").append(securityId).append(", ");
        sb.append("productCode=").append(productCode).append(", ");
        sb.append("account=").append(account).append(", ");
        sb.append("level1TagName=").append(level1TagName).append(", ");
        sb.append("level2TagName=").append(level2TagName).append(", ");
        sb.append("level3TagName=").append(level3TagName).append(", ");
        sb.append("level4TagName=").append(level4TagName).append(", ");
        sb.append("primeBroker=").append(primeBroker).append(", ");
        sb.append("openPosition=").append(openPosition);

        return sb.toString();
    }

    /**
     * @return the primeBroker
     */
    public String getPrimeBroker() {
        return primeBroker;
    }

    /**
     * @return the openPosition
     */
    public BigDecimal getOpenPosition() {
        return openPosition;
    }

}