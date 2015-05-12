package com.fftw.bloomberg.aggregator;

import java.math.BigDecimal;
import java.sql.SQLException;

import org.joda.time.LocalDate;
import org.mule.extras.client.MuleClient;
import org.mule.umo.UMOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.FutSettDate;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.MaturityMonthYear;
import quickfix.field.OrdStatus;
import quickfix.field.PossDupFlag;
import quickfix.field.PossResend;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityType;
import quickfix.field.SenderCompID;
import quickfix.field.SendingTime;
import quickfix.field.SettlmntTyp;
import quickfix.field.Side;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.Logout;

import com.fftw.bloomberg.cmfp.CmfConstants;
import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfMessageFactory;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.cmfp.dao.CmfTradeRecordDAO;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;
import com.fftw.util.Emailer;
import com.fftw.util.datetime.DateTimeUtil;
import com.fftw.util.settlement.SettlementDate;
import com.fftw.util.settlement.SettlementUtil;
import com.fftw.util.settlement.calendar.MarketCalendar;

public class AggregatorApplication extends MessageCracker implements Application
{

    private static final String SENDER_TS = "TRAD";

    private static final String SENDER_PP = "MSDW-PPT";

    private static final String SENDER_REDI = "GS-REDI";

    // private static final String TRADERNAME_TS = "TRADSTAT";
    // private static final String TRADERNAME_PP = "PASSPORT";
    // private static final String TRADERNAME_REDI = "REDI";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Logger fixLogger = LoggerFactory.getLogger("FixMessageLog");

    private CmfTradeRecordDAO dao;

    private Emailer mailer;

    /**
     * default to using in-memory queue
     */
    private String destinationUri = "vm://poms.outbound.queue";

    public AggregatorApplication ()
    {
    }

    public void fromAdmin (Message message, SessionID sessionID) throws FieldNotFound,
        IncorrectDataFormat, IncorrectTagValue, RejectLogon
    {
        // Handle the Logout Message
        try
        {
            crack(message, sessionID);
        }
        catch (UnsupportedMessageType e)
        {
            log.error("Unable to crack message", e);
        }
        // How to do login for FIX 4.4
        // final Message.Header header = message.getHeader();
        // if ( header.getField( MsgType.FIELD ).valueEquals( MsgType.LOGON ) )
        // {

        // message.setField( UserName.FIELD, userName );
        // message.setField( Password.FIELD, password );
        // If necessary handle reseting the sequence number during login
        // message.setField(new ResetSeqNumFlag(true) );

        // }
    }

    /**
     * Receive messages from FIX connections.
     * <ul>
     * <li>TradeStation as 4.2</li>
     * <li>TOMS as 4.4 - disabled</li>
     * </ul>
     */
    public void fromApp (Message message, SessionID sessionID) throws FieldNotFound,
        IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType
    {
        fixLogger.info(message.toString());
        crack(message, sessionID);
    }

    public void onCreate (SessionID sessionID)
    {
        log.info("Created session:" + sessionID);
    }

    public void onLogon (SessionID arg0)
    {
        // TODO Auto-generated method stub

    }

    public void toAdmin (Message message, SessionID sessionId)
    {
        // TODO Auto-generated method stub
    }

    public void toApp (Message arg0, SessionID arg1) throws DoNotSend
    {

    }

    /**
     * Receive Execution Reports from TradeStation.
     * 
     * TradeStation, Passport and REDI all send FIX 4.2. We use the SenderCompID
     * to determine the logic that needs to be applied.
     */
    @Override
    public void onMessage (quickfix.fix42.ExecutionReport message, SessionID sessionID)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue
    {
        log.info("Received FIX 4.2 message" + message);
        try
        {
            ExecType execType = message.get(new ExecType());
            OrdStatus orderStatus = message.get(new OrdStatus());
            // All of the sources are sending Fills and partial fills
            if ((execType.getValue() == ExecType.FILL || execType.getValue() == ExecType.PARTIAL_FILL)
                && orderStatus.getValue() != OrdStatus.NEW)
            {
                String senderId = message.getHeader().getString(SenderCompID.FIELD);
                CmfMessage cmfMessage = null;
                if (SENDER_TS.equals(senderId))
                {
                    cmfMessage = convertTradeStationToCmf(message, TradingPlatform.TradeStation);
                }
                else if (SENDER_PP.equals(senderId))
                {
                    cmfMessage = convertPassportToCmf(message);
                }
                else if (SENDER_REDI.equals(senderId))
                {
                    cmfMessage = convertRediToCmf(message);
                }
                else
                {
                    log.error("Unknown SenderCompID: " + senderId);
                }
                if (cmfMessage != null)
                {
                    try
                    {
                        MuleClient client = new MuleClient();
                        client.send(destinationUri, cmfMessage, null);
                    }
                    catch (UMOException e)
                    {
                        log.error("Could not send message to POMS sender", e);
                        mailer.emailErrorMessage("Unable to send trade.  Trade not sent to POMS.",
                            cmfMessage.getTradeRecord(), true);
                    }
                }
            }
            else
            {
                log.info("Skipping execution report of execType: " + execType.getValue()
                    + " ordStatus:" + orderStatus.getValue());
            }
        }
        catch (FieldNotFound e)
        {
            mailer.emailErrorMessage("Unable parse FIX message.  Trade not sent to POMS.", message
                .toString()
                + "\n\n" + e.toString(), true);
            // Re-throw the message
            throw e;
        }
    }

    /**
     * Convert a Passport FIX 4.2 message to CMF.
     * 
     * Passport drop copies should already have the symbol converted to
     * Bloomberg symbology.
     * 
     * @param message
     * @param sessionID
     * @return
     */
    private CmfMessage convertPassportToCmf (ExecutionReport message) throws FieldNotFound
    {
        // For now we will us the TradeStation version until we find it does not
        // work.
        // Then we will have to move the logic here. Once we have the logic, we
        // can make a more generic process that we can then get more code re-use
        CmfMessage cmfMessage = convertTradeStationToCmf(message, TradingPlatform.Passport);

        CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();
        tradeRecord.setPrimeBroker("MSPB");
        tradeRecord.setBroker("MOGSNY");

        return cmfMessage;
    }

    /**
     * Convert a REDI Plus FIX 4.2 message to CMF.
     * 
     * REDI drop copies should already have the symbol converted to Bloomberg
     * symbology.
     * 
     * @param message
     * @param sessionID
     * @return
     */
    private CmfMessage convertRediToCmf (ExecutionReport message) throws FieldNotFound
    {
        // For now we will us the TradeStation version until we find it does not
        // work.
        // Then we will have to move the logic here. Once we have the logic, we
        // can make a more generic process that we can then get more code re-use
        CmfMessage cmfMessage = convertTradeStationToCmf(message, TradingPlatform.REDI);

        CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();
        tradeRecord.setPrimeBroker("MSPB");
        tradeRecord.setBroker("GOLDNY");

        return cmfMessage;

    }

    /**
     * TradeStation sends us FIX 4.2 messages. These need to be converted to
     * CMFP messages.
     * 
     * All equity/options trades are DVP accounts. DVP accounts have Morgan
     * Stanley as the prime broker. Futures do not have a prime broker.
     * 
     * @param message
     * @throws FieldNotFound
     */
    private CmfMessage convertTradeStationToCmf (quickfix.fix42.ExecutionReport message,
        TradingPlatform platform) throws FieldNotFound
    {
        log.info("Converting " + platform + " ExecutionReport to CMF");

        if (checkAlreadySent(platform, message))
        {
            return null;
        }

        CmfMessage cmfMessage = CmfMessageFactory.getInstance().createTradeRecordAim();

        CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();
        // Use the sender compID as the platform source
        tradeRecord.setTradingPlatform(platform);

        tradeRecord.setExecutionID(message.get(new ExecID()).getValue());
        tradeRecord.setStatus(CmfConstants.STATUS_NEW_TRADE);

        // If settlement type is not sent, assume that it is regular settlement
        SettlmntTyp settlementType = new SettlmntTyp(SettlmntTyp.REGULAR);

        if (message.isSetField(SettlmntTyp.FIELD))
        {
            settlementType = message.get(new SettlmntTyp());
        }

        BigDecimal priceMultiplier = BigDecimal.ONE;
        // if tag 167 is set, it indicates OPT/FUT, otherwise assume it is
        // equity
        if (message.isSetField(SecurityType.FIELD))
        {
            SecurityType securityType = message.get(new SecurityType());
            if (SecurityType.FUTURE.equals(securityType.getValue()))
            {
                tradeRecord.setSecurityIdFlag(CmfConstants.SIF_FUTURE_OPTION);
                // Default to CME
                SecurityExchange futuresExchange = new SecurityExchange("2");
                if (message.isSetField(SecurityExchange.FIELD))
                {
                    futuresExchange = message.get(futuresExchange);
                }

                Symbol symbol = message.get(new Symbol());

                String futureSymbol = Fix2CmfUtil.futureSymbol(platform, symbol, message
                    .get(new MaturityMonthYear()));
                tradeRecord.setSecurityId(futureSymbol);
                tradeRecord.setProductCode(Fix2CmfUtil.mapFuturesProductCode(platform, symbol));

                priceMultiplier = Fix2CmfUtil.mapFuturesPriceMultiplier(platform, symbol);
                // if (tradeRecord.getProductCode() == 10)
                // {
                // tradeRecord.setExpandedStatus(12);
                // }
                // should not be required
                tradeRecord.setTransactionCode("D");
                // no prime broker for 'futures'
            }
            else if (SecurityType.OPTION.equals(securityType.getValue()))
            {
                tradeRecord.setSecurityIdFlag(CmfConstants.SIF_FUTURE_OPTION);
                // Default to Chicago Board Options Exchange
                SecurityExchange exchange = new SecurityExchange("W");

                if (message.isSetField(SecurityExchange.FIELD))
                {
                    exchange = message.get(exchange);
                }
                String symbol = Fix2CmfUtil.optionsSymbol(message.get(new Symbol()), exchange,
                    message.get(new MaturityMonthYear()), message.get(new PutOrCall()), message
                        .get(new StrikePrice()));

                tradeRecord.setSecurityId(symbol);
                // tradeRecord.setProductCode(pc); // nothing for this
            }
            else
            {
                // If we don't know what it is, just use the basic symbol
                tradeRecord.setSecurityIdFlag(CmfConstants.SIF_UNKNOWN);
                tradeRecord.setSecurityId(message.get(new Symbol()).getValue());
            }

            tradeRecord
                .setSettleDate(determineSettlementDate(message, settlementType, securityType));
        }
        else
        // equities
        {
            tradeRecord.setSecurityIdFlag(CmfConstants.SIF_EQUITY);
            tradeRecord.setSettleDate(determineSettlementDate(message, settlementType));
            // Security includes the exchange
            Symbol symbol = message.get(new Symbol());
            // Default to NYSE
            SecurityExchange fixExchange = new SecurityExchange("N");
            if (message.isSetField(SecurityExchange.FIELD))
            {
                fixExchange = message.get(new SecurityExchange());
            }
            tradeRecord.setSecurityId(Fix2CmfUtil.equitySymbol(symbol, fixExchange));

            tradeRecord.setProductCode(2); // Equity
            tradeRecord.setBroker("BEARNY");
        }

        tradeRecord.setSide(determineSide(message.get(new Side())));
        tradeRecord.setQuantity(message.get(new LastShares()).getValue());

        if (priceMultiplier.equals(BigDecimal.ONE))
        {
            tradeRecord.setPriceQuote(message.get(new LastPx()).getValue());
        }
        else
        {
            // If we have a mapping (even if it is 1.000) we end up here
            BigDecimal price = BigDecimal.valueOf(message.get(new LastPx()).getValue());
            tradeRecord.setPriceQuote(priceMultiplier.multiply(price));
        }
        tradeRecord.setPriceQuoteDisplay(1); // Always using price

        // TradeStation's platform key is 'TS'
        tradeRecord.setAccount(Fix2CmfUtil.bloombergAccount(platform, message));
        tradeRecord.setTradingStrategy(Fix2CmfUtil.bloombergStrategy(platform, message));
        // All accounts are DVP, and all DVP are Morgan Stanley
        tradeRecord.setPrimeBroker("MSPB");
        // TODO this is the equity value, need to confirm for futures
        tradeRecord.setBroker("BEARNY");

        // This value is from Ryan Sheftel
        tradeRecord.setSourceCode(CmfConstants.SC_ECN);

        log.info(tradeRecord.getProtocolMessage());

        return cmfMessage;
    }

    private boolean checkAlreadySent (TradingPlatform platform, ExecutionReport message)
        throws FieldNotFound
    {
        if (message.isSetField(PossDupFlag.FIELD) || message.isSetField(PossResend.FIELD))
        {
            try
            {
                CmfTradeRecord dbTradeRecord = dao.findByExecId(new LocalDate(), platform
                    .toString(), message.get(new ExecID()).getValue());

                if (dbTradeRecord.getAcceptRejectTimestamp() != null
                    || dbTradeRecord.getReceiptTimestamp() != null)
                {
                    // we sent this item and it was received/accepted/rejected
                    log.info("Received possDup FIX message that was dup.  Not resending. "
                        + dbTradeRecord.toString());
                    return true;
                }
            }
            catch (SQLException e)
            {
                // this will not happen as we are using Spring
            }
        }
        return false;
    }

    private String determineSettlementDate (ExecutionReport message, SettlmntTyp settlementType)
        throws FieldNotFound
    {
        return determineSettlementDate(message, settlementType, null);
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
    private String determineSettlementDate (ExecutionReport message, SettlmntTyp settlementType,
        SecurityType securityType) throws FieldNotFound
    {

        // If Future date is provided, replace settlement date with future
        // date
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

    private int determineSide (Side side)
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

    public void setDao (CmfTradeRecordDAO dao)
    {
        this.dao = dao;
    }

    @Override
    public void onMessage (Logout message, SessionID sessionID) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue
    {
        System.out.println("message=" + message);
/*        if (message.isSetField(Text.FIELD))
        {
            String txt = message.get(new Text()).getValue();

            if (txt.contains("Serious Error:  Message sequence number:"))
            {

                try
                {
                    int pos = txt.lastIndexOf(' ');
                    String expectedNumber = txt.substring(pos + 1);

                    Session.lookupSession(sessionID).setNextSenderMsgSeqNum(
                        Integer.parseInt(expectedNumber));
                }
                catch (IOException e)
                { // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }*/
    }

    public void onLogout (SessionID arg0)
    {
        // TODO Auto-generated method stub

    }

    public String getDestinationUri ()
    {
        return destinationUri;
    }

    public void setDestinationUri (String destinationUri)
    {
        this.destinationUri = destinationUri;
    }

    public Emailer getMailer ()
    {
        return mailer;
    }

    public void setMailer (Emailer mailer)
    {
        this.mailer = mailer;
    }
}
