package com.fftw.positions;

import static malbec.util.StringUtils.emptyStringOrValue;

import java.util.Map;

import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.BBSecurityType;

public class DefaultSecurity implements ISecurity {

    private String name;
    private BBProductCode productCode;
    private String securityId;
    private BBSecurityIDFlag securityIdFlag;
    private BBSecurityType securityType2;
    private String ticker;

    public DefaultSecurity(String name, BBProductCode productCode, String securityId,
        BBSecurityIDFlag securityIdFlag, BBSecurityType securityType2, String ticker) {
        super();
        this.name = name;
        this.productCode = productCode;
        this.securityId = securityId;
        this.securityIdFlag = securityIdFlag;
        this.securityType2 = securityType2;
        this.ticker = ticker;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BBProductCode getProductCode() {
        return productCode;
    }

    @Override
    public String getSecurityId() {
        return securityId;
    }

    @Override
    public BBSecurityIDFlag getSecurityIdFlag() {
        return securityIdFlag;
    }

    @Override
    public BBSecurityType getSecurityType2() {
        return securityType2;
    }

    @Override
    public String getTicker() {
        return ticker;
    }

    @Override
    public ISecurity copy() {
        DefaultSecurity copy = new DefaultSecurity(name, productCode, securityId, securityIdFlag,
            securityType2, ticker);

        return copy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultSecurity) {
            DefaultSecurity other = (DefaultSecurity) obj;

            return (areEqual(name, other.name) && areEqual(productCode, other.productCode)
                && areEqual(securityId, other.securityId) && areEqual(securityIdFlag, other.securityIdFlag)
                && areEqual(securityType2, other.securityType2) && areEqual(ticker, other.ticker));
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return hashCode(name) + 17 * hashCode(productCode) + 17 * hashCode(securityId) + 17
            * hashCode(securityIdFlag) + 17 * hashCode(securityType2) + 17 * hashCode(ticker);
    }

    protected static boolean areEqual(Object a, Object b) {
        if (a == b) {
            return true;
        }

        if ((a == null && b != null) || (a != null && b == null)) {
            return false;
        }

        return a.equals(b);
    }

    protected static int hashCode(Object o) {
        if (o == null) {
            return 17;
        }

        return o.hashCode();
    }

    @Override
    public ISecurity combineWith(ISecurity other) {
        if (!(other instanceof DefaultSecurity)) {
            return null;
        }

        String name = areEqual(getName(), other.getName()) ? getName() : null;
        BBProductCode productCode = areEqual(getProductCode(), other.getProductCode()) ? getProductCode()
            : null;
        String securityId = areEqual(getSecurityId(), other.getSecurityId()) ? getSecurityId() : null;
        BBSecurityIDFlag flag = areEqual(getSecurityIdFlag(), other.getSecurityIdFlag()) ? getSecurityIdFlag()
            : null;
        BBSecurityType type = areEqual(getSecurityType2(), other.getSecurityType2()) ? getSecurityType2()
            : null;
        String ticker = areEqual(getTicker(), other.getTicker()) ? getTicker() : null;

        return new DefaultSecurity(name, productCode, securityId, flag, type, ticker);
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param productCode
     *            the productCode to set
     */
    public void setProductCode(BBProductCode productCode) {
        this.productCode = productCode;
    }

    /**
     * @param securityId
     *            the securityId to set
     */
    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    /**
     * @param securityIdFlag
     *            the securityIdFlag to set
     */
    public void setSecurityIdFlag(BBSecurityIDFlag securityIdFlag) {
        this.securityIdFlag = securityIdFlag;
    }

    /**
     * @param securityType2
     *            the securityType2 to set
     */
    public void setSecurityType2(BBSecurityType securityType2) {
        this.securityType2 = securityType2;
    }

    /**
     * @param ticker
     *            the ticker to set
     */
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String toString() {
        return toTextMessage(", ");
    }

    @Override
    public String toTextMessage(String delimiter) {
        StringBuilder sb = new StringBuilder(128);

        sb.append("name=").append(emptyStringOrValue(name));
        sb.append(delimiter).append("productCode=").append(productCode.getProductCode());
        sb.append(delimiter).append("yellowKey=").append(productCode.getShortString());
        sb.append(delimiter).append("productCodeText=").append(productCode.toString());
        sb.append(delimiter).append("securityId=").append(getSecurityId());
        sb.append(delimiter).append("securityIdFlag=").append(securityIdFlag);
        sb.append(delimiter).append("securityType2=").append(emptyStringOrValue(securityType2));
        sb.append(delimiter).append("ticker=").append(emptyStringOrValue(getTicker()));

        return sb.toString();
    }

    @Override
    public ISecurity copy(Map<String, Object> newValues) {

        DefaultSecurity copy = (DefaultSecurity) copy();

        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("securityId")) {
                copy.securityId = (String) entry.getValue();
                continue;
            }

            if (entry.getKey().equalsIgnoreCase("securityIdFlag")) {
                copy.securityIdFlag = (BBSecurityIDFlag) entry.getValue();
                continue;
            }

            if (entry.getKey().equalsIgnoreCase("name")) {
                copy.name = (String) entry.getValue();
                continue;
            }

            if (entry.getKey().equalsIgnoreCase("productCode")) {
                copy.productCode = (BBProductCode) entry.getValue();
                continue;
            }

            if (entry.getKey().equalsIgnoreCase("securityType2")) {
                copy.securityType2 = (BBSecurityType) entry.getValue();
                continue;
            }
            if (entry.getKey().equalsIgnoreCase("ticker")) {
                copy.ticker = (String) entry.getValue();
                continue;
            }

        }

        return copy;
    }

}
