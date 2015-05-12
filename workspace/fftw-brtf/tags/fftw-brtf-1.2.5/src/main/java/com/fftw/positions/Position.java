package com.fftw.positions;

import static malbec.util.StringUtils.*;


import java.math.BigDecimal;

/**
 * Represent a position for a security.
 * 
 */
public class Position {

    private BigDecimal openPosition;
    private int sharesBought; // intraday buys
    private int sharesSold; // intraday sells
    private int intradayPosition; // numberOfBuys - numberOfSells
    private BigDecimal currentPosition;

    private PositionType positionType;

    private String account;

    private String level1TagName;
    private String level2TagName;
    private String level3TagName;
    private String level4TagName;

    private String primeBroker;

    private ISecurity security;

    public Position(ISecurity security, String account, String level1TagName, String level2TagName,
        String level3TagName, String level4TagName, String primeBroker, BigDecimal openPosition,
        int sharesBought, int sharesSold, int intradayPosition, BigDecimal currentPosition,
        PositionType positionType) {
        super();
        this.openPosition = openPosition;
        this.sharesBought = sharesBought;
        this.sharesSold = sharesSold;
        this.intradayPosition = intradayPosition;
        this.currentPosition = currentPosition;
        this.positionType = positionType;
        this.account = account;
        this.level1TagName = level1TagName;
        this.level2TagName = level2TagName;
        this.level3TagName = level3TagName;
        this.level4TagName = level4TagName;
        this.primeBroker = primeBroker;
        this.security = security;
    }

    public Position(ISecurity security, String account, String level1TagName, String level2TagName,
        String level3TagName, String level4TagName, String primeBroker, int openPosition, int sharesBought,
        int sharesSold, int intradayPosition, int currentPosition, PositionType positionType) {

        this(security, account, level1TagName, level2TagName, level3TagName, level4TagName, primeBroker,
            new BigDecimal(openPosition), sharesBought, sharesSold, intradayPosition, new BigDecimal(
                currentPosition), positionType);
    }

    public Position copy() {
        ISecurity securityCopy = security != null ? security.copy() : null;

        Position copy = new Position(securityCopy, account, level1TagName, level2TagName, level3TagName,
            level4TagName, primeBroker, openPosition, sharesBought, sharesSold, intradayPosition,
            currentPosition, positionType);

        return copy;
    }

    public ISecurity getSecurity() {
        return security;
    }

    /**
     * @return the openPosition
     */
    public BigDecimal getOpenPosition() {
        return openPosition;
    }

    /**
     * @return the sharesBought
     */
    public int getSharesBought() {
        return sharesBought;
    }

    /**
     * @return the sharesSold
     */
    public int getSharesSold() {
        return sharesSold;
    }

    /**
     * @return the intradayPosition
     */
    public int getIntradayPosition() {
        return intradayPosition;
    }

    /**
     * @return the currentPosition
     */
    public BigDecimal getCurrentPosition() {
        return currentPosition;
    }

    /**
     * @return the positionType
     */
    public PositionType getPositionType() {
        return positionType;
    }

    /**
     * @return the account
     */
    public String getAccount() {
        return account;
    }

    /**
     * @return the level1TagName
     */
    public String getLevel1TagName() {
        return level1TagName;
    }

    /**
     * @return the level2TagName
     */
    public String getLevel2TagName() {
        return level2TagName;
    }

    /**
     * @return the level3TagName
     */
    public String getLevel3TagName() {
        return level3TagName;
    }

    /**
     * @return the level4TagName
     */
    public String getLevel4TagName() {
        return level4TagName;
    }

    /**
     * @return the primeBroker
     */
    public String getPrimeBroker() {
        return primeBroker;
    }

    /**
     * This does not check if these two positions should be added together. That is left as an exercise for
     * the caller.
     * 
     * This will however, return an aggregated position that will null out any identifying fields that are not
     * the same between the two original positions.
     * 
     * @param addend
     * @return
     */
    public Position aggregate(Position addend) {

        if (!canAggregate(addend)) {
            throw new IllegalArgumentException("Positions cannot be aggregated.");
        }
        Position augend = copy();

        // do the math and then clear the keys
        augend.openPosition = openPosition.add(addend.openPosition);
        augend.currentPosition = currentPosition.add(addend.currentPosition);
        augend.sharesBought = sharesBought + addend.sharesBought;
        augend.sharesSold = sharesSold + addend.sharesSold;
        augend.intradayPosition = augend.sharesBought - augend.sharesSold;

        return augend;
    }

    private boolean canAggregate(ISecurity a, ISecurity b) {

        if (a == null && b == null) {
            return true;
        }
        
        if (a == null || b == null) {
            return false;
        }
        
        return areEqual(a.getSecurityId(), b.getSecurityId())
            && areEqual(a.getSecurityIdFlag(), b.getSecurityIdFlag())
            && areEqual(a.getProductCode(), b.getProductCode());
    }

    private boolean areEqual(Object a, Object b) {
        if (a == b) {
            return true;
        }

        if ((a == null && b != null) || (a != null && b == null)) {
            return false;
        }

        return a.equals(b);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);

        sb.append("account=").append(account);
        sb.append(", level1TagName=").append(level1TagName);
        sb.append(", level2TagName=").append(level2TagName);
        sb.append(", level3TagName=").append(level3TagName);
        sb.append(", level4TagName=").append(level4TagName);
        sb.append(", primeBroker=").append(primeBroker);
        sb.append(", openPosition=").append(openPosition);
        sb.append(", currentPosition=").append(currentPosition);
        sb.append(", sharesBought=").append(sharesBought);
        sb.append(", sharesSold=").append(sharesSold);
        sb.append(", intradayPosition=").append(intradayPosition);

        if (security != null) {
            sb.append(", security={").append(security).append("}");
        }
        return sb.toString();
    }

    public boolean canAggregate(Position addend) {
        return canAggregate(getSecurity(), addend.getSecurity()) && areEqual(account, addend.account)
            && areEqual(level1TagName, addend.level1TagName) && areEqual(level2TagName, addend.level2TagName)
            && areEqual(level3TagName, addend.level3TagName) && areEqual(level4TagName, addend.level4TagName);
    }

    /**
     * @param positionType
     *            the positionType to set
     */
    public void setPositionType(PositionType positionType) {
        this.positionType = positionType;
    }

    /**
     * @param account
     *            the account to set
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * @param level1TagName
     *            the level1TagName to set
     */
    public void setLevel1TagName(String level1TagName) {
        this.level1TagName = level1TagName;
    }

    /**
     * @param level2TagName
     *            the level2TagName to set
     */
    public void setLevel2TagName(String level2TagName) {
        this.level2TagName = level2TagName;
    }

    /**
     * @param level3TagName
     *            the level3TagName to set
     */
    public void setLevel3TagName(String level3TagName) {
        this.level3TagName = level3TagName;
    }

    /**
     * @param level4TagName
     *            the level4TagName to set
     */
    public void setLevel4TagName(String level4TagName) {
        this.level4TagName = level4TagName;
    }

    /**
     * @param primeBroker
     *            the primeBroker to set
     */
    public void setPrimeBroker(String primeBroker) {
        this.primeBroker = primeBroker;
    }

    /**
     * @param security
     *            the security to set
     */
    public void setSecurity(ISecurity security) {
        this.security = security;
    }

    /**
     * @param openPosition
     *            the openPosition to set
     */
    public void setOpenPosition(BigDecimal openPosition) {
        this.openPosition = openPosition;
    }
    
    public String toTextMessage(String delimiter) {
        StringBuilder sb = new StringBuilder(128);

        sb.append("account=").append(account);
        sb.append(delimiter).append("level1TagName=").append(emptyStringOrValue(level1TagName));
        sb.append(delimiter).append("level2TagName=").append(emptyStringOrValue(level2TagName));
        sb.append(delimiter).append("level3TagName=").append(emptyStringOrValue(level3TagName));
        sb.append(delimiter).append("level4TagName=").append(emptyStringOrValue(level4TagName));
        sb.append(delimiter).append("primeBroker=").append(emptyStringOrValue(primeBroker));
        sb.append(delimiter).append("openPosition=").append(openPosition);
        sb.append(delimiter).append("currentPosition=").append(currentPosition);
        sb.append(delimiter).append("sharesBought=").append(sharesBought);
        sb.append(delimiter).append("sharesSold=").append(sharesSold);
        sb.append(delimiter).append("intradayPosition=").append(intradayPosition);

        if (security != null) {
            sb.append(delimiter);
            sb.append(security.toTextMessage(delimiter));
        }
        return sb.toString();
    }

}
