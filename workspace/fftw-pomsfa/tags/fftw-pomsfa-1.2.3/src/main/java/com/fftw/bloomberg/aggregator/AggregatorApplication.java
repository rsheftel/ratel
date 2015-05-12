package com.fftw.bloomberg.aggregator;

import java.io.IOException;
import java.sql.SQLException;

import org.joda.time.LocalDate;
import org.mule.extras.client.MuleClient;
import org.mule.extras.client.RemoteDispatcher;
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
import quickfix.Session;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.field.PossDupFlag;
import quickfix.field.PossResend;
import quickfix.field.SenderCompID;
import quickfix.field.Text;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.cmfp.dao.CmfTradeRecordDAO;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.util.Emailer;

public class AggregatorApplication extends MessageCracker implements Application
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Logger fixLogger = LoggerFactory.getLogger("FixMessageLog");

    private CmfTradeRecordDAO dao;

    private Emailer mailer;

    private boolean useRemote = false;

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

    /**
     * Session was created - add listener
     */
    public void onCreate (SessionID sessionID)
    {
        log.info("Created session:" + sessionID);
        Session mySession = Session.lookupSession(sessionID);

        // removed until we can get a better schedule for sessions
        mySession.addStateListener(new AggregatorSessionStateListener(sessionID, mailer));
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
                SenderCompID senderCompID = new SenderCompID(senderId);

                ConversionStrategy conversionStrategy = AbstractConversionStrategy
                    .getStrategy(senderCompID);

                if (conversionStrategy == null)
                {
                    log.error("Unknown SenderCompID: " + senderId);
                    mailer.emailErrorMessage("Unknown SenderCompID '" + senderId
                        + "' - cannot process.  Trade not sent to POMS.", message.toString(), true);
                    return;
                }
                if (checkAlreadySent(conversionStrategy.getPlatform(), message))
                {
                    return;
                }

                CmfMessage cmfMessage = conversionStrategy.convertMessage(message, mailer);
                if (cmfMessage != null)
                {
                    try
                    {
                        MuleClient client = new MuleClient();
                        if (useRemote)
                        {
                            RemoteDispatcher dispatcher = client.getRemoteDispatcher("tcp://localhost:60504");
                            //RemoteDispatcher dispatcher = client.getRemoteDispatcher("tcp://nysrv31:60504");
                            dispatcher.sendRemote(destinationUri, cmfMessage, null);
                        }
                        else
                        {
                            client.send(destinationUri, cmfMessage, null);
                        }
                    }
                    catch (UMOException e)
                    {
                        log.error("Could not send message to POMS sender", e);
                        mailer.emailErrorMessage("Unable to send trade.  Trade not sent to POMS.",
                            cmfMessage.getTradeRecord(), true);
                    }
                }
                else
                {
                    // if the record is null, we already processed
                    log.info("Already processed execution:" + message);
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

    public void setDao (CmfTradeRecordDAO dao)
    {
        this.dao = dao;
    }

    @Override
    public void onMessage (quickfix.fix42.Logout message, SessionID sessionID)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue
    {
        System.out.println("message=" + message);

        // This is really here to make PDM happy
        if (Boolean.getBoolean("resetSequenceOnLogout") && message.isSetField(Text.FIELD))
        {
            String txt = message.get(new Text()).getValue();
            if ((txt.contains("Serious Error:") && txt.contains("Message sequence number:"))
                || txt.contains("SEQUENCE NUMBER: Seq"))
            {
                try
                {
                    int pos = txt.lastIndexOf(' ');
                    String expectedNumber = txt.substring(pos + 1);

                    Integer expectedSeq = Integer.parseInt(expectedNumber);
                    log.warn("Setting sequence number to " + expectedSeq + " for sessionID:"
                        + sessionID);
                    Session.lookupSession(sessionID).setNextSenderMsgSeqNum(expectedSeq);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
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
