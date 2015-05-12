package com.fftw.bloomberg.cmfp;

import static com.fftw.bloomberg.util.FixedWidthFormatter.formatNumber;
import static com.fftw.bloomberg.util.FixedWidthFormatter.formatString;
import static com.fftw.util.datetime.DateTimeUtil.getDateAsString;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.FixedWidthFormatter;

/**
 * TradeRecord for AIM/Hedge Funds
 * 
 * This record is persisted to the database using an <tt>identity</tt> column
 * with a unique index on <code>creationDate</code>,
 * <code>tradeSequenceNumber</code>.
 * 
 */
public class CmfAimTradeRecord implements CmfTradeRecord
{
    /**
     * 
     */
    private static final long serialVersionUID = -545735850293510633L;

    public static final int FIXED_LENGTH = 1000;

    /**
     * This field is not sent, but is used for internal logic. It should be
     * insync with the header record.
     */
    String messageType;

    // Only required fields for tradeStatus 1 are defined.
    private int tradeSeqNum;

    private int status; // 1=New Trade;3=Cancel;5=Correct

    private int securityIdFlag; // Security type

    private String securityId;

    private String tradingStrategy;

    private String traderName; // This is actually account - not used

    private int side; // 1=sell;2=buy

    private double quantity; // N18.2

    private int priceQuoteDisplay; // 1=price;2=yield;3=discount...

    private BigDecimal priceQuote = BigDecimal.ZERO; // Type depends on

    // priceQuoteDisplay

    private String settleDate;

    private String account; // this is the broker field, moved

    private String broker;

    private int productCode;

    private int expandedStatus;

    private int shortSale;

    private String primeBroker;

    private String transactionCode;

    private int sourceCode;

    private int returnCode = -1; // We do not send, only receive

    private String errorMsg; // We do not send, only receive

    private int ticketNumber;

    /**
     * This is mapped to long note 4
     */
    private TradingPlatform tradingPlatform;

    /*
     * Malbec custom field that is not sent to Bloomberg
     */
    private String executionID;

    /**
     * This should be the same as trade date, but is used by the database
     */
    private LocalDate creationDate = new LocalDate(); // Use as a

    // java.sql.Date

    private DateTime receiptTimestamp;

    private DateTime acceptRejectTimestamp;

    private CmfAimTradeRecord ()
    {

    }

    public CmfAimTradeRecord (String messageType)
    {
        this.messageType = messageType;
    }

    public String getProtocolMessage ()
    {
        return getProtocolMessageAim();
        // return getProtocolMessageRegular();
    }

    /**
     * Return the normal trade record format
     */
    public String getProtocolMessageRegular ()
    {
        // Build a fixed width record padding any empty/unset fields
        // unset fields are filled with spaces, regardless of the type
        // The buffer gets doubled when we manipulate it,
        StringBuilder sb = new StringBuilder(FIXED_LENGTH * 2);
        for (int i = 0; i < FIXED_LENGTH / CmfConstants.SPACES_50.length(); i++)
        {
            sb.append(CmfConstants.SPACES_50);
        }

        // replace the spaces with actual data
        sb.replace(0, 8, formatNumber(tradeSeqNum, 8));
        sb.replace(16, 17, formatNumber(status, 1));
        sb.replace(17, 19, formatNumber(securityIdFlag, 2));
        // this is where the AIM format is different from the standard record
        sb.replace(19, 69, formatString(securityId, 50));
        sb.replace(77, 85, formatString(traderName, 8));
        sb.replace(85, 86, formatNumber(side, 1));
        sb.replace(86, 106, formatNumber(quantity, 18, 2));
        sb.replace(106, 107, formatNumber(priceQuoteDisplay, 1));
        sb.replace(107, 122, formatNumber(priceQuote, 7, 8));
        sb.replace(122, 130, formatString(settleDate, 8));
        sb.replace(130, 140, formatString(account, 10));
        // this is where the AIM format is different from the standard record
        sb.replace(622, 646, formatString(securityId, 24));
        sb.replace(771, 781, formatString(primeBroker, 10));
        sb.replace(901, 907, formatNumber(sourceCode, 6));
        sb.replace(898, 899, formatString(transactionCode, 1));

        // if generating a Trade Accept/Reject include the return code
        if (CmfConstants.ACCEPT_REJECT.equals(messageType))
        {
            sb.replace(956, 960, formatNumber(returnCode, 4));
            sb.replace(960, 1000, formatString(errorMsg, 40));

        }
        sb.setLength(FIXED_LENGTH);
        return sb.toString();
    }

    public String getProtocolMessageAim ()
    {
        // Build a fixed width record padding any empty/unset fields
        // unset fields are filled with spaces, regardless of the type
        // The buffer gets doubled when we manipulate it,
        StringBuilder sb = new StringBuilder(FIXED_LENGTH * 2);
        for (int i = 0; i < FIXED_LENGTH / CmfConstants.SPACES_50.length(); i++)
        {
            sb.append(CmfConstants.SPACES_50);
        }

        // replace the spaces with actual data
        sb.replace(0, 8, formatNumber(tradeSeqNum, 8));
        sb.replace(16, 17, formatNumber(status, 1));
        sb.replace(17, 19, formatNumber(securityIdFlag, 2));
        // this is where the AIM format is different from the standard record
        sb.replace(19, 69, formatString(tradingStrategy, 50));
        // sb.replace(77, 85, formatString(traderName, 8));
        sb.replace(77, 85, formatString(account, 8));
        sb.replace(85, 86, formatNumber(side, 1));
        sb.replace(86, 106, formatNumber(quantity, 18, 2));
        sb.replace(106, 107, formatNumber(priceQuoteDisplay, 1));
        sb.replace(107, 122, formatNumber(priceQuote, 7, 8));
        sb.replace(122, 130, formatString(settleDate, 8));
        // sb.replace(130, 140, formatString(account, 10));
        sb.replace(130, 140, formatString(broker, 10));
        sb.replace(323, 368, formatString(tradingPlatform.getLongText(), 45));
        sb.replace(425, 427, formatNumber(productCode, 2));
        sb.replace(612, 614, formatNumber(expandedStatus, 2));
        sb.replace(646, 647, formatNumber(shortSale, 1));
        // this is where the AIM format is different from the standard record
        sb.replace(622, 646, formatString(securityId, 24));
        sb.replace(771, 781, formatString(primeBroker, 10));
        sb.replace(901, 907, formatNumber(sourceCode, 6));
        sb.replace(898, 899, formatString(transactionCode, 1));

        sb.replace(948, 956, formatNumber(ticketNumber, 8));

        // if generating a Trade Accept/Reject include the return code
        if (CmfConstants.ACCEPT_REJECT.equals(messageType))
        {
            sb.replace(956, 960, formatNumber(returnCode, 4));
            sb.replace(960, 1000, formatString(errorMsg, 40));
        }
        sb.setLength(FIXED_LENGTH);
        return sb.toString();
    }

    /**
     * Generate a string that is human readable
     */
    public String toString ()
    {
        StringBuilder sb = new StringBuilder(FIXED_LENGTH);
        sb.append("tradeSequenceNumber=").append(tradeSeqNum);
        sb.append(", status=").append(status);
        sb.append(", securityIdFlag=").append(securityIdFlag);
        sb.append(", security=").append(securityId);
        sb.append(", tradingStrategy=").append(tradingStrategy);
        sb.append(", account=").append(account);
        sb.append(", side=").append(side == 1 ? "sell" : "buy");
        sb.append(", quantity=").append(quantity);
        sb.append(", price=").append(priceQuote);
        sb.append(", settleDate=").append(settleDate);
        sb.append(", primeBroker=").append(primeBroker == null ? "" : primeBroker);
        sb.append(", broker=").append(broker);
        sb.append(", tradingPlatform=").append(tradingPlatform);
        sb.append(", sourceCode=").append(sourceCode);
        sb.append(", transactionCode=").append(transactionCode == null ? "" : transactionCode);
        sb.append(", returnCode=").append(returnCode);
        sb.append(", errorMessage=").append(errorMsg == null ? "" : errorMsg);

        return sb.toString();
    }

    public static CmfTradeRecord createFromString (String tradeRecordStr)
    {
        CmfTradeRecord tradeRecord = new CmfAimTradeRecord();

        tradeRecord.setTradeSeqNum(Integer.parseInt(tradeRecordStr.substring(0, 8)));
        tradeRecord.setStatus(Integer.parseInt(tradeRecordStr.substring(16, 17)));
        tradeRecord.setSecurityIdFlag(Integer.parseInt(tradeRecordStr.substring(17, 19)));
        // this is where the AIM format is different from the standard record
        tradeRecord.setTradingStrategy(tradeRecordStr.substring(19, 69).trim());

        // tradeRecord.setTraderName(tradeRecordStr.substring(77, 85).trim());
        tradeRecord.setAccount(tradeRecordStr.substring(77, 85).trim());

        tradeRecord.setSide(Integer.parseInt(tradeRecordStr.substring(85, 86)));
        // Handle the decimal
        tradeRecord.setQuantity(FixedWidthFormatter.parseNumber(tradeRecordStr.substring(86, 106),
            2));
        tradeRecord.setPriceQuoteDisplay(Integer.parseInt(tradeRecordStr.substring(106, 107)));
        // Handle the decimal
        tradeRecord.setPriceQuote(FixedWidthFormatter.parseNumber(tradeRecordStr
            .substring(107, 122), 8));
        tradeRecord.setSettleDate(tradeRecordStr.substring(122, 130));

        // tradeRecord.setAccount(tradeRecordStr.substring(130, 140).trim());
        tradeRecord.setBroker(tradeRecordStr.substring(130, 140).trim());

        tradeRecord.setTradingPlatform(TradingPlatform.valueForLongText(tradeRecordStr.substring(
            323, 368).trim()));

        tradeRecord.setProductCode(Integer.parseInt(tradeRecordStr.substring(425, 427)));
        tradeRecord.setExpandedStatus(Integer.parseInt(tradeRecordStr.substring(612, 614)));

        String shortSellStr = tradeRecordStr.substring(646, 647);
        if (shortSellStr.trim().length() > 0)
        {
            tradeRecord.setShortSale(Integer.parseInt(shortSellStr));
        }

        // this is where the AIM format is different from the standard record
        tradeRecord.setSecurityId(tradeRecordStr.substring(622, 646).trim());
        tradeRecord.setPrimeBroker(tradeRecordStr.substring(771, 781).trim());

        tradeRecord.setTransactionCode(tradeRecordStr.substring(898, 899).trim());
        tradeRecord.setSourceCode(Integer.parseInt(tradeRecordStr.substring(901, 907)));

        String ticketNumber = tradeRecordStr.substring(948, 956).trim();
        if (ticketNumber.length() > 0)
        {
            tradeRecord.setTicketNumber(Integer.parseInt(ticketNumber));
        }

        // These fields are not on all TradeRecords (Trade Accept/Reject)
        String returnCode = tradeRecordStr.substring(956, 960).trim();
        if (returnCode.length() > 0)
        {
            tradeRecord.setReturnCode(Integer.parseInt(returnCode));
            tradeRecord.setMessageType(CmfConstants.ACCEPT_REJECT);
        }

        tradeRecord.setErrorMsg(tradeRecordStr.substring(960, 1000).trim());

        return tradeRecord;
    }

    /* *********************** */
    /* methods for format the data into the fixed width requirements */

    public int getLength ()
    {
        return FIXED_LENGTH;
    }

    public int getTradeSeqNum ()
    {
        return tradeSeqNum;
    }

    public void setTradeSeqNum (int tradeSeqNum)
    {
        this.tradeSeqNum = tradeSeqNum;
    }

    public int getStatus ()
    {
        return status;
    }

    public void setStatus (int status)
    {
        this.status = status;
    }

    public int getSecurityIdFlag ()
    {
        return securityIdFlag;
    }

    public void setSecurityIdFlag (int securityIdFlag)
    {
        this.securityIdFlag = securityIdFlag;
    }

    public String getSecurityId ()
    {
        return securityId;
    }

    public void setSecurityId (String securityId)
    {
        this.securityId = securityId;
    }

    public String getTraderName ()
    {
        return traderName;
    }

    public void setTraderName (String traderName)
    {
        this.traderName = traderName;
    }

    public int getSide ()
    {
        return side;
    }

    public void setSide (int side)
    {
        this.side = side;
    }

    public double getQuantity ()
    {
        return quantity;
    }

    public void setQuantity (double quantity)
    {
        this.quantity = quantity;
    }

    public int getPriceQuoteDisplay ()
    {
        return priceQuoteDisplay;
    }

    public void setPriceQuoteDisplay (int priceQuoteDisplay)
    {
        this.priceQuoteDisplay = priceQuoteDisplay;
    }

    public double getPriceQuote ()
    {
        return priceQuote.doubleValue();
    }

    public void setPriceQuote (BigDecimal priceQuote)
    {
        this.priceQuote = priceQuote;
    }

    public void setPriceQuote (double priceQuote)
    {
        this.priceQuote = BigDecimal.valueOf(priceQuote);
    }

    public String getSettleDate ()
    {
        return settleDate;
    }

    public void setSettleDate (Date settleDate)
    {
        this.settleDate = getDateAsString(settleDate);
    }

    /**
     * Set the settlement date to the specified string.
     * 
     * This checks to ensure the length of the string is 8. Null values are OK
     * 
     */
    public void setSettleDate (String settleDate)
    {
        if (settleDate != null && settleDate.trim().length() == 8)
        {
            this.settleDate = settleDate.trim();
        }
        else if (settleDate == null)
        {
            this.settleDate = null;
        }
    }

    public String getAccount ()
    {
        return account;
    }

    public void setAccount (String account)
    {
        this.account = account;
    }

    public int getSourceCode ()
    {
        return sourceCode;
    }

    public void setSourceCode (int sourceCode)
    {
        this.sourceCode = sourceCode;
    }

    public int getReturnCode ()
    {
        return returnCode;
    }

    public void setReturnCode (int returnCode)
    {
        this.returnCode = returnCode;
    }

    public String getErrorMsg ()
    {
        return errorMsg;
    }

    public void setErrorMsg (String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getMessageType ()
    {
        return messageType;
    }

    public void setMessageType (String msgType)
    {
        this.messageType = msgType;

    }

    public TradingPlatform getTradingPlatform ()
    {
        return tradingPlatform;
    }

    public void setTradingPlatform (TradingPlatform tradingPlatform)
    {
        this.tradingPlatform = tradingPlatform;
    }

    public String getExecutionID ()
    {
        return executionID;
    }

    public void setExecutionID (String executionID)
    {
        this.executionID = executionID;
    }

    public LocalDate getCreationDate ()
    {
        return creationDate;
    }

    public void setCreationDate (LocalDate creationDate)
    {
        this.creationDate = creationDate;
    }

    public DateTime getReceiptTimestamp ()
    {
        return receiptTimestamp;
    }

    public void setReceiptTimestamp (DateTime receiptTimestamp)
    {
        this.receiptTimestamp = receiptTimestamp;
    }

    public DateTime getAcceptRejectTimestamp ()
    {
        return acceptRejectTimestamp;
    }

    public void setAcceptRejectTimestamp (DateTime acceptRejectTimestamp)
    {
        this.acceptRejectTimestamp = acceptRejectTimestamp;
    }

    public String getTradingStrategy ()
    {
        return tradingStrategy;
    }

    public void setTradingStrategy (String tradingStrategy)
    {
        this.tradingStrategy = tradingStrategy;
    }

    public String getPrimeBroker ()
    {
        return primeBroker;
    }

    public void setPrimeBroker (String primeBroker)
    {
        this.primeBroker = primeBroker;
    }

    public String getTransactionCode ()
    {
        return transactionCode;
    }

    public void setTransactionCode (String transactionCode)
    {
        this.transactionCode = transactionCode;
    }

    public int getProductCode ()
    {
        return productCode;
    }

    public void setProductCode (int productCode)
    {
        this.productCode = productCode;
    }

    public int getExpandedStatus ()
    {
        return expandedStatus;
    }

    public void setExpandedStatus (int expandedStatus)
    {
        if (expandedStatus != 0)
        {
            this.expandedStatus = expandedStatus;
            this.status = 9;
        }
    }

    public String getBroker ()
    {
        return broker;
    }

    public void setBroker (String broker)
    {
        this.broker = broker;
    }

    public int getTicketNumber ()
    {
        return ticketNumber;
    }

    public void setTicketNumber (int ticketNumber)
    {
        this.ticketNumber = ticketNumber;
    }

    public int getShortSale ()
    {
        return shortSale;
    }

    public void setShortSale (int shortSale)
    {
        this.shortSale = shortSale;
    }

}
