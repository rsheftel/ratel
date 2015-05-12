package com.fftw.bloomberg.cmfp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.TradingPlatform;

/**
 * Trade Record
 * 
 */
public interface CmfTradeRecord extends Serializable
{

    String getProtocolMessage ();

    void setMessageType (String msgType);

    String getMessageType ();

    int getLength ();

    int getTradeSeqNum ();

    void setTradeSeqNum (int tradeSeqNum);

    int getStatus ();

    void setStatus (int status);

    BBSecurityIDFlag getSecurityIdFlag ();

    void setSecurityIdFlag (BBSecurityIDFlag securityIdFlag);

    String getSecurityId ();

    void setSecurityId (String securityId);

    String getTradingStrategy ();

    void setTradingStrategy (String strategy);

    String getTraderName ();

    void setTraderName (String traderName);

    int getSide ();

    void setSide (int side);

    double getQuantity ();

    void setQuantity (double quantity);

    int getPriceQuoteDisplay ();

    void setPriceQuoteDisplay (int priceQuoteDisplay);

    double getPriceQuote ();

    void setPriceQuote (BigDecimal priceQuote);

    void setPriceQuote (double priceQuote);

    String getSettleDate ();

    void setSettleDate (Date settleDate);

    void setSettleDate (String settleDate);

    String getAccount ();

    void setAccount (String account);

    String getPrimeBroker ();

    void setPrimeBroker (String primeBroker);

    String getBroker ();

    void setBroker (String primeBroker);
    
    String getTransactionCode ();

    void setTransactionCode (String transactionCode);

    int getSourceCode ();

    void setSourceCode (int sourceCode);

    int getReturnCode ();

    void setReturnCode (int returnCode);

    String getErrorMsg ();

    void setErrorMsg (String errorMsg);

    TradingPlatform getTradingPlatform ();

    void setTradingPlatform (TradingPlatform platform);

    String getExecutionID ();

    void setExecutionID (String executionID);

    LocalDate getCreationDate ();

    void setReceiptTimestamp (DateTime dateTime);

    DateTime getReceiptTimestamp ();

    void setAcceptRejectTimestamp (DateTime dateTime);

    DateTime getAcceptRejectTimestamp ();

    BBProductCode getProductCode ();

    void setProductCode (BBProductCode pc);

    int getExpandedStatus ();

    void setExpandedStatus (int eStatus);
    
    int getTicketNumber();
    
    void setTicketNumber(int ticketNumber);

    void setShortSale (int parseInt);

}