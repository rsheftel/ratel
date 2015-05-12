package com.fftw.bloomberg.util;

import java.util.HashMap;
import java.util.Map;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;

public class CmfRecordFormatter
{

    private static final Map<Integer, String> securityFlag = new HashMap<Integer, String>();

    private static final Map<Integer, String> errorMessages = new HashMap<Integer, String>();

    static
    {
        initializeMaps();
    }

    private static void initializeMaps ()
    {
        initializeSecurityFlag();
        initializeErrorMessages();
    }

    private static void initializeSecurityFlag ()
    {
        securityFlag.put(94, "FX Ticker");
        securityFlag.put(97, "Money Market Ticker");
        securityFlag.put(98, "Equity Ticker");
        securityFlag.put(99, "Future/Option Ticker");
    }

    private static void initializeErrorMessages ()
    {
        // -1 is the initial value, no message for it
        errorMessages.put(-1, "");
        errorMessages.put(0, "No error - trade entered successfully.");
        errorMessages.put(1, "Trade could not be entered. (generic failure)");
        errorMessages.put(2, "Duplicate TSN received.");
        errorMessages.put(3, "Required field not filled.");
        errorMessages.put(4, "System error while logging trade.");
        errorMessages.put(5, "Unknown security.");
        errorMessages.put(6, "Security type not supported (e.g., swap).");
        errorMessages.put(7, "Unknown trader name tradername (trade record).");
        errorMessages.put(8, "Incorrect pricing number pnum (header record).");
        errorMessages.put(9, "Bad pydflag (trade record).");
        errorMessages.put(10, "Bad side (trade record).");
        errorMessages.put(11, "Trader/book is inappropriate for trade or security.");
        errorMessages.put(12, "Calculation error.");
        errorMessages.put(13, "Unknown account name account (trade record).");
        errorMessages.put(14, "Bad trade date tradedate (trade record).");
        errorMessages.put(15, "Specified userno not associated with firm (trade record).");
        errorMessages.put(16, "Firm has been turned off for trading.");
        errorMessages.put(17,
            "Ticketnum being canceled/corrected was not found (cancels/corrects only)");
        errorMessages.put(18,
            "Trade associated with ticketnum has previously been canceled (cancels/corrects only)");
        errorMessages.put(19,
            "Bad sttlexchcode, or sttlexchrate provided without an accompanying sttlexchcode.");
        errorMessages.put(20, "Transaction cost error - See errormsg field for specifics");
        errorMessages.put(21, "Secid, tradername, or side changed in this correction.");
        errorMessages.put(22, "Trade will short the trader (if the trader has a short block)");
        errorMessages.put(23, "Bad settlement location code");
        errorMessages.put(24, "Bad salesname (for slates)");
        errorMessages.put(25,
            "Quantity on allocation ticket exceeds remaining quantity on PM master ticket");
    }

    /**
     * Format the trade record to be used within an email.
     * 
     * @param tradeRecord
     * @return
     */
    public static String formatForEmail (CmfTradeRecord tradeRecord)
    {
        StringBuilder sb = new StringBuilder(1000);

        sb.append("tradeSequenceNumber=").append(tradeRecord.getTradeSeqNum());
        sb.append("\nstatus=").append(tradeRecord.getStatus());
        sb.append("\nsecurityIdFlag=").append(securityFlagToText(tradeRecord.getSecurityIdFlag()));
        sb.append(" (").append(tradeRecord.getSecurityIdFlag()).append(")");
        sb.append("\nsecurity=").append(tradeRecord.getSecurityId());
        sb.append("\nproductCode=").append(tradeRecord.getProductCode());
        sb.append("\ntradingStrategy=").append(tradeRecord.getTradingStrategy());
        sb.append("\nbroker=").append(tradeRecord.getBroker());
        sb.append("\ntradingPlatform=").append(tradeRecord.getTradingPlatform().getLongText());
        sb.append("\naccount=").append(tradeRecord.getAccount());
        sb.append("\nside=").append(tradeRecord.getSide() == 1 ? "sell" : "buy");
        sb.append("\nquantity=").append(tradeRecord.getQuantity());
        sb.append("\nprice=").append(tradeRecord.getPriceQuote());
        sb.append("\nsettleDate=").append(tradeRecord.getSettleDate());
        sb.append("\nprimeBroker=").append(
            tradeRecord.getPrimeBroker() == null ? "" : tradeRecord.getPrimeBroker());
        sb.append("\nsourceCode=").append(tradeRecord.getSourceCode());
        sb.append("\ntransactionCode=").append(
            tradeRecord.getTransactionCode() == null ? "" : tradeRecord.getTransactionCode());
        sb.append("\nreturnCode=").append(tradeRecord.getReturnCode());
        // Sometimes the error message is returned, otherwise use the lookup
        // value
        sb.append("\nerrorMessage=").append(
            isSet(tradeRecord.getErrorMsg()) ? tradeRecord.getErrorMsg() : returnCodeText(tradeRecord.getReturnCode()));

        return sb.toString();
    }

    private static boolean isSet(String str) {
        return str != null && str.trim().length() > 0;
    }
    private static String securityFlagToText (int securityIdFlag)
    {
        String text = CmfRecordFormatter.securityFlag.get(securityIdFlag);

        if (text == null)
        {
            return "Uknown";
        }

        return text;
    }

    private static String returnCodeText (int returnCode)
    {
        String textMsg = errorMessages.get(returnCode);

        if (textMsg == null)
        {
            return "Unknown message";
        }

        return textMsg;
    }
}
