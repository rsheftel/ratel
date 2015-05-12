package com.fftw.bloomberg;

import java.math.BigDecimal;

import com.fftw.bloomberg.types.BBProductCode;

/**
 * Represent the key that makes a position unique.
 * <p/>
 * Currently each line is unique based on:
 * <ol>
 * <li>securityId</li>
 * <li>productCode</li>
 * <li>account</>
 * <li>level1TagName</li>
 * <li>level2TagName</li>
 * <li>level3TagName</li>
 * <li>level4TagName</li>
 * <li>primeBroker</li>
 * <li>onlineOpenPosition</li>
 * </ol>
 */
public class PositionKey extends AbstractPositionKey {

    private String primeBroker;
    private BigDecimal onlineOpenPosition; 

    public PositionKey(String securityId, BBProductCode productCode, String account, String level1TagName, String level2TagName,
        String level3TagName, String level4TagName, String primeBroker, BigDecimal onlineOpenPosition) {
        super(securityId, productCode, account, level1TagName, level2TagName, level3TagName, level4TagName);

        this.primeBroker = primeBroker;
        this.onlineOpenPosition = onlineOpenPosition;
    }
    
    @Override
    public int hashCode() {
        int parentHashCode = super.hashCode();

        int hashCode = 17 * stringHashcode(primeBroker)
            + (onlineOpenPosition != null ? onlineOpenPosition.intValue() * 17 : 0);

        return hashCode + parentHashCode;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof PositionKey) {
            PositionKey other = (PositionKey) obj;
            return securityId.equals(other.securityId) && account.equals(other.account)
                && stringEquals(level1TagName, other.level1TagName)
                && stringEquals(level2TagName, other.level2TagName)
                && stringEquals(level3TagName, other.level3TagName)
                && stringEquals(level4TagName, other.level4TagName)
                && stringEquals(primeBroker, other.primeBroker)
                && productCode == other.productCode
                && compareBigDecimal(onlineOpenPosition, other.onlineOpenPosition);
        }

        return false;
    }

    private static boolean compareBigDecimal(BigDecimal a, BigDecimal b) {
        if (a != null) {
            return (a.compareTo(b) == 0);
        }

        if (b != null) {
            return (b.compareTo(a) == 0);
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
        sb.append("onlineOpenPosition=").append(onlineOpenPosition);

        return sb.toString();
    }

    /**
     * @return the primeBroker
     */
    public String getPrimeBroker() {
        return primeBroker;
    }

    /**
     * @param primeBroker the primeBroker to set
     */
    public void setPrimeBroker(String primeBroker) {
        this.primeBroker = primeBroker;
    }

    /**
     * @return the currentNetPosition
     */
    public BigDecimal getOnlineOpenPosition() {
        return onlineOpenPosition;
    }

    /**
     * @param onlineOpenPosition the currentNetPosition to set
     */
    public void setOnlineOpenPosition(BigDecimal onlineOpenPosition) {
        this.onlineOpenPosition = onlineOpenPosition;
    }
}
