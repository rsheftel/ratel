package com.fftw.bloomberg.rtf.messages;

import com.fftw.bloomberg.types.BBFractionIndicator;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.PositionRecord;
import com.fftw.util.Filter;
import static com.fftw.util.strings.FixedWidthExtractor.*;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represent an Online Position record
 */
public class RtfOnlinePosition implements RtfMessageTradeBody, PositionRecord {

    private LocalDate messageDate;

    private int messageSequenceNumber;

    private BBSecurityIDFlag securityIdFlag;
    private String securityId;
    private BBFractionIndicator currentAvgCostFI; // fraction indicator
    private long currentAvgCost;
    private BBFractionIndicator openAvgCostFI; // fraction indicator
    private long openAvgCost;
    private BBFractionIndicator realizedPLFI;
    private long realizedPL;
    private BBFractionIndicator currentPositionFI;
    private long currentPosition;
    private BBFractionIndicator totalBuyVolumeFI;
    private long totalBuyVolume;
    private BBFractionIndicator totalSellVolumeFI;
    private long totalSellVolume;
    private BBFractionIndicator openPositionFI;
    private long openPosition;
    private int totalNumberOfBuys; // interday
    private int totalNumberOfSells; // interday
    private String account;
    private BBProductCode productCode;
    private String bloombergId;

    private BBFractionIndicator strikePriceFI;
    private Long strikePrice;
    private LocalDate tradeDate;
    private Integer programType;  // TODO find out what this is
    private Integer daysToSettle;

    private Integer level1TagId;
    private String level1TagName;
    private Integer level2TagId;
    private String level2TagName;
    private Integer level3TagId;
    private String level3TagName;
    private Integer level4TagId;
    private String level4TagName;
    private Integer level5TagId;
    private String level5TagName;
    private Integer level6TagId;
    private String level6TagName;

    private String shortFlag; // N for not short(long) and Y for short
    private String cfd;

    private String primeBroker;


    RtfOnlinePosition() {

    }

    /**
     * Create a batch position with all the required keys to uniquely identify
     *
     * @param securityId
     * @param account
     * @param level1TagName
     * @param level2TagName
     * @param level3TagName
     * @param level4TagName
     */
    public RtfOnlinePosition(String securityId, String account, String level1TagName, String level2TagName,
                             String level3TagName, String level4TagName) {

        this.securityId = securityId;
        this.account = account;
        this.level1TagName = level1TagName;
        this.level2TagName = level2TagName;
        this.level3TagName = level3TagName;
        this.level4TagName = level4TagName;
    }


    public String getPositionType() {
        return "OnlinePosition";
    }

    public LocalDate getMessageDate() {
        return messageDate;
    }

    public int getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public char getMessageType() {
        return '4';
    }

    public BigDecimal getAverageCost() {
        if (currentAvgCostFI != null) {
            return convertValue(currentAvgCostFI, 11, currentAvgCost);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getOpenAverageCost() {
        if (openAvgCostFI != null) {
            return convertValue(openAvgCostFI, 11, openAvgCost);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getRealizedPL() {
        if (realizedPLFI != null) {
            return convertValue(realizedPLFI, 11, realizedPL);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getCurrentPosition() {
        if (currentPositionFI != null) {
            return convertValue(currentPositionFI, 11, currentPosition);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalBuyVolume() {
        if (totalBuyVolumeFI != null) {
            return convertValue(totalBuyVolumeFI, 11, totalBuyVolume);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalSellVolume() {
        if (totalSellVolumeFI != null) {
            return convertValue(totalSellVolumeFI, 11, totalSellVolume);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getOpenPosition() {
        if (openPositionFI != null) {
            return convertValue(openPositionFI, 11, openPosition);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getStrikePrice() {
        if (strikePriceFI != null && strikePrice != null) {
            return convertValue(strikePriceFI, 11, strikePrice);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BBSecurityIDFlag getSecurityIdFlag() {
        return securityIdFlag;
    }

    public String getSecurityId() {
        return securityId;
    }

    public String getAccount() {
        return account;
    }

    public BBProductCode getProductCode() {
        return productCode;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public int getLevel1TagId() {
        return level1TagId;
    }

    public String getLevel1TagName() {
        return level1TagName;
    }

    public int getLevel2TagId() {
        return level2TagId;
    }

    public String getLevel2TagName() {
        return level2TagName;
    }

    public int getLevel3TagId() {
        return level3TagId;
    }

    public String getLevel3TagName() {
        return level3TagName;
    }

    public int getLevel4TagId() {
        return level4TagId;
    }

    public String getLevel4TagName() {
        return level4TagName;
    }

    public int getLevel5TagId() {
        return level5TagId;
    }

    public String getLevel5TagName() {
        return level5TagName;
    }

    public int getLevel6TagId() {
        return level6TagId;
    }

    public String getLevel6TagName() {
        return level6TagName;
    }

    public String getCfd() {
        return cfd;
    }

    public String toTextMessage(String delimiter) {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("messageDate=").append(messageDate);
        sb.append(delimiter).append("messageSequenceNumber=").append(messageSequenceNumber);
        sb.append(delimiter).append("securityIdFlag=").append(securityIdFlag);
        sb.append(delimiter).append("securityId=").append(securityId);
        sb.append(delimiter).append("currentAvgCost=").append(getAverageCost());
        sb.append(delimiter).append("openAvgCost=").append(getOpenAverageCost());
        sb.append(delimiter).append("realizedPL=").append(getRealizedPL());
        sb.append(delimiter).append("currentPosition=").append(getCurrentPosition());
        sb.append(delimiter).append("totalBuyVolume=").append(getTotalBuyVolume());
        sb.append(delimiter).append("totalSellVolume=").append(getTotalSellVolume());
        sb.append(delimiter).append("openPosition=").append(getOpenPosition());
        sb.append(delimiter).append("totalNumberOfBuys=").append(totalNumberOfBuys);
        sb.append(delimiter).append("totalNumberOfSells=").append(totalNumberOfSells);
        sb.append(delimiter).append("account=").append(account);
        sb.append(delimiter).append("productCode=").append(productCode);
        sb.append(delimiter).append("bloombergId=").append(bloombergId);
        sb.append(delimiter).append("strikePrice=").append(getStrikePrice());
        sb.append(delimiter).append("tradeDate=").append(tradeDate);
        sb.append(delimiter).append("programType=").append(emptyStringOrValue(programType));
        sb.append(delimiter).append("daysToSettle=").append(emptyStringOrValue(daysToSettle));

        sb.append(delimiter).append("level1TagId=").append(emptyStringOrValue(level1TagId));
        sb.append(delimiter).append("level1TagName=").append(emptyStringOrValue(level1TagName));
        sb.append(delimiter).append("level2TagId=").append(emptyStringOrValue(level2TagId));
        sb.append(delimiter).append("level2TagName=").append(emptyStringOrValue(level2TagName));
        sb.append(delimiter).append("level3TagId=").append(emptyStringOrValue(level3TagId));
        sb.append(delimiter).append("level3TagName=").append(emptyStringOrValue(level3TagName));
        sb.append(delimiter).append("level4TagId=").append(emptyStringOrValue(level4TagId));
        sb.append(delimiter).append("level4TagName=").append(emptyStringOrValue(level4TagName));
        sb.append(delimiter).append("level5TagId=").append(emptyStringOrValue(level5TagId));
        sb.append(delimiter).append("level5TagName=").append(emptyStringOrValue(level5TagName));
        sb.append(delimiter).append("level6TagId=").append(emptyStringOrValue(level6TagId));
        sb.append(delimiter).append("level6TagName=").append(emptyStringOrValue(level6TagName));

        sb.append(delimiter).append("short=").append(shortFlag);
        sb.append(delimiter).append("cfd=").append(cfd);
        sb.append(delimiter).append("primeBroker=").append(primeBroker);

        return sb.toString();
    }

    public String toString() {
        return toTextMessage(", ");
    }
    /**
     * Return an empty string for null values.
     *
     * @param value
     * @return
     */
    private String emptyStringOrValue(Object value) {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    public String toRawString() {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("messageDate=").append(messageDate);
        sb.append(", messageSequenceNumber=").append(messageSequenceNumber);
        sb.append(", securityIdFlag=").append(securityIdFlag);
        sb.append(", securityId=").append(securityId);
        sb.append(", currentAvgCost=").append(currentAvgCost);
        sb.append(", openAvgCost=").append(openAvgCost);
        sb.append(", realizedPL=").append(realizedPL);
        sb.append(", currentPosition=").append(currentPosition);
        sb.append(", totalBuyVolume=").append(totalBuyVolume);
        sb.append(", totalSellVolume=").append(totalSellVolume);
        sb.append(", openPosition=").append(openPosition);
        sb.append(", totalNumberOfBuys=").append(totalNumberOfBuys);
        sb.append(", totalNumberOfSells=").append(totalNumberOfSells);
        sb.append(", account=").append(account);
        sb.append(", productCode=").append(productCode);
        sb.append(", bloombergId=").append(bloombergId);
        sb.append(", strikePrice=").append(strikePrice);
        sb.append(", tradeDate=").append(tradeDate);
        sb.append(", programType=").append(programType);
        sb.append(", daysToSettle=").append(daysToSettle);

        sb.append(", level1TagId=").append(level1TagId);
        sb.append(", level1TagName=").append(level1TagName);
        sb.append(", level2TagId=").append(level2TagId);
        sb.append(", level2TagName=").append(level2TagName);
        sb.append(", level3TagId=").append(level3TagId);
        sb.append(", level3TagName=").append(level3TagName);
        sb.append(", level4TagId=").append(level4TagId);
        sb.append(", level4TagName=").append(level4TagName);
        sb.append(", level5TagId=").append(level5TagId);
        sb.append(", level5TagName=").append(level5TagName);
        sb.append(", level6TagId=").append(level6TagId);
        sb.append(", level6TagName=").append(level6TagName);

        sb.append(", short=").append(shortFlag);
        sb.append(", cfd=").append(cfd);
        sb.append(", primeBroker=").append(primeBroker);

        return sb.toString();
    }

    static BigDecimal convertValue(BBFractionIndicator fi, int valueLength, long value) {
        MathContext mc = new MathContext(valueLength);
        BigDecimal bd = new BigDecimal(value, mc);
        bd = bd.movePointLeft(fi.getDecimals());

        return bd;
    }


    static RtfOnlinePosition valueOf(LocalDate messageDate, int sequenceNubmer, String rawString) {
        RtfOnlinePosition position = valueOf(rawString);
        position.messageDate = messageDate;
        position.messageSequenceNumber = sequenceNubmer;

        return position;
    }

    private static RtfOnlinePosition valueOf(String rawString) {
        RtfOnlinePosition position = new RtfOnlinePosition();

        position.securityIdFlag = BBSecurityIDFlag.valueOf(extractInt(rawString, 1, 2));
        position.securityId = extractString(rawString, 3, 14);

        position.currentAvgCostFI = BBFractionIndicator.valueOf(extractChar(rawString, 15));
        position.currentAvgCost = extractLong(rawString, 16, 26);

        position.openAvgCostFI = BBFractionIndicator.valueOf(extractChar(rawString, 27));
        position.openAvgCost = extractLong(rawString, 28, 38);

        position.realizedPLFI = BBFractionIndicator.valueOf(extractChar(rawString, 39, 40));
        position.realizedPL = extractLong(rawString, 41, 56);

        position.currentPositionFI = BBFractionIndicator.valueOf(extractChar(rawString, 57, 58));
        position.currentPosition = extractLong(rawString, 59, 74);

        position.totalBuyVolumeFI = BBFractionIndicator.valueOf(extractChar(rawString, 75, 76));
        position.totalBuyVolume = extractLong(rawString, 77, 92);

        position.totalSellVolumeFI = BBFractionIndicator.valueOf(extractChar(rawString, 93, 94));
        position.totalSellVolume = extractLong(rawString, 95, 110);

        position.openPositionFI = BBFractionIndicator.valueOf(extractChar(rawString, 111, 112));
        position.openPosition = extractLong(rawString, 113, 128);

        position.totalNumberOfBuys = extractInt(rawString, 129, 132);
        position.totalNumberOfSells = extractInt(rawString, 133, 136);

        position.account = extractString(rawString, 137, 144);
        position.productCode = BBProductCode.valueOf(extractInt(rawString, 145, 146));
        position.bloombergId = extractString(rawString, 147, 158);

        position.strikePriceFI = BBFractionIndicator.valueOf(extractChar(rawString, 159));
        position.strikePrice = extractLong(rawString, 160, 170, null);

        position.tradeDate = extractDate(rawString, 171, 178);
        position.programType = extractInteger(rawString, 179, 181, null);
        position.daysToSettle = extractInteger(rawString, 182, 184, null);

        position.level1TagId = extractInteger(rawString, 185, 193, null);
        position.level1TagName = extractString(rawString, 194, 243);

        position.level2TagId = extractInteger(rawString, 244, 252, null);
        position.level2TagName = extractString(rawString, 253, 302);

        position.level3TagId = extractInteger(rawString, 303, 311, null);
        position.level3TagName = extractString(rawString, 312, 361);

        position.level4TagId = extractInteger(rawString, 362, 370, null);
        position.level4TagName = extractString(rawString, 371, 420);

        position.level5TagId = extractInteger(rawString, 421, 429, null);
        position.level5TagName = extractString(rawString, 430, 479);

        position.level6TagId = extractInteger(rawString, 480, 488, null);
        position.level6TagName = extractString(rawString, 489, 538);

        position.shortFlag = extractString(rawString, 539, 539);

        position.cfd = extractString(rawString, 540, 540);
        position.primeBroker = extractString(rawString, 541, 546);

        return position;
    }

    /**
     * RtfOnlinePosition does not store the raw string.
     *
     * @return
     */
    public boolean hasRawMessage() {
        return false;
    }

    /**
     * This throws UnsupportedOperationException.  Use hasRawMessage prior to calling
     * getRawMessage;
     *
     * @return
     * @exception UnsupportedOperationException
     */
    public String getRawMessage() {
        throw new UnsupportedOperationException("RtfOnlinePosition does not store the raw string");
    }

    public static Filter<RtfOnlinePosition> createFilter(Map<String, String> filteredFields) {
        return new OnlinePositionDynamicFieldFilter(filteredFields);
    }

    private static class OnlinePositionDynamicFieldFilter implements Filter<RtfOnlinePosition> {

        private String[] supportedFields = {"account"};

        private boolean checkAccount = false;
        private String accountValue;

        /**
         * More logic than needed, but this was originally going to be able to handle
         * generic field selection
         *
         * @param requestedFieldMap
         */
        OnlinePositionDynamicFieldFilter(Map<String, String> requestedFieldMap) {

            if (!requestedFieldMap.isEmpty()) {

                Set<String> supportedFieldSet = new TreeSet<String>();
                supportedFieldSet.addAll(Arrays.asList(supportedFields));

                for (String field : requestedFieldMap.keySet()) {
                    if (supportedFieldSet.contains(field)) {
                        checkAccount = true;
                        accountValue = requestedFieldMap.get(field);
                    }
                }
            }
        }

        public boolean accept(RtfOnlinePosition item) {

            if (checkAccount) {
                return item.getAccount().equals(accountValue);
            }

            return true;
        }
    }
}
