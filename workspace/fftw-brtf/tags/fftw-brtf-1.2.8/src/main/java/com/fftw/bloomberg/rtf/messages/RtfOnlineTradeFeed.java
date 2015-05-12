package com.fftw.bloomberg.rtf.messages;

import com.fftw.bloomberg.types.BBFractionIndicator;
import com.fftw.bloomberg.types.BBSide;

import java.util.Currency;

import org.joda.time.LocalDate;

/**
 *
 */
public class RtfOnlineTradeFeed {

    private int securityIdFlag;
    private String securityId;
    private Currency securityCurrency; // ISO code
    private Integer programType;  // TODO find out what this is
    private String bloombergId;
    private String ticker;
    private BBFractionIndicator couponFI;
    private int couponStrikePrice;
    private LocalDate maturityDate;
    private String exchangeCode; // This has multiple description - picking exchange
    private String poolNumber; // this has multiple descriptions
    private BBSide side;  // This is an incomplete list - may not work for OnlinePosition
    private int recordType;
    private LocalDate tradeDate;
    private LocalDate asOfTradeDate;
    private LocalDate settleDate;
    private BBFractionIndicator yieldFI;
    private int yield;
    private BBFractionIndicator discountRateFI;
    private int discountRate;
    private BBFractionIndicator tradeAmountFI;
    private int tradeAmount;
    private String counterParty;
    private String counterPartyShort;
    private String principalFlag;
    private int settlementLocation;
    private String glAccount;
    private int productSubFlag;
    private BBFractionIndicator loanAmountFI;
    private int loanAmount;
    private BBFractionIndicator accruedInterestFI;
    private int accruedInterest;
    private int numberOfDaysAccrued;
    // skipping SEC fees and short notes

    private String longNote1;
    private String longNote2;
    private String longNote3;
    private String longNote4;
    private String accountName;
    private String salesPersonName;
    private String lastLogin; // Trader Name
    // skipping mortgage/good million number/ concession

    private String contractSize;
    private Currency settlementCurrency;
    // Skipping the rest, this may not be necessary for what we need.  It would be good get
    // all of the trade back from AIM and validate with what we sent with CMF

    public int getSecurityIdFlag() {
        return securityIdFlag;
    }

    public String getSecurityId() {
        return securityId;
    }

    public Currency getSecurityCurrency() {
        return securityCurrency;
    }

    public Integer getProgramType() {
        return programType;
    }

    public String getBloombergId() {
        return bloombergId;
    }

    public String getTicker() {
        return ticker;
    }

    public BBFractionIndicator getCouponFI() {
        return couponFI;
    }

    public int getCouponStrikePrice() {
        return couponStrikePrice;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public String getExchangeCode() {
        return exchangeCode;
    }

    public String getPoolNumber() {
        return poolNumber;
    }

    public BBSide getSide() {
        return side;
    }

    public int getRecordType() {
        return recordType;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public LocalDate getAsOfTradeDate() {
        return asOfTradeDate;
    }

    public LocalDate getSettleDate() {
        return settleDate;
    }

    public BBFractionIndicator getYieldFI() {
        return yieldFI;
    }

    public int getYield() {
        return yield;
    }

    public BBFractionIndicator getDiscountRateFI() {
        return discountRateFI;
    }

    public int getDiscountRate() {
        return discountRate;
    }

    public BBFractionIndicator getTradeAmountFI() {
        return tradeAmountFI;
    }

    public int getTradeAmount() {
        return tradeAmount;
    }

    public String getCounterParty() {
        return counterParty;
    }

    public String getCounterPartyShort() {
        return counterPartyShort;
    }

    public String getPrincipalFlag() {
        return principalFlag;
    }

    public int getSettlementLocation() {
        return settlementLocation;
    }

    public String getGlAccount() {
        return glAccount;
    }

    public int getProductSubFlag() {
        return productSubFlag;
    }

    public BBFractionIndicator getLoanAmountFI() {
        return loanAmountFI;
    }

    public int getLoanAmount() {
        return loanAmount;
    }

    public BBFractionIndicator getAccruedInterestFI() {
        return accruedInterestFI;
    }

    public int getAccruedInterest() {
        return accruedInterest;
    }

    public int getNumberOfDaysAccrued() {
        return numberOfDaysAccrued;
    }

    public String getLongNote1() {
        return longNote1;
    }

    public String getLongNote2() {
        return longNote2;
    }

    public String getLongNote3() {
        return longNote3;
    }

    public String getLongNote4() {
        return longNote4;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getSalesPersonName() {
        return salesPersonName;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public String getContractSize() {
        return contractSize;
    }

    public Currency getSettlementCurrency() {
        return settlementCurrency;
    }
}
