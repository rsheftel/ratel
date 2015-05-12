package com.fftw.bloomberg.rtf.messages;

import static com.fftw.util.BigMath.*;
import static com.fftw.util.strings.FixedWidthExtractor.*;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.LocalDate;

import quickfix.SessionSettings;

import com.fftw.bloomberg.PositionRecord;
import com.fftw.bloomberg.types.BBFractionIndicator;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.util.Filter;

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
    private int totalNumberOfBuys; // intraday
    private int totalNumberOfSells; // intraday
    private String account;
    private BBProductCode productCode;
    private String bloombergId;

    private BBFractionIndicator strikePriceFI;
    private Long strikePrice;
    private LocalDate tradeDate;
    private Integer programType; // TODO find out what this is
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

    // this is not in the actual record we receive
    private BigDecimal contractSize = BigDecimal.ONE;
    private String exchangeTicker;
    private String ric;

    // normalized values
    BigDecimal normTotalBuyVolume;
    BigDecimal normTotalSellVolume;
    BigDecimal normOpenPosition;
    BigDecimal normCurrentPosition;

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
    private RtfOnlinePosition(String securityId, String account, String level1TagName, String level2TagName,
        String level3TagName, String level4TagName) {

        this.securityId = securityId;
        this.account = account;
        this.level1TagName = level1TagName;
        this.level2TagName = level2TagName;
        this.level3TagName = level3TagName;
        this.level4TagName = level4TagName;
    }

    public RtfOnlinePosition(String securityId, String account, String level1TagName, String level2TagName,
        String level3TagName, String level4TagName, String primeBroker, BigDecimal openPosition) {

        this.securityId = securityId;
        this.account = account;
        this.level1TagName = level1TagName;
        this.level2TagName = level2TagName;
        this.level3TagName = level3TagName;
        this.level4TagName = level4TagName;
        this.primeBroker = primeBroker;

        if (openPosition != null) {
            setOpenPosition(openPosition);
        }
    }

    public RtfOnlinePosition(String securityId, String account) {
        this(securityId, account, "", "", "", "");
    }

    public RtfOnlinePosition(String securityId, String account, String level1TagName) {
        this(securityId, account, level1TagName, "", "", "");
    }

    public String getPositionType() {
        return "OnlinePosition";
    }

    public LocalDate getMessageDate() {
        return messageDate;
    }

    void setMessageDate(String dateStr) {
        messageDate = new LocalDate(dateStr);
    }

    public int getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    void setMessageSequenceNumber(String msn) {
        messageSequenceNumber = Integer.parseInt(msn);
    }

    public char getMessageType() {
        return '4';
    }

    public BigDecimal getOpenAverageCost() {
        if (openAvgCostFI != null) {
            return convertValue(openAvgCostFI, 11, openAvgCost);
        } else {
            return BigDecimal.ZERO;
        }
    }

    void setOpenAvgCost(String oac) {
        BloombergDecimal tmp = extractParts(new BigDecimal(oac));

        openAvgCostFI = tmp.fi;
        openAvgCost = tmp.value;
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

    void setCurrentPosition(String cp) {
        BloombergDecimal tmp = extractParts(new BigDecimal(cp));

        currentPositionFI = tmp.fi;
        currentPosition = tmp.value;
    }

    public BigDecimal getTotalBuyVolume() {
        if (totalBuyVolumeFI != null) {
            return convertValue(totalBuyVolumeFI, 11, totalBuyVolume);
        } else {
            return BigDecimal.ZERO;
        }
    }

    void setTotalBuyVolume(String tbv) {
        BloombergDecimal tmp = extractParts(new BigDecimal(tbv));

        totalBuyVolumeFI = tmp.fi;
        totalBuyVolume = tmp.value;
    }

    public BigDecimal getTotalSellVolume() {
        if (totalSellVolumeFI != null) {
            return convertValue(totalSellVolumeFI, 11, totalSellVolume);
        } else {
            return BigDecimal.ZERO;
        }
    }

    void setTotalSellVolume(String tsv) {
        BloombergDecimal tmp = extractParts(new BigDecimal(tsv));

        totalSellVolumeFI = tmp.fi;
        totalSellVolume = tmp.value;
    }

    public BigDecimal getOpenPosition() {
        if (openPositionFI != null) {
            return convertValue(openPositionFI, 11, openPosition);
        } else {
            return BigDecimal.ZERO;
        }
    }

    void setOpenPosition(String op) {
        BloombergDecimal tmp = extractParts(new BigDecimal(op));

        openPositionFI = tmp.fi;
        openPosition = tmp.value;
    }

    public BigDecimal getStrikePrice() {
        if (strikePriceFI != null && strikePrice != null) {
            return convertValue(strikePriceFI, 11, strikePrice);
        } else {
            return BigDecimal.ZERO;
        }
    }

    void setStrikePrice(String sp) {
        if (sp == null || "null".equals(sp)) {
            return;
        }
        BloombergDecimal tmp = extractParts(new BigDecimal(sp));

        strikePriceFI = tmp.fi;
        strikePrice = tmp.value;
    }

    public BigDecimal getCurrentAvgCost() {
        if (currentAvgCostFI != null) {
            return convertValue(currentAvgCostFI, 11, currentAvgCost);
        } else {
            return BigDecimal.ZERO;
        }
    }

    void setCurrentAvgCost(String cav) {
        BloombergDecimal tmp = extractParts(new BigDecimal(cav));

        currentAvgCostFI = tmp.fi;
        currentAvgCost = tmp.value;
    }

    public BBSecurityIDFlag getSecurityIdFlag() {
        return securityIdFlag;
    }

    void setSecurityIdFlag(String flag) {
        securityIdFlag = BBSecurityIDFlag.valueOf(flag);
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String id) {
        securityId = id;
    }

    public String getAccount() {
        return account;
    }

    void setAccount(String a) {
        account = a;
    }

    public BBProductCode getProductCode() {
        return productCode;
    }

    void setProductCode(String pc) {
        productCode = BBProductCode.valueOf(pc);
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    void setTradeDate(String td) {
        tradeDate = new LocalDate(td);
    }

    public int getLevel1TagId() {
        return level1TagId;
    }

    void setLevel1TagId(String id) {
        if (id == null || "null".equals(id)) {
            return;
        }
        level1TagId = Integer.parseInt(id);
    }

    public String getLevel1TagName() {
        return level1TagName;
    }

    public void setLevel1TagName(String name) {
        level1TagName = name;
    }

    public int getLevel2TagId() {
        return level2TagId;
    }

    void setLevel2TagId(String id) {
        if (id == null || "null".equals(id)) {
            return;
        }
        level2TagId = Integer.parseInt(id);
    }

    public String getLevel2TagName() {
        return level2TagName;
    }

    public void setLevel2TagName(String name) {
        level2TagName = name;
    }

    void setLevel3TagId(String id) {
        if (id == null || "null".equals(id)) {
            return;
        }
        level3TagId = Integer.parseInt(id);
    }

    public int getLevel3TagId() {
        return level3TagId;
    }

    public String getLevel3TagName() {
        return level3TagName;
    }

    public void setLevel3TagName(String name) {
        level3TagName = name;
    }

    public int getLevel4TagId() {
        return level4TagId;
    }

    void setLevel4TagId(String id) {
        if (id == null || "null".equals(id)) {
            return;
        }
        level4TagId = Integer.parseInt(id);
    }

    public void setLevel4TagName(String name) {
        level4TagName = name;
    }

    public String getLevel4TagName() {
        return level4TagName;
    }

    public int getLevel5TagId() {
        return level5TagId;
    }

    void setLevel5TagId(String id) {
        if (id == null || "null".equals(id)) {
            return;
        }
        level5TagId = Integer.parseInt(id);
    }

    public String getLevel5TagName() {
        return level5TagName;
    }

    void setLevel5TagName(String name) {
        level5TagName = name;
    }

    public int getLevel6TagId() {
        return level6TagId;
    }

    void setLevel6TagId(String id) {
        if (id == null || "null".equals(id)) {
            return;
        }
        level6TagId = Integer.parseInt(id);
    }

    public String getLevel6TagName() {
        return level6TagName;
    }

    void setLevel6TagName(String name) {
        level6TagName = name;
    }

    public String getCfd() {
        return cfd;
    }

    public String getPrimeBroker() {
        return primeBroker;
    }

    public void setOpenPosition(BigDecimal openPosition) {
        BloombergDecimal bd = extractParts(openPosition);

        this.openPositionFI = bd.fi;
        this.openPosition = bd.value;
    }

    public int getTotalNumberOfBuys() {
        return totalNumberOfBuys;
    }

    void setTotalNumberOfBuys(String tnb) {
        totalNumberOfBuys = Integer.parseInt(tnb);
    }

    public int getTotalNumberOfSells() {
        return totalNumberOfSells;
    }

    public void setTotalNumberOfBuys(int totalNumberOfBuys) {
        this.totalNumberOfBuys = totalNumberOfBuys;
    }

    public void setTotalNumberOfSells(int totalNumberOfSells) {
        this.totalNumberOfSells = totalNumberOfSells;
    }

    void setTotalNumberOfSells(String tns) {
        totalNumberOfSells = Integer.parseInt(tns);
    }

    public void setCurrentPosition(BigDecimal currentPosition) {
        BloombergDecimal bd = extractParts(currentPosition);

        this.currentPositionFI = bd.fi;
        this.currentPosition = bd.value;
    }

    public void setTotalBuyVolume(BigDecimal totalBuyVolume) {
        BloombergDecimal bd = extractParts(totalBuyVolume);

        this.totalBuyVolumeFI = bd.fi;
        this.totalBuyVolume = bd.value;
    }

    public void setTotalSellVolume(BigDecimal totalSellVolume) {
        BloombergDecimal bd = extractParts(totalSellVolume);

        this.totalSellVolumeFI = bd.fi;
        this.totalSellVolume = bd.value;
    }

    public void setRealizedPL(BigDecimal realizedPL) {
        BloombergDecimal bd = extractParts(realizedPL);

        this.realizedPLFI = bd.fi;
        this.realizedPL = bd.value;
    }

    public void setRealizedPL(String realizedPL) {
        BloombergDecimal bd = extractParts(new BigDecimal(realizedPL));

        this.realizedPLFI = bd.fi;
        this.realizedPL = bd.value;
    }

    public void setOpenAverageCost(BigDecimal openAvgCost) {
        BloombergDecimal bd = extractParts(openAvgCost);

        this.openAvgCostFI = bd.fi;
        this.openAvgCost = bd.value;
    }

    public void setCurrentAverageCost(BigDecimal currentAvgCost) {
        BloombergDecimal bd = extractParts(currentAvgCost);

        this.currentAvgCostFI = bd.fi;
        this.currentAvgCost = bd.value;
    }

    public void setPrimeBroker(String primeBroker) {
        this.primeBroker = primeBroker;
    }

    public String toTextMessage(String delimiter) {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("messageDate=").append(messageDate);
        sb.append(delimiter).append("messageSequenceNumber=").append(messageSequenceNumber);
        sb.append(delimiter).append("securityIdFlag=").append(securityIdFlag);
        sb.append(delimiter).append("securityId=").append(securityId);
        sb.append(delimiter).append("exchangeTicker=").append(emptyStringOrValue(exchangeTicker));
        sb.append(delimiter).append("currentAvgCost=").append(getCurrentAvgCost());
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
        sb.append(delimiter).append("yellowKey=").append(productCode.getShortString());
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
        sb.append(delimiter).append("contractSize=").append(contractSize);

        // Add the normalized values
        sb.append(delimiter).append("normTotalBuyVolume=").append(normTotalBuyVolume);
        sb.append(delimiter).append("normTotalSellVolume=").append(normTotalSellVolume);
        sb.append(delimiter).append("normOpenPosition=").append(emptyStringOrValueBG(normOpenPosition));
        sb.append(delimiter).append("normCurrentPosition=").append(normCurrentPosition);

        BigDecimal intraDayNetPosition = normTotalBuyVolume.subtract(normTotalSellVolume);
        sb.append(delimiter).append("intraDayNetPosition=").append(intraDayNetPosition);

        return sb.toString();
    }

    public static String emptyStringOrValueBG(BigDecimal value) {
        if (value == null) {
            return "";
        } else {
            return value.stripTrailingZeros().toPlainString();
        }
    }
    
    public BigDecimal getContractSize() {
        return contractSize;
    }

    /**
     * Blinding add our aggretable parts without checking any rules about key violations.
     * 
     * @param addend
     * @return
     */
    public RtfOnlinePosition aggregate(RtfOnlinePosition addend) {

        RtfOnlinePosition augend = copy();

        augend.setOpenPosition(add(augend.getOpenPosition(), addend.getOpenPosition()));
        augend.setRealizedPL(add(augend.getRealizedPL(), addend.getRealizedPL()));
        augend.setCurrentPosition(add(augend.getCurrentPosition(), addend.getCurrentPosition()));

        augend.setTotalBuyVolume(add(augend.getTotalBuyVolume(), addend.getTotalBuyVolume()));
        augend.setTotalSellVolume(add(augend.getTotalSellVolume(), addend.getTotalSellVolume()));

        augend.setTotalNumberOfBuys(augend.totalNumberOfBuys + addend.totalNumberOfBuys);
        augend.setTotalNumberOfSells(augend.totalNumberOfSells + addend.totalNumberOfSells);

        augend.setShort(augend.getCurrentPosition().compareTo(BigDecimal.ZERO) >= 0 ? "N" : "Y");

        // wipe these out as we cannot deal with them
        augend.setCurrentAverageCost(BigDecimal.ZERO);
        augend.setOpenAverageCost(BigDecimal.ZERO);

        augend.normalizePositionValues();

        return augend;
    }

    /**
     * Create a copy of ourselves.
     * 
     * @return
     */
    public RtfOnlinePosition copy() {
        RtfOnlinePosition localCopy = new RtfOnlinePosition(securityId, account, level1TagName,
            level2TagName, level3TagName, level4TagName);

        localCopy.securityIdFlag = securityIdFlag;
        localCopy.messageDate = messageDate;

        localCopy.currentAvgCostFI = currentAvgCostFI;
        localCopy.currentAvgCost = currentAvgCost;

        localCopy.openAvgCostFI = openAvgCostFI;
        localCopy.openAvgCost = openAvgCost;

        localCopy.realizedPLFI = realizedPLFI;
        localCopy.realizedPL = realizedPL;

        localCopy.currentPositionFI = currentPositionFI;
        localCopy.currentPosition = currentPosition;

        localCopy.totalBuyVolumeFI = totalBuyVolumeFI;
        localCopy.totalBuyVolume = totalBuyVolume;

        localCopy.totalSellVolumeFI = totalSellVolumeFI;
        localCopy.totalSellVolume = totalSellVolume;

        localCopy.openPositionFI = openPositionFI;
        localCopy.openPosition = openPosition;

        localCopy.totalNumberOfBuys = totalNumberOfBuys;
        localCopy.totalNumberOfSells = totalNumberOfSells;

        localCopy.productCode = productCode;
        localCopy.bloombergId = bloombergId;

        localCopy.strikePriceFI = strikePriceFI;
        localCopy.strikePrice = strikePrice;

        localCopy.tradeDate = tradeDate;
        localCopy.programType = programType;
        localCopy.daysToSettle = daysToSettle;

        localCopy.level1TagId = level1TagId;
        localCopy.level1TagName = level1TagName;

        localCopy.level2TagId = level2TagId;
        localCopy.level2TagName = level2TagName;

        localCopy.level3TagId = level3TagId;
        localCopy.level3TagName = level3TagName;

        localCopy.level4TagId = level4TagId;
        localCopy.level4TagName = level4TagName;

        localCopy.level5TagId = level5TagId;
        localCopy.level5TagName = level5TagName;

        localCopy.level6TagId = level6TagId;
        localCopy.level6TagName = level6TagName;

        localCopy.shortFlag = shortFlag;

        localCopy.cfd = cfd;
        localCopy.primeBroker = primeBroker;

        localCopy.exchangeTicker = exchangeTicker;
        localCopy.contractSize = contractSize;
        localCopy.productCode = productCode;
        localCopy.ric = ric;

        localCopy.normalizePositionValues();

        return localCopy;
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
        sb.append(", exchangeTicker=").append(exchangeTicker);
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
        sb.append(", contractSize=").append(contractSize);

        return sb.toString();
    }

    static BigDecimal convertValue(BBFractionIndicator fi, int valueLength, long value) {
        MathContext mc = new MathContext(valueLength);
        BigDecimal bd = new BigDecimal(value, mc);
        bd = bd.movePointLeft(fi.getDecimals());

        return bd;
    }

    static BloombergDecimal extractParts(BigDecimal value) {
        int fractions = value.scale();
        // Do the calculations in temp variables to ensure we don't have exceptions that
        // leave the state undefined
        BBFractionIndicator tmpFi = BBFractionIndicator.valueOf(fractions);
        BigDecimal tmp = value.multiply(powerOf(10, fractions));

        return new BloombergDecimal(tmpFi, tmp.longValue());
    }

    public static RtfOnlinePosition valueOf(LocalDate messageDate, int sequenceNubmer, String rawString) {
        RtfOnlinePosition position = valueOf(rawString);
        position.messageDate = messageDate;
        position.messageSequenceNumber = sequenceNubmer;

        return position;
    }

    public static RtfOnlinePosition valueOfCommaString(String toString) throws IntrospectionException,
        IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        Map<String, Method> methods = getPropertyWriters();

        String[] pairs = toString.split(",");
        RtfOnlinePosition position = new RtfOnlinePosition();

        for (String pair : pairs) {
            String[] kv = pair.split("=");
            Method method = methods.get(kv[0].trim());

            if (method == null) {
                System.out.println("Missing setter for " + kv[0]);
            }
            if (kv.length > 1) {
                try {
                    method.invoke(position, kv[1].trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        position.normalizePositionValues();

        return position;
    }

    private static Map<String, Method> getPropertyWriters() {
        Map<String, Method> methodMap = new HashMap<String, Method>();

        try {
            Class<?> partypes[] = new Class[1];
            partypes[0] = SessionSettings.class;

            final Method[] methods = RtfOnlinePosition.class.getDeclaredMethods();
            // loop over the list since finding it directly does not work
            Method meth = null;
            for (int i = 0; i < methods.length; i++) {

                meth = methods[i];
                String name = meth.getName();
                Class<?>[] params = meth.getParameterTypes();
                if (name.startsWith("set") && (params[0].getName().contains("String"))) {
                    meth.setAccessible(true);
                    StringBuilder propertyName = new StringBuilder(name.substring(3, name.length()));
                    propertyName.replace(0, 1, propertyName.substring(0, 1).toLowerCase());
                    methodMap.put(propertyName.toString(), meth);
                }
            }
        } catch (Throwable e) {
            System.err.println(e);
        }

        return methodMap;
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

        // Ensure that the normalized fields are populated. Once we get the real
        // contract size, we will re-normalize
        position.normalizePositionValues();

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
     * This throws UnsupportedOperationException. Use hasRawMessage prior to calling getRawMessage;
     * 
     * @return
     * @throws UnsupportedOperationException
     */
    public String getRawMessage() {
        throw new UnsupportedOperationException("RtfOnlinePosition does not store the raw string");
    }

    public static Filter<RtfOnlinePosition> createFilter(Map<String, String> filteredFields) {
        return new OnlinePositionDynamicFieldFilter(filteredFields);
    }

    private static class OnlinePositionDynamicFieldFilter implements Filter<RtfOnlinePosition> {

        private String[] supportedFields = { "account" };

        private boolean checkAccount = false;
        private String accountValue;

        /**
         * More logic than needed, but this was originally going to be able to handle generic field selection
         * 
         * @param requestedFieldMap
         */
        OnlinePositionDynamicFieldFilter(Map<String, String> requestedFieldMap) {

            if (!requestedFieldMap.isEmpty()) {

                Set<String> supportedFieldSet = new TreeSet<String>();
                supportedFieldSet.addAll(Arrays.asList(supportedFields));

                for (Map.Entry<String, String> entry : requestedFieldMap.entrySet()) {
                    if (supportedFieldSet.contains(entry.getKey())) {
                        checkAccount = true;
                        accountValue = entry.getValue();
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

    private static class BloombergDecimal {
        BBFractionIndicator fi;
        long value;

        BloombergDecimal(BBFractionIndicator fi, long v) {
            this.fi = fi;
            this.value = v;
        }
    }

    private void normalizePositionValues() {
        if (productCode == null) {
            throw new NullPointerException("Cannot normalize without a product code");
        }

        normTotalBuyVolume = normalizeShares(getTotalBuyVolume());
        normTotalSellVolume = normalizeShares(getTotalSellVolume());

        if (productCode == BBProductCode.Currency) {
            normOpenPosition = normalizeShares(getOpenPosition());
            normCurrentPosition = normalizeShares(getCurrentPosition());
        } else {
            normOpenPosition = getOpenPosition();
            normCurrentPosition = getCurrentPosition();
        }
    }

    public String toReconString() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("ACCOUNT=").append(getAccount()).append("|");
        sb.append("BID=").append(getReconBloombergId()).append("|");
        sb.append("SECURITYIDFLAG=").append(securityIdFlag).append("|");

        sb.append("SHARESBOUGHT=").append(normTotalBuyVolume).append("|");
        sb.append("SHARESSOLD=").append(normTotalSellVolume).append("|");
        sb.append("OPENSHARES=").append(normOpenPosition).append("|");

        sb.append("RIC=").append(ric == null ? "" : ric).append("|");
        sb.append("CURRENTPOSITION=").append(normCurrentPosition).append("|");

        BigDecimal intraDayNetPosition = normTotalBuyVolume.subtract(normTotalSellVolume);
        sb.append("POSITION=").append(intraDayNetPosition).append("|");

        sb.append("YELLOWKEY=").append(productCode.getShortString()).append("|");
        sb.append("AVERAGEPRICE=").append(getCurrentAvgCost()).append("|");
        sb.append("SOURCE=AIM");

        return sb.toString();
    }

    private BigDecimal normalizeShares(BigDecimal shareVolume) {
        switch (productCode) {
            case Equity:
            case Index:
                return shareVolume;
            default:
                if (BigDecimal.ZERO.compareTo(contractSize) == 0) {
                    return shareVolume;
                }
                return shareVolume.divide(contractSize);
        }
    }

    /*
     * private long normalizeShares(long shareVolume) { switch (productCode) { case Equity: case Index: return
     * shareVolume; default: return shareVolume / contractSize.longValue(); } }
     */

    public void setBloombergId(String bloombergId) {
        this.bloombergId = bloombergId;
    }

    public String getReconBloombergId() {
        return securityId;
    }

    public void setProductCode(BBProductCode productCode) {
        this.productCode = productCode;
    }

    public String getBloombergId() {
        return bloombergId;
    }

    void setProgramType(String pt) {
        if (pt == null || "null".equals(pt)) {
            return;
        }

        programType = Integer.parseInt(pt);
    }

    void setdaysToSettle(String dts) {
        if (dts == null || "null".equals(dts)) {
            return;
        }

        daysToSettle = Integer.parseInt(dts);
    }

    void setShort(String s) {
        shortFlag = s;
    }

    public String getShort() {
        return shortFlag;
    }

    void setCfd(String cfd) {
        this.cfd = cfd;
    }

    public void setContractSize(BigDecimal contractSize) {
        this.contractSize = contractSize;
        normalizePositionValues();
    }

    public boolean isEquity() {
        return productCode == BBProductCode.Equity;
    }

    public boolean isIndex() {
        return productCode == BBProductCode.Index;
    }

    public boolean isMortgage() {
        return productCode == BBProductCode.Mortgage;
    }

    public boolean isCommodity() {
        return productCode == BBProductCode.Commodity;
    }

    public boolean isCurrency() {
        return productCode == BBProductCode.Currency;
    }

    public void setExchangeTicker(String ticker) {
        exchangeTicker = ticker;
    }

    public String getExchangeTicker() {
        return exchangeTicker;
    }

    public void setRic(String ric) {
        this.ric = ric;
    }

    public boolean isCorporate() {
        return productCode == BBProductCode.Corporate;
    }

    /**
     * @return the normTotalBuyVolume
     */
    public BigDecimal getNormTotalBuyVolume() {
        return normTotalBuyVolume;
    }

    /**
     * @return the normTotalSellVolume
     */
    public BigDecimal getNormTotalSellVolume() {
        return normTotalSellVolume;
    }

    /**
     * @return the normOpenPosition
     */
    public BigDecimal getNormOpenPosition() {
        return normOpenPosition;
    }

    /**
     * @return the normCurrentPosition
     */
    public BigDecimal getNormCurrentPosition() {
        return normCurrentPosition;
    }

}
