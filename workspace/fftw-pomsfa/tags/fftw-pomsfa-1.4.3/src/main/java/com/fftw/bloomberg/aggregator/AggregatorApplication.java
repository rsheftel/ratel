package com.fftw.bloomberg.aggregator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import malbec.fer.dao.FixFillDao;
import malbec.fix.message.FixFill;
import malbec.fix.message.FixFillFactory;

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
import quickfix.fix42.Allocation;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.Logout;

import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.cmfp.dao.CmfTradeRecordDAO;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.util.Emailer;

public class AggregatorApplication extends MessageCracker implements Application
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Logger fixLogger = LoggerFactory.getLogger("FixMessageLog");

    private final Logger processedFixLogger = LoggerFactory.getLogger("ProcessedFixMessageLog");

    private final Map<SessionID, AggregatorSessionStateListener> stateListenerMap = new HashMap<SessionID, AggregatorSessionStateListener>();

    private CmfTradeRecordDAO dao;

    private Emailer mailer;

    private boolean useRemote = false;

    private boolean sendToProd = false;

    private boolean useThreadedProcessing = false;

    private boolean resetSequenceNumberOnLogout;

    private ExecutionProcessor executionProcessor;

    private final BlockingQueue<ExecutionReport> messagesToProcess = new LinkedBlockingQueue<ExecutionReport>();

    /**
     * default to using in-memory queue
     */
    private String destinationUri = "vm://poms.outbound.queue";

    public AggregatorApplication ()
    {
    }

    public void setResetSequenceNumberOnLogout (boolean reset)
    {
        this.resetSequenceNumberOnLogout = reset;
    }

    public boolean isResetSequenceNumberOnLogout ()
    {
        return resetSequenceNumberOnLogout;
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
        if (!stateListenerMap.containsKey(sessionID))
        {
            Session mySession = Session.lookupSession(sessionID);
            AggregatorSessionStateListener sl = new AggregatorSessionStateListener(sessionID,
                mailer);
            mySession.addStateListener(sl);
            stateListenerMap.put(sessionID, sl);
            log.info("Added SessionStateListener:" + sessionID);
        }
    }

    public void onLogon (SessionID arg0)
    {
        // TODO Auto-generated method stub

    }

    public void toAdmin (Message message, SessionID sessionId)
    {
        // If we are sending a logout message, turn off the disconnect listener
        if (message instanceof Logout)
        {
            AggregatorSessionStateListener listener = stateListenerMap.get(sessionId);

            listener.setLogoutSent(true);
        }
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
        String senderCompStr = "";

        try
        {
            ExecType execType = message.get(new ExecType());
            OrdStatus orderStatus = message.get(new OrdStatus());
            // All of the sources are sending Fills and partial fills
            if ((execType.getValue() == ExecType.FILL || execType.getValue() == ExecType.PARTIAL_FILL)
                && orderStatus.getValue() != OrdStatus.NEW)
            {
             // Record the execution as it comes in
                FixFill fill = FixFillFactory.valueOf(message);
                if (insertFixFill(fill) == -1) {
                    if (fill.isPossibleDuplicate()) {
                        log.warn("Duplicate execution report: " + message);
                    } else {
                        log.error("Unable to convert FIX ExecutionReport: " + message);
                    }
                }
                
                if (useThreadedProcessing)
                {
                    messagesToProcess.offer(message);
                    return;
                }

                String senderId = message.getHeader().getString(SenderCompID.FIELD);
                SenderCompID senderCompID = new SenderCompID(senderId);

                senderCompStr = senderCompID.getValue();

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
                    dispatchMessage(cmfMessage);
                }
                else
                {
                    // if the record is null, we already processed or do not
                    // want to process
                    log.info("Already processed or unable to process execution:" + message);
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
        catch (UnsupportedOperationException e)
        {
            mailer.emailErrorMessage("Unsupported trade sent to " + senderCompStr,
                "Check log for details.  " + e.getMessage(), true);
        }
        catch (Exception e)
        {
            mailer.emailErrorMessage("Unexpected Exception " + senderCompStr,
                "Check log for details.  " + e.getMessage(), true);
            log.error("Unexpected Exception", e);
        }
    }

    public void onMessage () throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue
    {
        quickfix.fix42.ExecutionReport message = messagesToProcess.poll();
        if (message == null)
        {
            return;
        }

        String senderCompStr = "";

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

                senderCompStr = senderCompID.getValue();

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
                    dispatchMessage(cmfMessage);
                }
                else
                {
                    // if the record is null, we already processed or do not
                    // want to process
                    log.info("Already processed or unable to process execution:" + message);
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
        catch (UnsupportedOperationException e)
        {
            mailer.emailErrorMessage("Unsupported trade sent to " + senderCompStr,
                "Check log for details.  " + e.getMessage(), true);
        }
        catch (Exception e)
        {
            mailer.emailErrorMessage("Unexpected Exception " + senderCompStr,
                "Check log for details.  " + e.getMessage(), true);
            log.error("Unexpected Exception", e);
        }

        processedFixLogger.info(message.toString());
    }

    private void dispatchMessage (CmfMessage cmfMessage)
    {
        try
        {
            MuleClient client = new MuleClient();
            if (useRemote)
            {
                if (sendToProd)
                {
                    RemoteDispatcher dispatcher = client.getRemoteDispatcher("tcp://nysrv31:60504");
                    dispatcher.sendRemote(destinationUri, cmfMessage.getWireMessage(), null);
                    // dispatcher.sendRemote(destinationUri, cmfMessage, null);
                }
                else
                {
                    RemoteDispatcher dispatcher = client
                        .getRemoteDispatcher("tcp://localhost:60504");
                    dispatcher.sendRemote(destinationUri, cmfMessage, null);
                }
            }
            else
            {
                // Send as a string so we don't have to deal with serialization
                // - this is missing an execution id, so we cannot use it for
                // anything other than testing
                // client.send(destinationUri,cmfMessage.getWireMessage(), null);
                
                // Sent as an CmfRecord
                client.send(destinationUri, cmfMessage, null);
                // send to ActiveMQ as well
                
            }
        }
        catch (UMOException e)
        {
            log.error("Could not send message to POMS sender", e);
            mailer.emailErrorMessage("Unable to send trade.  Trade not sent to POMS.", cmfMessage
                .getTradeRecord(), true);
        }
    }

    /**
     * TradeWeb sends us an Allocation message.
     * 
     * Adding this messages prevents the 'Rejected message type' from showing up
     * in the log file.
     */
    public void onMessage (Allocation message, SessionID sessionID)
    {
        // The only thing we can do with this is ignore it. They send us the
        // account to allocate to, but we already created the BB record by then
        // and will not easily be able to match
    }

    private boolean checkAlreadySent (TradingPlatform platform, ExecutionReport message)
        throws FieldNotFound
    {
        if (message.getHeader().isSetField(PossDupFlag.FIELD)
            || message.getHeader().isSetField(PossResend.FIELD))
        {
            try
            {
                CmfTradeRecord dbTradeRecord = dao.findByExecId(new LocalDate(), platform
                    .toString(), message.get(new ExecID()).getValue());

                if (dbTradeRecord != null
                    && (dbTradeRecord.getAcceptRejectTimestamp() != null || dbTradeRecord
                        .getReceiptTimestamp() != null))
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
    public void onMessage (quickfix.fix42.Logon message, SessionID sessionID)
    {
        log.info("logon message", message.toString());
    }

    @Override
    public void onMessage (quickfix.fix42.Logout message, SessionID sessionID)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue
    {
        log.info("logout message", message.toString());

        if (message.isSetField(Text.FIELD))
        {
            // 58=MsgSeqNum too low, expecting 2987 but received 85010=211
            String txt = message.get(new Text()).getValue();
            if (resetSequenceNumberOnLogout
                && ((txt.contains("Serious Error:") && txt.contains("Message sequence number:"))
                    || txt.contains("SEQUENCE NUMBER: Seq") || txt.contains("MsgSeqNum too low")))
            {
                try
                {
                    String expectedNumber = "";
                    if (txt.contains("MsgSeqNum too low"))
                    {
                        int pos = txt.lastIndexOf("expecting ");
                        int numStart = txt.indexOf(' ', pos) + 1;
                        int numEnd = txt.indexOf(' ', numStart);
                        expectedNumber = txt.substring(numStart, numEnd);
                    }
                    else
                    {
                        int pos = txt.lastIndexOf(' ');
                        expectedNumber = txt.substring(pos + 1);
                    }
                    Integer expectedSeq = Integer.parseInt(expectedNumber);
                    log.warn("NextSender sequence number should be " + expectedSeq
                        + " for sessionID:" + sessionID);
                    // if (Boolean.getBoolean("resetSequenceOnLogout"))
                    // {
                    Session.lookupSession(sessionID).setNextSenderMsgSeqNum(expectedSeq);
                    StringBuilder sb = new StringBuilder(1024);
                    sb.append(sessionID).append(" Reset Sequence Number to ").append(expectedSeq);
                    mailer.emailErrorMessage(sb.toString(), txt, true);
                    resetSequenceNumberOnLogout = false;
                    // }
                }
                catch (Exception e)
                {
                    log.error("Unable to process sequence error:", e);
                }
            }
            else if (txt.contains("Application Request To End Session"))
            {
                log.info("Request to end session for " + sessionID);
                try
                {
                    Session session = Session.lookupSession(sessionID);
                    session.reset();
                }
                catch (IOException e)
                {
                    log.error("Unable to reset the sequence numbers after request to end session");
                }
            }
        }
    }

    private long insertFixFill(FixFill fill) {
        if (fill == null || fill.isOrderAck()) {
            log.warn("Skipping execution report: " + fill);
            return -1;
        }

        FixFillDao dao = FixFillDao.getInstance();

        if (fill.isPossibleDuplicate()) {
            FixFill dbFill = dao.findById(fill.getBeginString(), fill.getSenderCompId(), fill.getSenderSubId(),
                fill.getTradeDate(), fill.getExecutionId());

            if (dbFill != null) {
                return -1;
            }
        }

        return dao.persist(fill);
    }
    
    public void onLogout (SessionID sessionID)
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

    public void stopExecutionProcessor ()
    {
        if (executionProcessor != null)
        {
            executionProcessor.setRunning(false);
            executionProcessor = null;
        }
    }

    BlockingQueue<ExecutionReport> getExecutionQueue ()
    {
        return messagesToProcess;
    }

    public void startExecutionProcessor ()
    {
        stopExecutionProcessor();

        if (isUseThreadedProcessing())
        {
            executionProcessor = new ExecutionProcessor(this);
            executionProcessor.setRunning(true);
            Thread executionProcesThread = new Thread(executionProcessor);
            executionProcesThread.start();
        }
    }

    private static class ExecutionProcessor implements Runnable
    {

        private boolean running = false;

        private AggregatorApplication aa;

        public ExecutionProcessor (AggregatorApplication aa)
        {
            this.aa = aa;
        }

        public synchronized boolean isRunning ()
        {
            return running;
        }

        public synchronized void setRunning (boolean running)
        {
            this.running = running;
        }

        @Override
        public void run ()
        {
            while (isRunning())
            {
                try
                {
                    aa.onMessage();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    public synchronized boolean isUseThreadedProcessing ()
    {
        return useThreadedProcessing;
    }

    public synchronized void setUseThreadedProcessing (boolean useThreadedProcessing)
    {
        this.useThreadedProcessing = useThreadedProcessing;
    }
}
