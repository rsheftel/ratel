package com.fftw.bloomberg.aggregator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import malbec.bloomberg.types.BBYellowKey;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fer.mapping.IDatabaseMapper;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.ExecID;
import quickfix.field.FutSettDate;
import quickfix.field.LastMkt;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.MaturityMonthYear;
import quickfix.field.PositionEffect;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityType;
import quickfix.field.SenderCompID;
import quickfix.field.SendingTime;
import quickfix.field.SettlmntTyp;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

import com.fftw.bloomberg.cmfp.CmfConstants;
import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfMessageFactory;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.AccountType;
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

    public static final SenderCompID TRADINGSCREEN = new SenderCompID("TSRPT");

    private static final SenderCompID PASSPORT = new SenderCompID("MSDW-PPT");

    public static final SenderCompID REDI = new SenderCompID("REDIRPT");

    public static final SenderCompID TRADEWEB = new SenderCompID("TRADEWEB");

    public static final SenderCompID EMSX = new SenderCompID("BLPDROP");

    private static final List<String> sentEmailFor = new ArrayList<String>();

    private TradingPlatform platform;
    
    private DatabaseMapper dbMapper;

    /**
     * Factory method to determine the strategy.
     * 
     * @param senderCompId
     * @return
     */
    @Deprecated
    public static ConversionStrategy getStrategy (SenderCompID senderCompId)
    {

        if (REDI.equals(senderCompId))
        {
            return new RediConversionStrategy(null);
        }

        if (TRADEWEB.equals(senderCompId))
        {
            return new TradeWebConversionStrategy();
        }

        if (EMSX.equals(senderCompId))
        {
            return new EmsxConversionStrategy();
        }

        if (TRADINGSCREEN.equals(senderCompId))
        {
            return new TradingScreenConversionStrategy(null);
        }

        if (TRADESTATION.equals(senderCompId))
        {
            return new TradeStationConversionStrategy(null);
        }

        if (PASSPORT.equals(senderCompId))
        {
            return new PassportConversionStrategy(null);
        }

        // nothing matched - unsupported platform
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
    
    protected DatabaseMapper getDatabaseMapper() {
        return dbMapper;
    }

    protected void setDatabaseMapper(DatabaseMapper dbMapper) {
        this.dbMapper = dbMapper;
    }
    
    public CmfMessage convertMessage (quickfix.fix44.ExecutionReport message, Emailer emailer)
        throws FieldNotFound
    {
        return convertExecutionMessage((Message)message, emailer);
    }

    public CmfMessage convertMessage (quickfix.fix42.ExecutionReport message, Emailer emailer)
        throws FieldNotFound
    {
        return convertExecutionMessage((Message)message, emailer);
    }

    protected CmfMessage convertExecutionMessage (quickfix.Message message, Emailer mailer)
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
            String securityType = message.getString(SecurityType.FIELD);
            if (SecurityType.FUTURE.equals(securityType))
            {
                convertFutures(message, tradeRecord);
            }
            else if (SecurityType.OPTION.equals(securityType))
            {
                convertOptions(message, tradeRecord);
            }
            else if (SecurityType.COMMON_STOCK.equals(securityType))
            {
                convertEquities(message, tradeRecord);
            }
            else if (SecurityType.FOREIGN_EXCHANGE_CONTRACT.equals(securityType))
            {
                convertFX(message, tradeRecord);
            }
            else
            {
                // If we don't know what it is, just use the basic symbol
                // Maybe we should throw an exception here instead?
                tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.Unknown);
                tradeRecord.setSecurityId(message.getString(Symbol.FIELD));
                throw new IllegalArgumentException("Unknown security type:"
                    + securityType);
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
            else if ("UST".equals(securityType))
            {
                convertGovt(message, tradeRecord);
            }
            else if ("CDS".equals(securityType))
            {
                convertCds(message, tradeRecord);
            }
            else
            {
                log.warn("Received security type of " + securityType + " from " + getPlatform());
                if (!sentEmailFor.contains(securityType))
                {
                    mailer.emailErrorMessage("TradeWeb Execution - unknown trade type "
                        + securityType, message.toString(), true);
                    sentEmailFor.add(securityType);
                }
                return null; // nothing to process
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

    protected void convertFX (Message message, CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        throw new UnsupportedOperationException("No default behavior for FX");
    }

    protected void convertCds (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("No default behavior for CDS");
    }

    private String extractTradeDetails (CmfMessage cmfMessage)
    {
        return CmfRecordFormatter.formatForEmail(cmfMessage.getTradeRecord());
    }

    protected SettlmntTyp extractSettlementType (quickfix.Message message) throws FieldNotFound
    {
        // If settlement type is not sent, assume that it is regular settlement
        SettlmntTyp settlementType = new SettlmntTyp(SettlmntTyp.REGULAR);

        if (message.isSetField(SettlmntTyp.FIELD))
        {
            settlementType = new SettlmntTyp(message.getChar(SettlmntTyp.FIELD));
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
    protected List<String> convertCommon (quickfix.Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        List<String> errorMessages = null;

        tradeRecord.setTradingPlatform(getPlatform());
        tradeRecord.setExecutionID(message.getString(ExecID.FIELD));
        // tradeRecord.setExecutionID(message.get(new ExecID()).getValue());
        tradeRecord.setStatus(CmfConstants.STATUS_NEW_TRADE);

        tradeRecord.setPriceQuote(message.getDecimal(LastPx.FIELD));
        // tradeRecord.setPriceQuote(message.get(new LastPx()).getValue());
        tradeRecord.setPriceQuoteDisplay(1); // Always using price

        //String account = Fix2CmfUtil.bloombergAccount(getPlatform(), message);
        String account = getBloombergAccount(getPlatform(), message);

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

        String tradingStrategy = getTradingStrategy(getPlatform(), message);

        if (tradingStrategy == null)
        {
            // Default to test - create an email message
            tradingStrategy = "TEST";

            errorMessages = addErrorMessage(errorMessages,
                "No matching trading strategy for trade, using " + tradingStrategy);
        }

        tradeRecord.setTradingStrategy(tradingStrategy);

        tradeRecord.setSourceCode(CmfConstants.SC_ECN);

        tradeRecord.setSide(determineSide(message));
        // tradeRecord.setQuantity(message.get(new LastShares()).getValue());
        tradeRecord.setQuantity(message.getDouble(LastShares.FIELD));

        return errorMessages;
    }

    protected String getBloombergAccount (TradingPlatform platform, Message message)
    {
        return Fix2CmfUtil.bloombergAccount(platform, message);
    }

    protected String getTradingStrategy (TradingPlatform platform, Message message)
    {
        return Fix2CmfUtil.bloombergStrategy(platform, message);
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
    protected void convertEquities (quickfix.Message message,
        CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.Equity);
        tradeRecord.setProductCode(BBYellowKey.Equity);

        SettlmntTyp settlementType = extractSettlementType(message);
        tradeRecord.setSettleDate(determineSettlementDate(message, settlementType));

        String primeBroker = Fix2CmfUtil.mapPrimeBroker(getPlatform(), AccountType.Equity, "NO PB");
        tradeRecord.setPrimeBroker(primeBroker);
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
    protected void convertOptions (quickfix.Message message,
        CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.FutureOption);

        SecurityType securityType = new SecurityType(message.getString(SecurityType.FIELD));
        SettlmntTyp settlementType = extractSettlementType(message);

        tradeRecord.setSettleDate(determineSettlementDate(message, settlementType, securityType));

        String primeBroker = Fix2CmfUtil
            .mapPrimeBroker(getPlatform(), AccountType.Unknown, "NO PB");
        tradeRecord.setPrimeBroker(primeBroker);
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
    protected void convertFutures (quickfix.Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.FutureOption);
        tradeRecord.setTransactionCode("D");

        SecurityType securityType = new SecurityType(message.getString(SecurityType.FIELD));
        SettlmntTyp settlementType = extractSettlementType(message);

        tradeRecord.setSettleDate(determineSettlementDate(message, settlementType, securityType));

        String primeBroker = Fix2CmfUtil
            .mapPrimeBroker(getPlatform(), AccountType.Futures, "NO PB");
        tradeRecord.setPrimeBroker(primeBroker);
        
        String bbRoot = populateFuturesSymbol(message, tradeRecord);
        BBYellowKey yellowKey = populateBloombergYellowKey(bbRoot, tradeRecord);
        applyPriceMultiplier(message, bbRoot, tradeRecord);
        
        // if we have a currency future, we need to send the Bloomberg unique ID
        if (BBYellowKey.Curncy == yellowKey)
        {
            tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.BBUID);
            String bbuid = Fix2CmfUtil.tickerToBloombergUniqueId(tradeRecord.getSecurityId());
            // if there is no mapping, use the original ticker
            if (bbuid != null)
            {
                tradeRecord.setSecurityId(bbuid);
            }
        }
    }

    protected void applyPriceMultiplier (Message message, String bbRoot, CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        IDatabaseMapper dbm = getDatabaseMapper();
        
        BigDecimal priceMultiplier = dbm.lookupFuturesInboundPriceMultiplier(getPlatform().getText(), bbRoot);
        if (BigDecimal.ONE.compareTo(priceMultiplier) == 0)
        {
            tradeRecord.setPriceQuote(message.getDecimal(LastPx.FIELD));
        }
        else
        {
            BigDecimal price = message.getDecimal(LastPx.FIELD);
            tradeRecord.setPriceQuote(priceMultiplier.multiply(price));
        }
    }

    protected BBYellowKey populateBloombergYellowKey (String bbRoot, CmfTradeRecord tradeRecord)
    {
        IDatabaseMapper dbm = getDatabaseMapper();
        BBYellowKey productCode = dbm.lookupYellowKey(bbRoot);
        tradeRecord.setProductCode(productCode);
        
        return productCode;
    }

    /**
     * Populate the trade record with the correct Bloomberg symbol.
     * 
     * @param message
     * @param tradeRecord
     * @return the Bloomberg root
     */
    protected String populateFuturesSymbol (Message message, CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        // TODO put in default logic here - probably expecting full bloomberg symbol
        return null;
    }

    protected void convertTba (quickfix.Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("No default behavior for TBA");
    }

    protected void convertGovt (quickfix.Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("No default behavior for TBA");
    }

    /**
     * Not all provides send the symbols in upper-case. Extract the symbol,
     * upper case it and then return the symbol.
     * 
     * @param message
     * @return
     * @throws FieldNotFound
     */
    protected Symbol extractSymbol (quickfix.Message message) throws FieldNotFound
    {
        String symbol = message.getString(Symbol.FIELD);

        return new Symbol(symbol.toUpperCase().trim());
    }

    protected SecurityExchange extractExchange (quickfix.Message message,
        SecurityExchange defaultExchange) throws FieldNotFound
    {
        // Use LastMarket first, if not found, use SecurityExchange, if not, use
        // the specified default
        if (message.isSetField(LastMkt.FIELD))
        {
            String lastMarket = message.getString(LastMkt.FIELD);
            return new SecurityExchange(lastMarket);
        }

        if (message.isSetField(SecurityExchange.FIELD))
        {
            return new SecurityExchange(message.getString(SecurityExchange.FIELD));
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
    protected String determineSettlementDate (Message message, SettlmntTyp settlementType,
        SecurityType securityType) throws FieldNotFound
    {

        // If Future date is provided, replace settlement date with futures date
        if (message.isSetField(FutSettDate.FIELD))
        {
            // Normally on Futures have this set
            return message.getString(FutSettDate.FIELD);
        }

        // Default to sent time
        TransactTime tt = new TransactTime(DateTimeUtil.getDate(message.getHeader().getString(
            SendingTime.FIELD)));
        if (message.isSetField(TransactTime.FIELD))
        {
            tt = new TransactTime(message.getUtcTimeStamp(TransactTime.FIELD));
        }

        SettlementDate sd = SettlementUtil.determineSettlementDate(settlementType, securityType);
        LocalDate ld = MarketCalendar.getInstance("NYSE").determineDate(
            new LocalDate(tt.getValue()), sd);

        return DateTimeUtil.getDateAsString(ld);
    }

    protected String determineSettlementDate (Message message, SettlmntTyp settlementType)
        throws FieldNotFound
    {
        return determineSettlementDate(message, settlementType, null);
    }

    protected int determineSide (quickfix.Message message) throws FieldNotFound
    {
        // Side side = message.get(new Side());
        char side = message.getChar(Side.FIELD);
        switch (side)
        {
            case Side.BUY:
            case Side.BUY_MINUS:
                if (isBuyToCover(message)) {
                    return CmfConstants.SIDE_BUY_COVER;
                }
                
           
                return CmfConstants.SIDE_BUY;
            case Side.SELL:
            case Side.SELL_PLUS:
            case Side.SELL_SHORT_EXEMPT:
                return CmfConstants.SIDE_SELL;
            case Side.SELL_SHORT:
                return CmfConstants.SIDE_SELL_SHORT;
            default:
                log.error("Unable to detemine side for FIX value: " + side);
        }
        return 0;
    }

    /**
     * This is really the logic for REDI.  Should really be removed
     * @param message
     * @return
     */
    protected boolean isBuyToCover (Message message)
    {
        if (message.isSetField(PositionEffect.FIELD))
        {
            try
            {
                char pe = message.getChar(PositionEffect.FIELD);
                return (pe == 'C');
            }
            catch (FieldNotFound e) 
            { 
                // deal with QFJ's stupid API
            }
        }
        
        return false;
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
