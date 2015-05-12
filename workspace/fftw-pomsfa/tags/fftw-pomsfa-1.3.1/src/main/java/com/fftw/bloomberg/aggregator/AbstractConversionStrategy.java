package com.fftw.bloomberg.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.field.ExecID;
import quickfix.field.FutSettDate;
import quickfix.field.LastMkt;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.MaturityMonthYear;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityType;
import quickfix.field.SenderCompID;
import quickfix.field.SendingTime;
import quickfix.field.SettlmntTyp;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfConstants;
import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfMessageFactory;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.CmfRecordFormatter;
import com.fftw.bloomberg.util.Fix2CmfUtil;
import com.fftw.util.Emailer;
import com.fftw.util.datetime.DateTimeUtil;
import com.fftw.util.settlement.SettlementDate;
import com.fftw.util.settlement.SettlementUtil;
import com.fftw.util.settlement.calendar.MarketCalendar;

/**
 * This is the base strategy for converting FIX drop copies into Bloomberg CMF
 * objects.
 * 
 * 
 */
public abstract class AbstractConversionStrategy implements ConversionStrategy
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final SenderCompID TRADESTATION = new SenderCompID("TRAD");

    private static final SenderCompID PASSPORT = new SenderCompID("MSDW-PPT");

    private static final SenderCompID REDI = new SenderCompID("REDIRPT");

    private static final SenderCompID TRADEWEB = new SenderCompID("TRADEWEB");

    private TradingPlatform platform;

    /**
     * Factory method to determine the strategy.
     * 
     * @param senderCompId
     * @return
     */
    public static ConversionStrategy getStrategy (SenderCompID senderCompId)
    {
        if (TRADESTATION.equals(senderCompId))
        {
            return new TradeStationConversionStrategy();
        }
        else if (PASSPORT.equals(senderCompId))
        {
            return new PassportConversionStrategy();
        }
        else if (REDI.equals(senderCompId))
        {
            return new RediConversionStrategy();
        }
        else if (TRADEWEB.equals(senderCompId))
        {
            return new TradeWebConversionStrategy();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fftw.bloomberg.aggregator.ConversionStrategy#getPlatform()
     */
    public TradingPlatform getPlatform ()
    {
        return this.platform;
    }

    protected void setPlatform (TradingPlatform platform)
    {
        this.platform = platform;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fftw.bloomberg.aggregator.ConversionStrategy#convertMessage(quickfix.fix42.ExecutionReport)
     */
    public CmfMessage convertMessage (quickfix.fix42.ExecutionReport message, Emailer mailer)
        throws FieldNotFound
    {
        log.info("Converting " + getPlatform() + " ExecutionReport to CMF");

        CmfMessage cmfMessage = CmfMessageFactory.getInstance().createTradeRecordAim();

        CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();

        List<String> errorMessages = convertCommon(message, tradeRecord);

        // if tag 167 is set, it indicates OPT/FUT, otherwise assume it is
        // equity
        if (message.isSetField(SecurityType.FIELD))
        {
            SecurityType securityType = message.get(new SecurityType());
            if (SecurityType.FUTURE.equals(securityType.getValue()))
            {
                convertFutures(message, tradeRecord);
            }
            else if (SecurityType.OPTION.equals(securityType.getValue()))
            {
                convertOptions(message, tradeRecord);
            }
            else
            {
                // If we don't know what it is, just use the basic symbol
                // Maybe we should throw an exception here instead?
                tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.Unknown);
                tradeRecord.setSecurityId(message.get(new Symbol()).getValue());
                throw new IllegalArgumentException("Unknown security type:"
                    + securityType.getValue());
            }
        }
        else if (message.isSetField(6609))
        {
            // TradeWeb uses 6609 instead of 167 for FIX 4.2
            String securityType = message.getString(6609);
            if (SecurityType.TO_BE_ANNOUNCED.equals(securityType))
            {
                convertTba(message, tradeRecord);
            }
            else
            {
                log.warn("Received security type of " + securityType + " from " + getPlatform());
                mailer.emailErrorMessage("TradeWeb Execution - unknown trade type", message.toString(), true);
            }
        }
        else
        // equities
        {
            convertEquities(message, tradeRecord);
        }

        log.info(tradeRecord.getProtocolMessage());

        if (errorMessages != null && errorMessages.size() > 0)
        {
            String tradeDetails = extractTradeDetails(cmfMessage);
            errorMessages.add("\n");
            errorMessages.add(tradeDetails);
            mailer.emailErrorMessage("Warnings during conversion", errorMessages, false);
        }

        return cmfMessage;
    }

    private String extractTradeDetails (CmfMessage cmfMessage)
    {
        return CmfRecordFormatter.formatForEmail(cmfMessage.getTradeRecord());
    }

    protected SettlmntTyp extractSettlementType (quickfix.fix42.ExecutionReport message)
        throws FieldNotFound
    {
        // If settlement type is not sent, assume that it is regular settlement
        SettlmntTyp settlementType = new SettlmntTyp(SettlmntTyp.REGULAR);

        if (message.isSetField(SettlmntTyp.FIELD))
        {
            settlementType = message.get(new SettlmntTyp());
        }
        return settlementType;
    }

    /**
     * Convert fields that are common to all securities.
     * 
     * The current fields that are set are:
     * <ul>
     * <li>tradingPlatform</li>
     * <li>executionID</li>
     * <li>cmfStatus - new trade (1)</li>
     * <li>price</li>
     * <li>price/quote display - price (1)</li>
     * <li>bloombergAccount - from lookup table</li>
     * <li>tradingStrategy - from lookup table</li>
     * <li>sourceCode - ecn (1600)
     * <li>
     * <li>side</li>
     * <li>shares</li>
     * </ul>
     * 
     * Subclasses should call this method first before performing any custom
     * logic.
     * 
     * Fields that are likely to be in subclasses:
     * <ul>
     * <li>primeBroker</li>
     * </ul>
     * 
     * @param message
     * @param tradeRecord
     * @throws FieldNotFound
     */
    protected List<String> convertCommon (quickfix.fix42.ExecutionReport message,
        CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        List<String> errorMessages = null;

        tradeRecord.setTradingPlatform(getPlatform());
        tradeRecord.setExecutionID(message.get(new ExecID()).getValue());
        tradeRecord.setStatus(CmfConstants.STATUS_NEW_TRADE);

        tradeRecord.setPriceQuote(message.get(new LastPx()).getValue());
        tradeRecord.setPriceQuoteDisplay(1); // Always using price

        String account = Fix2CmfUtil.bloombergAccount(getPlatform(), message);

        if (account == null)
        {
            Map<Integer, String> newMappings = Fix2CmfUtil.determineMissingMappingValues(
                getPlatform(), message);
            // Default to test - create an email message
            account = "TEST";

            StringBuilder sb = new StringBuilder(1024);
            for (int tagNum : newMappings.keySet())
            {
                sb.append("platform=").append(getPlatform());
                sb.append("; tag=").append(tagNum).append("; tagValue=");
                sb.append(newMappings.get(tagNum));
                sb.append("\n");
            }
            errorMessages = addErrorMessage(errorMessages,
                "The following values are missing from the mapping tables to "
                    + "determine account and strategy:\n" + sb.toString());

            errorMessages = addErrorMessage(errorMessages, "No matching account for trade, using "
                + account);
        }
        tradeRecord.setAccount(account);

        String tradingStrategy = Fix2CmfUtil.bloombergStrategy(getPlatform(), message);

        if (tradingStrategy == null)
        {
            // Default to test - create an email message
            tradingStrategy = "TEST";

            errorMessages = addErrorMessage(errorMessages,
                "No matching trading strategy for trade, using " + tradingStrategy);
        }

        tradeRecord.setTradingStrategy(tradingStrategy);

        tradeRecord.setSourceCode(CmfConstants.SC_ECN);

        tradeRecord.setSide(determineSide(message.get(new Side())));
        tradeRecord.setQuantity(message.get(new LastShares()).getValue());

        return errorMessages;
    }

    /**
     * Convert equities specific fields.
     * 
     * Currently converted fields:
     * <ul>
     * <li>productCode - equity (2)</li>
     * <li>securityIdFlag - equity ticker (98)</li>
     * <li>settlementDate</li>
     * </ul>
     * 
     * Subclasses should call this method first before performing any custom
     * logic.
     * 
     * Fields that are likely to be in subclasses:
     * <ul>
     * <li>securityId - symbol</li>
     * <li>broker</li>
     * </ul>
     * 
     * @param message
     * @param tradeRecord
     * @throws FieldNotFound
     */
    protected void convertEquities (quickfix.fix42.ExecutionReport message,
        CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.Equity);
        tradeRecord.setProductCode(BBProductCode.Equity); // Equity

        SettlmntTyp settlementType = extractSettlementType(message);
        tradeRecord.setSettleDate(determineSettlementDate(message, settlementType));

        // Testing for Short Sell/Buy Cover
        Side side = message.getSide();
        if (side.getValue() == Side.SELL_SHORT)
        {
            tradeRecord.setShortSale(1);
        }
        // else if (side.getValue() == Side.BUY &&
        // message.isSetField(PositionEffect.FIELD))
        // {
        // char pe = message.getChar(PositionEffect.FIELD);
        // if (pe == 'C')
        // {
        // // We need to indicate a buy to cover
        // }
        // }

    }

    /**
     * Convert options specific fields.
     * 
     * Currently converted fields:
     * <ul>
     * <li>productCode - this is not set, logic should be determined to do so</li>
     * <li>securityIdFlag - futures/options (99)</li>
     * <li>settlementDate</li>
     * </ul>
     * 
     * Subclasses should call this method first before performing any custom
     * logic.
     * 
     * Fields that are likely to be in subclasses:
     * <ul>
     * <li>securityId - symbol</li>
     * <li>broker</li>
     * </ul>
     * 
     * @param message
     * @param tradeRecord
     * @throws FieldNotFound
     */
    protected void convertOptions (quickfix.fix42.ExecutionReport message,
        CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.FutureOption);

        SecurityType securityType = message.get(new SecurityType());
        SettlmntTyp settlementType = extractSettlementType(message);

        tradeRecord.setSettleDate(determineSettlementDate(message, settlementType, securityType));
    }

    /**
     * Convert futures specific fields.
     * 
     * Currently converted fields:
     * <ul>
     * <li>securityIdFlag - futures/options (99)</li>
     * <li>settlementDate</li>
     * <li>transactionCode - 'D'</li>
     * </ul>
     * 
     * Subclasses should call this method first before performing any custom
     * logic.
     * 
     * Fields that are likely to be in subclasses:
     * <ul>
     * <li>securityId - symbol</li>
     * <li>broker</li>
     * <li>productCode</li>
     * </ul>
     * 
     * @param message
     * @param tradeRecord
     * @return
     * @throws FieldNotFound
     */
    protected void convertFutures (quickfix.fix42.ExecutionReport message,
        CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.FutureOption);
        tradeRecord.setTransactionCode("D");

        SecurityType securityType = message.get(new SecurityType());
        SettlmntTyp settlementType = extractSettlementType(message);

        tradeRecord.setSettleDate(determineSettlementDate(message, settlementType, securityType));
    }

    protected void convertTba (quickfix.fix42.ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("No default behavior for TBA");
    }

    /**
     * Not all provides send the symbols in upper-case. Extract the symbol,
     * uppercase it and then return the symbol.
     * 
     * @param message
     * @return
     * @throws FieldNotFound
     */
    protected Symbol extractSymbol (quickfix.fix42.ExecutionReport message) throws FieldNotFound
    {
        Symbol symbol = message.get(new Symbol());

        return new Symbol(symbol.getValue().toUpperCase().trim());
    }

    protected SecurityExchange extractExchange (quickfix.fix42.ExecutionReport message,
        SecurityExchange defaultExchange) throws FieldNotFound
    {
        // Use LastMarket first, if not found, use SecurityExchange, if not, use
        // the specified default
        if (message.isSetField(LastMkt.FIELD))
        {
            LastMkt lastMarket = message.get(new LastMkt());
            return new SecurityExchange(lastMarket.getValue());
        }

        if (message.isSetField(SecurityExchange.FIELD))
        {
            return message.get(new SecurityExchange());
        }
        return defaultExchange;
    }

    /**
     * Extract the root symbol from the Bloomberg symbol.
     * 
     * The monthYear is important as we are truncating the symbol based on the
     * year. Century years 0-9 is minus 2, years 10-99 is minus 3.
     * 
     * 
     * @param symbol
     * @param monthYear
     * @return
     */
    protected Symbol extractFuturesRootSymbol (Symbol symbol, MaturityMonthYear monthYear)
    {
        // based on the year, determine how much to trim
        LocalDate ld = DateTimeUtil.getLocalDate(monthYear.getValue() + "01");

        int year = ld.getYearOfCentury();
        String str = symbol.getValue();
        String root = null;

        if (year < 10)
        {
            root = str.substring(0, str.length() - 2);
        }
        else
        {
            root = str.substring(0, str.length() - 3);
        }
        return new Symbol(root);
    }

    /**
     * Based on the settlement type and security type determine the settlement
     * date.
     * 
     * This uses the settlement business calendar. We assume US calendar.
     * 
     * @param message
     * @param settlementType
     * @param securityType
     * @return
     * @throws FieldNotFound
     */
    protected String determineSettlementDate (ExecutionReport message, SettlmntTyp settlementType,
        SecurityType securityType) throws FieldNotFound
    {

        // If Future date is provided, replace settlement date with futures date
        if (message.isSetField(FutSettDate.FIELD))
        {
            // Normally on Futures have this set
            return message.get(new FutSettDate()).getValue();
        }

        // Default to sent time
        TransactTime tt = new TransactTime(DateTimeUtil.getDate(message.getHeader().getString(
            SendingTime.FIELD).toString()));
        if (message.isSetField(TransactTime.FIELD))
        {
            tt = message.get(new TransactTime());
        }

        SettlementDate sd = SettlementUtil.determineSettlementDate(settlementType, securityType);
        LocalDate ld = MarketCalendar.getInstance("NYSE").determineDate(
            new LocalDate(tt.getValue()), sd);

        return DateTimeUtil.getDateAsString(ld);
    }

    protected String determineSettlementDate (ExecutionReport message, SettlmntTyp settlementType)
        throws FieldNotFound
    {
        return determineSettlementDate(message, settlementType, null);
    }

    protected int determineSide (Side side)
    {
        switch (side.getValue())
        {
            case Side.BUY:
            case Side.BUY_MINUS:
                return CmfConstants.SIDE_BUY;
            case Side.SELL:
            case Side.SELL_PLUS:
            case Side.SELL_SHORT:
            case Side.SELL_SHORT_EXEMPT:
                return CmfConstants.SIDE_SELL;

            default:
                log.error("Unable to detemine side for FIX value: " + side);
        }
        return 0;
    }

    protected List<String> addErrorMessage (List<String> errorMessages, String message)
    {
        if (errorMessages == null)
        {
            errorMessages = new ArrayList<String>();
        }

        errorMessages.add(message);

        return errorMessages;
    }
}
