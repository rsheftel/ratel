package com.fftw.bloomberg.aggregator;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fftw.bloomberg.cmfp.CmfApplication;
import com.fftw.bloomberg.cmfp.CmfConstants;
import com.fftw.bloomberg.cmfp.CmfHeader;
import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfSession;
import com.fftw.bloomberg.cmfp.CmfSessionID;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.cmfp.dao.CmfTradeRecordDAO;
import com.fftw.util.Emailer;

public class CmfSenderApplication implements CmfApplication
{

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Book-keeping for sent trades. Track ACK and Accept/Rejects
     */
    private Map<String, CmfMessage> sentTrades = new HashMap<String, CmfMessage>();

    private final Timer responseTimer = new Timer("ResendTimer", true);

    private CmfSessionID pomsSessionID;

    private CmfTradeRecordDAO dao;

    private Emailer mailer;

    public CmfSenderApplication (CmfSessionID pomsSessionID)
    {
        this.pomsSessionID = pomsSessionID;
    }

    public CmfSenderApplication ()
    {

    }

    public void setPomsSessionID (CmfSessionID sessionID)
    {
        pomsSessionID = sessionID;
    }

    /**
     * Check if the Receipt or Accept/Reject has been has been received.
     * 
     */
    private static class ResendTimerTask extends TimerTask
    {

        private CmfMessage resendMessage;

        private String waitingForResponse;

        private CmfSenderApplication application;

        private CmfSessionID srcSessionID;

        private ResendTimerTask (CmfSenderApplication app, CmfSessionID sessionID,
            CmfMessage message, String waitingFor)
        {
            application = app;
            srcSessionID = sessionID;
            resendMessage = message;
            waitingForResponse = waitingFor;
        }

        @Override
        public void run ()
        {
            // We need to check if we received our response
            CmfMessage cacheMessage = application.getSentTrade(resendMessage);

            if (cacheMessage != null)
            {
                if (waitingForResponse.equals(CmfConstants.TRADE_RECEIPT)
                    && cacheMessage.getTradeRecord().getReceiptTimestamp() == null)
                {
                    application.resendMessage(cacheMessage, srcSessionID);
                    application.log.warn("No receipt, resending tradeRecord "
                        + cacheMessage.getTradeRecord().getTradeSeqNum());
                }
                else if (waitingForResponse.equals(CmfConstants.ACCEPT_REJECT)
                    && cacheMessage.getTradeRecord().getAcceptRejectTimestamp() == null)
                {
                    application.resendMessage(cacheMessage, srcSessionID);
                    application.log.warn("No accept/reject, resending tradeRecord "
                        + cacheMessage.getTradeRecord().getTradeSeqNum());
                }
                else
                {
                    application.log.info("Received " + waitingForResponse + " "
                        + resendMessage.getTradeRecord().getTradeSeqNum());
                    application.removeSentTrade(resendMessage);
                }
            }
            else
            {
                application.log.info("Timer fired, but record already processed: "+ waitingForResponse);
            }
        }

    }

    public void fromAdmin (CmfMessage message, CmfSessionID sessionId)
    {
        // TODO Auto-generated method stub

    }

    public void fromApp (CmfMessage message, CmfSessionID sessionId)
    {
        // We have received either an Receipt or an Accept/Reject message
        // Perform book-keeping
        CmfHeader header = message.getMutableHeader();

        if (CmfConstants.TRADE_RECEIPT.equals(header.getMessageType()))
        {
            CmfTradeRecord tradeRecord = message.getTradeRecord();
            tradeRecord.setReceiptTimestamp(new DateTime());
            updateReceipt(tradeRecord);
            updateSentTrade(message);
            responseTimer.schedule(new ResendTimerTask(this, sessionId, message,
                CmfConstants.ACCEPT_REJECT), 30 * 1000);
        }
        else if (CmfConstants.ACCEPT_REJECT.equals(header.getMessageType()))
        {
            CmfTradeRecord tradeRecord = message.getTradeRecord();
            tradeRecord.setAcceptRejectTimestamp(new DateTime());
            updateAcceptReject(tradeRecord);
            removeSentTrade(message);
            // Do we need to report an error?
            if (tradeRecord.getReturnCode() != 0 && tradeRecord.getReturnCode() != 2)
            {
                mailer.emailErrorMessage("Rejected Bloomberg Trade; rcode="
                    + tradeRecord.getReturnCode(), tradeRecord, false);
            }
        }
    }

    public void toAdmin (CmfMessage message, CmfSessionID sessionId)
    {
        // TODO Auto-generated method stub

    }

    public void toApp (CmfMessage message, CmfSessionID sessionId)
    {
        // TODO Auto-generated method stub

    }

    private void resendMessage (CmfMessage cmfMessage, CmfSessionID dstSessionID)
    {
        CmfSession dstSession = CmfSession.lookupSession(dstSessionID);
        dstSession.sendMessage(cmfMessage);

        insertNewSentTrade(cmfMessage);

        responseTimer.schedule(new ResendTimerTask(this, dstSession.getCmfSessionID(), cmfMessage,
            CmfConstants.TRADE_RECEIPT), 30 * 1000);

    }

    private boolean updateAcceptReject (CmfTradeRecord tradeRecord)
    {
        boolean updated = false;

        for (int i = 0; i < 3 && !updated; i++)
        {
            try
            {
                tradeRecord = dao.updateAcceptReject(tradeRecord);

                if (tradeRecord == null)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private boolean updateReceipt (CmfTradeRecord tradeRecord)
    {
        boolean updated = false;

        for (int i = 0; i < 3 && !updated; i++)
        {
            try
            {
                tradeRecord = dao.updateReceipt(tradeRecord);

                if (tradeRecord == null)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private void updateSentTrade (CmfMessage cmfMessage)
    {
        // Replace the current record with the updated one
        CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();
        String mapKey = tradeRecord.getCreationDate().toString() + tradeRecord.getTradeSeqNum();
        sentTrades.put(mapKey, cmfMessage);
    }

    private void insertNewSentTrade (CmfMessage cmfMessage)
    {
        CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();

        String mapKey = tradeRecord.getCreationDate().toString() + tradeRecord.getTradeSeqNum();
        sentTrades.put(mapKey, cmfMessage);
    }

    CmfMessage getSentTrade (CmfMessage cmfMessage)
    {
        CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();
        String mapKey = tradeRecord.getCreationDate().toString() + tradeRecord.getTradeSeqNum();

        return sentTrades.get(mapKey);
    }

    private void removeSentTrade (CmfMessage cmfMessage)
    {
        CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();
        String mapKey = tradeRecord.getCreationDate().toString() + tradeRecord.getTradeSeqNum();
        sentTrades.remove(mapKey);
    }

    public void sendMessage (CmfMessage cmfMessage)
    {
        sendMessage(cmfMessage, pomsSessionID);
    }

    /**
     * This is the point that we can/should decouple.
     * 
     * In the TradeStation/FIX sender we want to put the message on the JMS
     * Queue.
     * 
     * In POMS sender we want to read from an JMS Queue and persist to the
     * database and then send.
     * 
     * @param cmfMessage
     * @param srcSessionID
     */
    private synchronized void sendMessage (CmfMessage cmfMessage, CmfSessionID dstSessionID)
    {
        // Store the message into the database - generates the
        // tradeSequenceNumber and then send.
        int tradeSequenceNumber = persistTradeMessage(cmfMessage);
        if (tradeSequenceNumber != -1)
        {
            CmfSession dstSession = CmfSession.lookupSession(dstSessionID);
            if (dstSession == null)
            {
                log.error("Unable find POMS session.  Not sending: " + cmfMessage.getWireMessage());
                mailer.emailErrorMessage("Unable to find POMS session.  Trade not sent to POMS.",
                    cmfMessage.getTradeRecord(), true);
            }
            dstSession.sendMessage(cmfMessage);
            insertNewSentTrade(cmfMessage);
            responseTimer.schedule(new ResendTimerTask(this, dstSession.getCmfSessionID(),
                cmfMessage, CmfConstants.TRADE_RECEIPT), 30 * 1000);
        }
        else
        {
            log.error("Unable to generate tradeSequenceNumber for TradeRecord.  Not sending: "
                + cmfMessage.getWireMessage());
            mailer.emailErrorMessage("Unable to insert Bloomberg trade.  Trade not sent to POMS.",
                cmfMessage.getTradeRecord(), true);
        }
    }

    private int persistTradeMessage (CmfMessage cmfMessage)
    {
        boolean inserted = false;

        Exception lastException = null;
        CmfTradeRecord tradeRecord = null;
        for (int i = 0; i < 3 && !inserted; i++)
        {
            try
            {
                tradeRecord = dao.insert(cmfMessage.getTradeRecord());

                if (tradeRecord == null)
                {
                    return -1;
                }
                else
                {
                    return tradeRecord.getTradeSeqNum();
                }
            }
            catch (Exception e)
            {
                lastException = e;
            }
        }
        if (lastException != null)
        {
            log.error("Unable to insert trade record", lastException);
            mailer.emailErrorMessage("Unable to insert trade record", cmfMessage.getTradeRecord(),
                true);
        }
        return -1;
    }

    public void setDao (CmfTradeRecordDAO dao)
    {
        this.dao = dao;
    }

    public void setMailer (Emailer mailer)
    {
        this.mailer = mailer;
    }

}
