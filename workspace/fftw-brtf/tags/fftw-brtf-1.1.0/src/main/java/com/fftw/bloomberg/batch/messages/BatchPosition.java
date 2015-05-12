package com.fftw.bloomberg.batch.messages;

import com.fftw.bloomberg.PositionRecord;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.BBSide;
import com.fftw.util.Aggregatable;
import static com.fftw.util.BigMath.add;
import com.fftw.util.Filter;
import static com.fftw.util.strings.FixedWidthExtractor.*;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The Batch Position File layout -- selected fields.
 * <p/>
 * The keys to a batch position are:
 * <ol><li>identifier</li>
 * <li>account/tradername</li>
 * <li>level1TagName - Strategy</li>
 * </ol>
 */
public class BatchPosition implements PositionRecord, Aggregatable<BatchPosition> {

    private LocalDate batchFileDate;
    private BBSecurityIDFlag securityIdFlag;
    private String securityId;

    private BBProductCode productCode; // This layout has it as decimal...
    private String ticker;
    private String name;
    private String identifier;

    private BigDecimal fullCurrentNetPosition;
    private Currency currency;

    private BigDecimal coupon;
    private BigDecimal couponFrequency;
    private LocalDate firstCouponDate;
    private LocalDate maturityDate;

    private BigDecimal markToMarketPrice;

    private String transactionCounterparty;

    private String account;
    private String subsector;
    private BigDecimal previousClosePrice;
    private String countryFullName;

    private BigDecimal dayCount;
    private LocalDate dlvMinimumMaturityDate;
    private BigDecimal underlyingOptionsPrice;
    private LocalDate underlyingExpirationDate;
    private BigDecimal conversionFactor;

    private String numberOfContracts;
    private LocalDate expirationDate;

    private BigDecimal financeRate;
    private LocalDate lastTradeDate;
    private String putOrCall;

    private String exerciseType;
    private Currency underlyingCurrency;
    private String longExchangeName;

    private BigDecimal contractSize;

    private LocalDate convertibleStartDate;
    private BigDecimal bqFaceAmount;
    private BigDecimal convertionRatio;
    private String commonStockExchange;
    private String issuerParentEquityTicker;
    private BigDecimal commonStockPrice;
    private BigDecimal currentPosition;
    private String primaryExchangeMic;  // market identification code
    private String tickerAndExchangeCode;
    private String isinNumber;

    private BigDecimal strikePrice;
    private BigDecimal swapSpread;
    private String receiveFrequency;
    private String payFrequency;

    private BBSide customerSide;

    private String calculationAgent;

    private BBSide cdsSide;
    private Currency baseCurrency;
    private String currencySecurityDescription;
    private BigDecimal forwardPrice;
    private String underlyingReferenceIndex;
    private String bloombergSwapCurveName;
    private BigDecimal swapCurveDaysToMaturity;
    private String securityType2;
    private LocalDate nextSettlementDate;
    private String issuerIndustry;
    private BigDecimal nextCallPrice;
    private String futuresCategory;
    private BigDecimal nextPutPrice;
    private LocalDate nextPutDate;
    private String pricingModelType;
    private LocalDate forwardDate;
    private String paymentType;
    private String bloombergSwapCurveNameRealtime;

    private String level1TagName;

    private LocalDate interestAccrualDate;
    private String cusipNumber;
    private BigDecimal cheapestToDeliverCoupon;

    private String cheapestToDeliverFrequency;
    private LocalDate cheapestToDeliverMaturity;

    private String level2TagName;
    private String level3TagName;
    private String level4TagName;

    private String countryIsoCode; // Locale has the language also, and not way to get just the country
    private LocalDate swaptionExperiationDate;
    private String swapType;
    private String payFixedOrPayFloat;
    private BigDecimal payCoupon;
    private String paySideDayCount;
    private String receiveSideDayCount;
    private String receiveSideCouponType;
    private BigDecimal couponOrSpread;
    private BigDecimal receiveCoupon;

    private BigDecimal primaryTraderPosition;

    private BigDecimal currentLongPosition;

    private BigDecimal currentShortPosition;

    private BigDecimal fullCurrentNetPositionWithoutComma;
    private Currency tradedByCurrency;
    private String couponType;
    private String bloombergId;

    private BatchPosition() {

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
    public BatchPosition(LocalDate batchFileDate, String securityId, String account, String level1TagName, String level2TagName,
                         String level3TagName, String level4TagName) {

        this.batchFileDate = batchFileDate;
        this.securityId = securityId;
        this.account = account;
        this.level1TagName = level1TagName;
        this.level2TagName = level2TagName;
        this.level3TagName = level3TagName;
        this.level4TagName = level4TagName;
    }

    public String getPositionType() {
        return "BatchPosition";
    }

    public void setFullCurrentNetPosition(BigDecimal fullCurrentNetPosition) {
        this.fullCurrentNetPosition = fullCurrentNetPosition;
    }

    public void setFullCurrentNetPositionWithoutComma(BigDecimal fullCurrentNetPositionWithoutComma) {
        this.fullCurrentNetPositionWithoutComma = fullCurrentNetPositionWithoutComma;
    }

    public void setSecurityIdFlag(BBSecurityIDFlag securityIdFlag) {
        this.securityIdFlag = securityIdFlag;
    }

    public void setProductCode(BBProductCode productCode) {
        this.productCode = productCode;
    }

    public void setNumberOfContracts(String numberOfContracts) {
        this.numberOfContracts = numberOfContracts;
    }

    public void setCurrentLongPosition(BigDecimal currentLongPosition) {
        this.currentLongPosition = currentLongPosition;
    }

    public void setCurrentShortPosition(BigDecimal currentShortPosition) {
        this.currentShortPosition = currentShortPosition;
    }

    /**
     * Create a BatchPostion record from a string of text.
     * <p/>
     * This uses the production layout - should be the correct version
     *
     * @param rawString
     * @return
     */
    public static BatchPosition valueOf(LocalDate batchFileDate, String rawString) {
        BatchPosition position = new BatchPosition();

        position.batchFileDate = batchFileDate;

        position.securityIdFlag = BBSecurityIDFlag.valueOf(extractInt(rawString, 1, 2));
        position.securityId = extractString(rawString, 3, 14);
        position.productCode = BBProductCode.valueOf((int) extractFloat(rawString, 15, 23));


        position.ticker = extractString(rawString, 24, 33);
        position.name = extractString(rawString, 34, 63);
        position.identifier = extractString(rawString, 64, 89);

        position.fullCurrentNetPosition = extractBigDecimal(rawString, 90, 106, null);
        position.currency = extractCurrency(rawString, 107, 114, null);
        position.coupon = extractBigDecimal(rawString, 121, 135, null);
        position.couponFrequency = extractBigDecimal(rawString, 136, 142, null);
        position.firstCouponDate = extractDate(rawString, 143, 150, null);
        position.maturityDate = extractDate(rawString, 151, 158, null);


        position.markToMarketPrice = extractBigDecimal(rawString, 159, 169, null);

        position.transactionCounterparty = extractString(rawString, 178, 187);

        position.account = extractString(rawString, 188, 195);
        position.subsector = extractString(rawString, 196, 215);

        position.previousClosePrice = extractBigDecimal(rawString, 216, 230, null);
        position.countryFullName = extractString(rawString, 231, 246);
        position.dayCount = extractBigDecimal(rawString, 247, 257, null);
        position.dlvMinimumMaturityDate = extractDate(rawString, 258, 265, null);
        position.underlyingOptionsPrice = extractBigDecimal(rawString, 266, 280, null);
        position.underlyingExpirationDate = extractDate(rawString, 281, 288, null);
        position.conversionFactor = extractBigDecimal(rawString, 289, 301, null);
        position.numberOfContracts = extractString(rawString, 302, 311);
        position.expirationDate = extractDate(rawString, 312, 319, null);
        position.financeRate = extractBigDecimal(rawString, 320, 334, null);

        position.lastTradeDate = extractDate(rawString, 335, 342, null);
        position.putOrCall = extractString(rawString, 343, 350);

        position.exerciseType = extractString(rawString, 351, 358);
        position.underlyingCurrency = extractCurrency(rawString, 359, 366, null);
        position.longExchangeName = extractString(rawString, 367, 396);

        position.contractSize = extractBigDecimal(rawString, 397, 417, null);

        position.convertibleStartDate = extractDate(rawString, 424, 431, null);
        position.bqFaceAmount = extractBigDecimal(rawString, 432, 452, null);
        position.convertionRatio = extractBigDecimal(rawString, 453, 471, null);
        position.commonStockExchange = extractString(rawString, 472, 481);
        position.issuerParentEquityTicker = extractString(rawString, 481, 491);
        position.commonStockPrice = extractBigDecimal(rawString, 492, 506, null);
        position.currentPosition = extractBigDecimal(rawString, 507, 517, null);
        position.primaryExchangeMic = extractString(rawString, 518, 521);
        position.tickerAndExchangeCode = extractString(rawString, 522, 533);
        position.isinNumber = extractString(rawString, 534, 546);

        position.strikePrice = extractBigDecimal(rawString, 546, 564, null);

        position.swapSpread = extractBigDecimal(rawString, 565, 575, null);
        position.receiveFrequency = extractString(rawString, 576, 583);
        position.payFrequency = extractString(rawString, 584, 591);

        // For some reason BB uses a string instead of a char, always 'B'/'S'
        position.customerSide = extractSide(rawString, 592, 595, null);

        position.calculationAgent = extractString(rawString, 596, 625);
        // end of page 1

        position.cdsSide = extractSide(rawString, 626, 629, null);
        position.baseCurrency = extractCurrency(rawString, 630, 633, null);
        position.currencySecurityDescription = extractString(rawString, 634, 663);
        position.forwardPrice = extractBigDecimal(rawString, 664, 680, null);
        position.underlyingReferenceIndex = extractString(rawString, 681, 710);
        position.bloombergSwapCurveName = extractString(rawString, 711, 740);
        position.swapCurveDaysToMaturity = extractBigDecimal(rawString, 741, 747, null);
        position.securityType2 = extractString(rawString, 748, 775);
        position.nextSettlementDate = extractDate(rawString, 776, 783, null);
        position.issuerIndustry = extractString(rawString, 784, 813);
        position.nextCallPrice = extractBigDecimal(rawString, 814, 828, null);

        position.futuresCategory = extractString(rawString, 829, 848);
        position.nextPutPrice = extractBigDecimal(rawString, 849, 863, null);
        position.nextPutDate = extractDate(rawString, 864, 871, null);
        position.pricingModelType = extractString(rawString, 872, 901);
        position.forwardDate = extractDate(rawString, 902, 912, null);
        position.paymentType = extractString(rawString, 913, 942);
        position.bloombergSwapCurveNameRealtime = extractString(rawString, 943, 972);

        position.level1TagName = extractString(rawString, 973, 1022);

        position.interestAccrualDate = extractDate(rawString, 1023, 1030, null);
        position.cusipNumber = extractString(rawString, 1031, 1039);
        position.cheapestToDeliverCoupon = extractBigDecimal(rawString, 1040, 1054, null);
        position.cheapestToDeliverFrequency = extractString(rawString, 1055, 1062);
        position.cheapestToDeliverMaturity = extractDate(rawString, 1063, 1070, null);

        position.level2TagName = extractString(rawString, 1071, 1120);
        position.level3TagName = extractString(rawString, 1121, 1170);
        position.level4TagName = extractString(rawString, 1171, 1220);

        position.countryIsoCode = extractString(rawString, 1241, 1244);
        position.swaptionExperiationDate = extractDate(rawString, 1245, 1252, null);
        position.swapType = extractString(rawString, 1253, 1272);
        position.payFixedOrPayFloat = extractString(rawString, 1273, 1289);
        position.payCoupon = extractBigDecimal(rawString, 1290, 1304, null);
        position.paySideDayCount = extractString(rawString, 1305, 1322);
        position.receiveSideDayCount = extractString(rawString, 1323, 1340);
        position.receiveSideCouponType = extractString(rawString, 1341, 1348);
        position.couponOrSpread = extractBigDecimal(rawString, 1349, 1363, null);
        position.receiveCoupon = extractBigDecimal(rawString, 1364, 1378, null);

        String ptp = extractString(rawString, 1379, 1389);
        if (ptp.contains("*") || ptp.length() == 0) {
            position.primaryTraderPosition = null; // Number too large or does not exist
        } else {
            position.primaryTraderPosition = new BigDecimal(ptp);
        }


        position.currentLongPosition = extractBigDecimal(rawString, 1390, 1408, null);
        position.currentShortPosition = extractBigDecimal(rawString, 1409, 1427, null);
        position.fullCurrentNetPositionWithoutComma = extractBigDecimal(rawString, 1428, 1442, null);

        if (rawString.length() > 1443) {
            // 1443 seems to be the limit
            position.tradedByCurrency = extractCurrency(rawString, 1443, 1446, null);
            position.couponType = extractString(rawString, 1447, 1470);
            // Bloomberg ID is not in the production version
            //position.bloombergId = extractString(rawString, 1471, 1479);
        }

        return position;
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


    String syntheticId() {
        StringBuilder key = new StringBuilder(50);

        if (securityIdFlag == BBSecurityIDFlag.Cusip || securityIdFlag == BBSecurityIDFlag.Isin) {
            key.append(securityId);
        } else {
            key.append(ticker);
        }

        if (strikePrice != null) {
            key.append(" ").append(String.format("%.2f", strikePrice));
        }

        // This is probably wrong, but it works  (Futures && empty sting)
        if (!"Options".equals(subsector)) {
            if (putOrCall != null) {
                key.append(" ").append(putOrCall);
            }
            if (expirationDate != null) {
                key.append(" ").append(expirationDate.toString("MM/dd/yy"));
            }
        }
        return key.toString().trim();
        // Jun's logic
//        String positionName = underlyingCurrency + " " + currency + " " + putOrCall + " " + strikePrice + " "
//                + expirationDate;

//        return positionName;

    }

    /**
     * Build our own identifier as Bloomberg does not provide a unique key between the
     * batch position file and the online position feed.
     *
     * @return
     */
    public String buildOnlineId() {
        StringBuilder key = new StringBuilder(50);

        key.append(securityId);

        if (strikePrice != null) {
            key.append(" ").append(String.format("%.2f", strikePrice));
        }

        return key.toString().trim();

    }

    public String toTextMessage(String delimiter) {
        StringBuilder sb = new StringBuilder(1500);

        String onlineId = buildOnlineId();

        sb.append("batchFileDate=").append(batchFileDate);
        sb.append(delimiter).append("securityIdFlag=").append(securityIdFlag);
        sb.append(delimiter).append("securityId=").append(securityId);
        sb.append(delimiter).append("ticker=").append(emptyStringOrValue(ticker));
        sb.append(delimiter).append("strikePrice=").append(emptyStringOrValue(strikePrice));
        sb.append(delimiter).append("identifier=").append(emptyStringOrValue(identifier));

        sb.append(delimiter).append("onlineId=").append(onlineId);

        sb.append(delimiter).append("account=").append(emptyStringOrValue(account));
        sb.append(delimiter).append("level1TagName=").append(emptyStringOrValue(level1TagName));
        sb.append(delimiter).append("level2TagName=").append(emptyStringOrValue(level2TagName));
        sb.append(delimiter).append("level3TagName=").append(emptyStringOrValue(level3TagName));
        sb.append(delimiter).append("level4TagName=").append(emptyStringOrValue(level4TagName));


        sb.append(delimiter).append("productCode=").append(productCode);

        sb.append(delimiter).append("name=").append(emptyStringOrValue(name));

        sb.append(delimiter).append("fullCurrentNetPosition=").append(emptyStringOrValue(fullCurrentNetPosition));
        sb.append(delimiter).append("currency=").append(emptyStringOrValue(currency));

        sb.append(delimiter).append("coupon=").append(emptyStringOrValue(coupon));
        sb.append(delimiter).append("couponFrequency=").append(emptyStringOrValue(couponFrequency));
        sb.append(delimiter).append("firstCouponDate=").append(emptyStringOrValue(firstCouponDate));
        sb.append(delimiter).append("maturityDate=").append(emptyStringOrValue(maturityDate));

        sb.append(delimiter).append("markToMarketPrice=").append(emptyStringOrValue(markToMarketPrice));

        sb.append(delimiter).append("transactionCounterparty=").append(emptyStringOrValue(transactionCounterparty));


        sb.append(delimiter).append("subsector=").append(emptyStringOrValue(subsector));
        sb.append(delimiter).append("previousClosePrice=").append(emptyStringOrValue(previousClosePrice));
        sb.append(delimiter).append("countryFullName=").append(emptyStringOrValue(countryFullName));

        sb.append(delimiter).append("dayCount=").append(emptyStringOrValue(dayCount));
        sb.append(delimiter).append("dlvMinimumMaturityDate=").append(emptyStringOrValue(dlvMinimumMaturityDate));
        sb.append(delimiter).append("underlyingOptionsPrice=").append(emptyStringOrValue(underlyingOptionsPrice));
        sb.append(delimiter).append("underlyingExpirationDate=").append(emptyStringOrValue(underlyingExpirationDate));
        sb.append(delimiter).append("conversionFactor=").append(emptyStringOrValue(conversionFactor));

        sb.append(delimiter).append("numberOfContracts=").append(emptyStringOrValue(numberOfContracts));
        sb.append(delimiter).append("expirationDate=").append(emptyStringOrValue(expirationDate));

        sb.append(delimiter).append("financeRate=").append(emptyStringOrValue(financeRate));
        sb.append(delimiter).append("lastTradeDate=").append(emptyStringOrValue(lastTradeDate));
        sb.append(delimiter).append("putOrCall=").append(emptyStringOrValue(putOrCall));

        sb.append(delimiter).append("exerciseType=").append(emptyStringOrValue(exerciseType));
        sb.append(delimiter).append("underlyingCurrency=").append(emptyStringOrValue(underlyingCurrency));
        sb.append(delimiter).append("longExchangeName=").append(emptyStringOrValue(longExchangeName));

        sb.append(delimiter).append("contractSize=").append(emptyStringOrValue(contractSize));

        sb.append(delimiter).append("convertibleStartDate=").append(emptyStringOrValue(convertibleStartDate));
        sb.append(delimiter).append("bqFaceAmount=").append(emptyStringOrValue(bqFaceAmount));
        sb.append(delimiter).append("convertionRatio=").append(emptyStringOrValue(convertionRatio));
        sb.append(delimiter).append("commonStockExchange=").append(emptyStringOrValue(commonStockExchange));
        sb.append(delimiter).append("issuerParentEquityTicker=").append(emptyStringOrValue(issuerParentEquityTicker));
        sb.append(delimiter).append("commonStockPrice=").append(emptyStringOrValue(commonStockPrice));
        sb.append(delimiter).append("currentPositionBatch=").append(emptyStringOrValue(currentPosition));
        sb.append(delimiter).append("primaryExchangeMic=").append(emptyStringOrValue(primaryExchangeMic));
        sb.append(delimiter).append("tickerAndExchangeCode=").append(emptyStringOrValue(tickerAndExchangeCode));
        sb.append(delimiter).append("isinNumber=").append(emptyStringOrValue(isinNumber));


        sb.append(delimiter).append("swapSpread=").append(emptyStringOrValue(swapSpread));
        sb.append(delimiter).append("receiveFrequency=").append(emptyStringOrValue(receiveFrequency));
        sb.append(delimiter).append("payFrequency=").append(emptyStringOrValue(payFrequency));

        sb.append(delimiter).append("customerSide=").append(emptyStringOrValue(customerSide));

        sb.append(delimiter).append("calculationAgent=").append(emptyStringOrValue(calculationAgent));

        sb.append(delimiter).append("cdsSide=").append(emptyStringOrValue(cdsSide));
        sb.append(delimiter).append("baseCurrency=").append(emptyStringOrValue(baseCurrency));
        sb.append(delimiter).append("currencySecurityDescription=").append(emptyStringOrValue(currencySecurityDescription));
        sb.append(delimiter).append("forwardPrice=").append(emptyStringOrValue(forwardPrice));
        sb.append(delimiter).append("underlyingReferenceIndex=").append(emptyStringOrValue(underlyingReferenceIndex));
        sb.append(delimiter).append("bloombergSwapCurveName=").append(emptyStringOrValue(bloombergSwapCurveName));
        sb.append(delimiter).append("swapCurveDaysToMaturity=").append(emptyStringOrValue(swapCurveDaysToMaturity));
        sb.append(delimiter).append("securityType2=").append(emptyStringOrValue(securityType2));
        sb.append(delimiter).append("nextSettlementDate=").append(emptyStringOrValue(nextSettlementDate));
        sb.append(delimiter).append("issuerIndustry=").append(emptyStringOrValue(issuerIndustry));
        sb.append(delimiter).append("nextCallPrice=").append(emptyStringOrValue(nextCallPrice));
        sb.append(delimiter).append("futuresCategory=").append(emptyStringOrValue(futuresCategory));
        sb.append(delimiter).append("nextPutPrice=").append(emptyStringOrValue(nextPutPrice));
        sb.append(delimiter).append("nextPutDate=").append(emptyStringOrValue(nextPutDate));
        sb.append(delimiter).append("pricingModelType=").append(emptyStringOrValue(pricingModelType));
        sb.append(delimiter).append("forwardDate=").append(emptyStringOrValue(forwardDate));
        sb.append(delimiter).append("paymentType=").append(emptyStringOrValue(paymentType));
        sb.append(delimiter).append("bloombergSwapCurveNameRealtime=").append(emptyStringOrValue(bloombergSwapCurveNameRealtime));


        sb.append(delimiter).append("interestAccrualDate=").append(emptyStringOrValue(interestAccrualDate));
        sb.append(delimiter).append("cusipNumber=").append(emptyStringOrValue(cusipNumber));
        sb.append(delimiter).append("cheapestToDeliverCoupon=").append(emptyStringOrValue(cheapestToDeliverCoupon));

        sb.append(delimiter).append("cheapestToDeliverFrequency=").append(emptyStringOrValue(cheapestToDeliverFrequency));
        sb.append(delimiter).append("cheapestToDeliverMaturity=").append(emptyStringOrValue(cheapestToDeliverMaturity));


        sb.append(delimiter).append("countryIsoCode=").append(emptyStringOrValue(countryIsoCode));
        sb.append(delimiter).append("swaptionExperiationDate=").append(emptyStringOrValue(swaptionExperiationDate));
        sb.append(delimiter).append("swapType=").append(emptyStringOrValue(swapType));
        sb.append(delimiter).append("payFixedOrPayFloat=").append(emptyStringOrValue(payFixedOrPayFloat));
        sb.append(delimiter).append("payCoupon=").append(emptyStringOrValue(payCoupon));
        sb.append(delimiter).append("paySideDayCount=").append(emptyStringOrValue(paySideDayCount));
        sb.append(delimiter).append("receiveSideDayCount=").append(emptyStringOrValue(receiveSideDayCount));
        sb.append(delimiter).append("receiveSideCouponType=").append(emptyStringOrValue(receiveSideCouponType));
        sb.append(delimiter).append("couponOrSpread=").append(emptyStringOrValue(couponOrSpread));
        sb.append(delimiter).append("receiveCoupon=").append(emptyStringOrValue(receiveCoupon));

        sb.append(delimiter).append("primaryTraderPosition=").append(emptyStringOrValue(primaryTraderPosition));

        sb.append(delimiter).append("currentLongPosition=").append(emptyStringOrValue(currentLongPosition));
        sb.append(delimiter).append("currentShortPosition=").append(emptyStringOrValue(currentShortPosition));
        sb.append(delimiter).append("fullCurrentNetPositionWithoutComma=").append(emptyStringOrValue(fullCurrentNetPositionWithoutComma));
        sb.append(delimiter).append("tradedByCurrency=").append(emptyStringOrValue(tradedByCurrency));
        sb.append(delimiter).append("couponType=").append(emptyStringOrValue(couponType));
        sb.append(delimiter).append("bloombergId=").append(emptyStringOrValue(bloombergId));

        return sb.toString();
    }

    public String toString() {
//        if (syntheticId.equals(identifier)) {
//            System.out.println("Matched");
//        } else {
//            System.out.println("No-match");
//        }

        return toTextMessage(", ");
    }

    public BBSecurityIDFlag getSecurityIdFlag() {
        return securityIdFlag;
    }

    public String getSecurityId() {
        return securityId;
    }

    public BBProductCode getProductCode() {
        return productCode;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public BigDecimal getFullCurrentNetPosition() {
        return fullCurrentNetPosition;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getCoupon() {
        return coupon;
    }

    public BigDecimal getCouponFrequency() {
        return couponFrequency;
    }

    public LocalDate getFirstCouponDate() {
        return firstCouponDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public BigDecimal getMarkToMarketPrice() {
        return markToMarketPrice;
    }

    public String getTransactionCounterparty() {
        return transactionCounterparty;
    }

    public String getAccount() {
        return account;
    }

    public String getSubsector() {
        return subsector;
    }

    public BigDecimal getPreviousClosePrice() {
        return previousClosePrice;
    }

    public String getCountryFullName() {
        return countryFullName;
    }

    public BigDecimal getDayCount() {
        return dayCount;
    }

    public LocalDate getDlvMinimumMaturityDate() {
        return dlvMinimumMaturityDate;
    }

    public BigDecimal getUnderlyingOptionsPrice() {
        return underlyingOptionsPrice;
    }

    public LocalDate getUnderlyingExpirationDate() {
        return underlyingExpirationDate;
    }

    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }

    public String getNumberOfContracts() {
        return numberOfContracts;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public BigDecimal getFinanceRate() {
        return financeRate;
    }

    public LocalDate getLastTradeDate() {
        return lastTradeDate;
    }

    public String getPutOrCall() {
        return putOrCall;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public Currency getUnderlyingCurrency() {
        return underlyingCurrency;
    }

    public String getLongExchangeName() {
        return longExchangeName;
    }

    public BigDecimal getContractSize() {
        return contractSize;
    }

    public LocalDate getConvertibleStartDate() {
        return convertibleStartDate;
    }

    public BigDecimal getBqFaceAmount() {
        return bqFaceAmount;
    }

    public BigDecimal getConvertionRatio() {
        return convertionRatio;
    }

    public String getCommonStockExchange() {
        return commonStockExchange;
    }

    public String getIssuerParentEquityTicker() {
        return issuerParentEquityTicker;
    }

    public BigDecimal getCommonStockPrice() {
        return commonStockPrice;
    }

    public BigDecimal getCurrentPosition() {
        return currentPosition;
    }

    public String getPrimaryExchangeMic() {
        return primaryExchangeMic;
    }

    public String getTickerAndExchangeCode() {
        return tickerAndExchangeCode;
    }

    public String getIsinNumber() {
        return isinNumber;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public BigDecimal getSwapSpread() {
        return swapSpread;
    }

    public String getReceiveFrequency() {
        return receiveFrequency;
    }

    public String getPayFrequency() {
        return payFrequency;
    }

    public BBSide getCustomerSide() {
        return customerSide;
    }

    public String getCalculationAgent() {
        return calculationAgent;
    }

    public BBSide getCdsSide() {
        return cdsSide;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public String getCurrencySecurityDescription() {
        return currencySecurityDescription;
    }

    public BigDecimal getForwardPrice() {
        return forwardPrice;
    }

    public String getUnderlyingReferenceIndex() {
        return underlyingReferenceIndex;
    }

    public String getBloombergSwapCurveName() {
        return bloombergSwapCurveName;
    }

    public BigDecimal getSwapCurveDaysToMaturity() {
        return swapCurveDaysToMaturity;
    }

    public String getSecurityType2() {
        return securityType2;
    }

    public LocalDate getNextSettlementDate() {
        return nextSettlementDate;
    }

    public String getIssuerIndustry() {
        return issuerIndustry;
    }

    public BigDecimal getNextCallPrice() {
        return nextCallPrice;
    }

    public String getFuturesCategory() {
        return futuresCategory;
    }

    public BigDecimal getNextPutPrice() {
        return nextPutPrice;
    }

    public LocalDate getNextPutDate() {
        return nextPutDate;
    }

    public String getPricingModelType() {
        return pricingModelType;
    }

    public LocalDate getForwardDate() {
        return forwardDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getBloombergSwapCurveNameRealtime() {
        return bloombergSwapCurveNameRealtime;
    }

    public String getLevel1TagName() {
        return level1TagName;
    }

    public LocalDate getInterestAccrualDate() {
        return interestAccrualDate;
    }

    public String getCusipNumber() {
        return cusipNumber;
    }

    public BigDecimal getCheapestToDeliverCoupon() {
        return cheapestToDeliverCoupon;
    }

    public String getCheapestToDeliverFrequency() {
        return cheapestToDeliverFrequency;
    }

    public LocalDate getCheapestToDeliverMaturity() {
        return cheapestToDeliverMaturity;
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

    public String getCountryIsoCode() {
        return countryIsoCode;
    }

    public LocalDate getSwaptionExperiationDate() {
        return swaptionExperiationDate;
    }

    public String getSwapType() {
        return swapType;
    }

    public String getPayFixedOrPayFloat() {
        return payFixedOrPayFloat;
    }

    public BigDecimal getPayCoupon() {
        return payCoupon;
    }

    public String getPaySideDayCount() {
        return paySideDayCount;
    }

    public String getReceiveSideDayCount() {
        return receiveSideDayCount;
    }

    public String getReceiveSideCouponType() {
        return receiveSideCouponType;
    }

    public BigDecimal getCouponOrSpread() {
        return couponOrSpread;
    }

    public BigDecimal getReceiveCoupon() {
        return receiveCoupon;
    }

    public BigDecimal getPrimaryTraderPosition() {
        return primaryTraderPosition;
    }

    public BigDecimal getCurrentLongPosition() {
        return currentLongPosition;
    }

    public BigDecimal getCurrentShortPosition() {
        return currentShortPosition;
    }

    public BigDecimal getFullCurrentNetPositionWithoutComma() {
        return fullCurrentNetPositionWithoutComma;
    }

    public Currency getTradedByCurrency() {
        return tradedByCurrency;
    }

    public String getCouponType() {
        return couponType;
    }

    public String getBloombergId() {
        return bloombergId;
    }

    public LocalDate getBatchFileDate() {
        return batchFileDate;
    }


    public BatchPosition aggregate(BatchPosition addend) {
        if (!aggregatable(addend)) {
            throw new IllegalArgumentException("BatchPositions cannot be aggregated, keys do not match");
        }
        BatchPosition augend = copy();

//        augend.setFullCurrentNetPosition(add(augend.getFullCurrentNetPosition(),addend.getFullCurrentNetPosition()));
//        augend.setFullCurrentNetPositionWithoutComma(add(augend.getFullCurrentNetPositionWithoutComma(),addend.getFullCurrentNetPositionWithoutComma()));
        augend.setCurrentLongPosition(add(augend.getCurrentLongPosition(), addend.getCurrentLongPosition()));
        augend.setCurrentShortPosition(add(augend.getCurrentShortPosition(), addend.getCurrentShortPosition()));

        return augend;
    }


    /**
     * Ensure that the keys are the same before we try to aggregate.
     *
     * @param other
     * @return
     */
    public boolean aggregatable(BatchPosition other) {
        return securityId.equals(other.securityId) && account.equals(other.account)
                && level1TagName.equals(other.level1TagName) && level2TagName.equals(other.level2TagName)
                && level3TagName.equals(other.level3TagName) && level4TagName.equals(other.level4TagName);
    }

    private BatchPosition copy() {
        // Create a base with immutable constructor fields
        BatchPosition localCopy = new BatchPosition(batchFileDate, securityId, account, level1TagName, level2TagName,
                level3TagName, level4TagName);

        localCopy.securityIdFlag = securityIdFlag;
        localCopy.productCode = productCode;

        localCopy.ticker = ticker;
        localCopy.name = name;
        localCopy.identifier = identifier;

        localCopy.fullCurrentNetPosition = fullCurrentNetPosition;
        localCopy.currency = currency;
        localCopy.coupon = coupon;
        localCopy.couponFrequency = couponFrequency;
        localCopy.firstCouponDate = firstCouponDate;
        localCopy.maturityDate = maturityDate;

        localCopy.markToMarketPrice = markToMarketPrice;

        localCopy.transactionCounterparty = transactionCounterparty;

        localCopy.account = account;
        localCopy.subsector = subsector;

        localCopy.previousClosePrice = previousClosePrice;
        localCopy.countryFullName = countryFullName;
        localCopy.dayCount = dayCount;
        localCopy.dlvMinimumMaturityDate = dlvMinimumMaturityDate;
        localCopy.underlyingOptionsPrice = underlyingOptionsPrice;
        localCopy.underlyingExpirationDate = underlyingExpirationDate;
        localCopy.conversionFactor = conversionFactor;
        localCopy.numberOfContracts = numberOfContracts;
        localCopy.expirationDate = expirationDate;
        localCopy.financeRate = financeRate;

        localCopy.lastTradeDate = lastTradeDate;
        localCopy.putOrCall = putOrCall;

        localCopy.exerciseType = exerciseType;
        localCopy.underlyingCurrency = underlyingCurrency;
        localCopy.longExchangeName = longExchangeName;

        localCopy.contractSize = contractSize;

        localCopy.convertibleStartDate = convertibleStartDate;
        localCopy.bqFaceAmount = bqFaceAmount;
        localCopy.convertionRatio = convertionRatio;
        localCopy.commonStockExchange = commonStockExchange;
        localCopy.issuerParentEquityTicker = issuerParentEquityTicker;
        localCopy.commonStockPrice = commonStockPrice;
        localCopy.currentPosition = currentPosition;
        localCopy.primaryExchangeMic = primaryExchangeMic;
        localCopy.tickerAndExchangeCode = tickerAndExchangeCode;
        localCopy.isinNumber = isinNumber;

        localCopy.strikePrice = strikePrice;

        localCopy.swapSpread = swapSpread;
        localCopy.receiveFrequency = receiveFrequency;
        localCopy.payFrequency = payFrequency;

        // For some reason BB uses a string instead of a char, always 'B'/'S'
        localCopy.customerSide = customerSide;

        localCopy.calculationAgent = calculationAgent;
        // end of page 1

        localCopy.cdsSide = cdsSide;
        localCopy.baseCurrency = baseCurrency;
        localCopy.currencySecurityDescription = currencySecurityDescription;
        localCopy.forwardPrice = forwardPrice;
        localCopy.underlyingReferenceIndex = underlyingReferenceIndex;
        localCopy.bloombergSwapCurveName = bloombergSwapCurveName;
        localCopy.swapCurveDaysToMaturity = swapCurveDaysToMaturity;
        localCopy.securityType2 = securityType2;
        localCopy.nextSettlementDate = nextSettlementDate;
        localCopy.issuerIndustry = issuerIndustry;
        localCopy.nextCallPrice = nextCallPrice;

        localCopy.futuresCategory = futuresCategory;
        localCopy.nextPutPrice = nextPutPrice;
        localCopy.nextPutDate = nextPutDate;
        localCopy.pricingModelType = pricingModelType;
        localCopy.forwardDate = forwardDate;
        localCopy.paymentType = paymentType;
        localCopy.bloombergSwapCurveNameRealtime = bloombergSwapCurveNameRealtime;

        localCopy.interestAccrualDate = interestAccrualDate;
        localCopy.cusipNumber = cusipNumber;
        localCopy.cheapestToDeliverCoupon = cheapestToDeliverCoupon;
        localCopy.cheapestToDeliverFrequency = cheapestToDeliverFrequency;
        localCopy.cheapestToDeliverMaturity = cheapestToDeliverMaturity;

        localCopy.countryIsoCode = countryIsoCode;
        localCopy.swaptionExperiationDate = swaptionExperiationDate;
        localCopy.swapType = swapType;
        localCopy.payFixedOrPayFloat = payFixedOrPayFloat;
        localCopy.payCoupon = payCoupon;
        localCopy.paySideDayCount = paySideDayCount;
        localCopy.receiveSideDayCount = receiveSideDayCount;
        localCopy.receiveSideCouponType = receiveSideCouponType;
        localCopy.couponOrSpread = couponOrSpread;
        localCopy.receiveCoupon = receiveCoupon;

        localCopy.primaryTraderPosition = primaryTraderPosition;

        localCopy.currentLongPosition = currentLongPosition;
        localCopy.currentShortPosition = currentShortPosition;
        localCopy.fullCurrentNetPositionWithoutComma = fullCurrentNetPositionWithoutComma;

        localCopy.tradedByCurrency = tradedByCurrency;
        localCopy.couponType = couponType;

        return localCopy;
    }

    public static Filter<BatchPosition> createFilter(Map<String, String> filteredFields) {
        return new BatchPositionDynamicFieldFilter(filteredFields);
    }

    /**
     * This could be alot more generic, but it seems over-kill for one field
     */
    private static class BatchPositionDynamicFieldFilter implements Filter<BatchPosition> {

        private String[] supportedFields = {"account"};

        private boolean checkAccount = false;
        private String accountValue;

        /**
         * More logic than needed, but this was originally going to be able to handle
         * generic field selection
         *
         * @param requestedFieldMap
         */
        BatchPositionDynamicFieldFilter(Map<String, String> requestedFieldMap) {

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


        /**
         * Compare this item with the account field, if set.
         * <p/>
         * We only check one field now.  If we are checking the field and the fields
         * are equal return true, otherwise false.
         * <p/>
         * If we are not checking the fields, return true
         *
         * @param item
         * @return
         */
        public boolean accept(BatchPosition item) {

            if (checkAccount) {
                return item.getAccount().equals(accountValue);
            }

            return true;
        }
    }

}
