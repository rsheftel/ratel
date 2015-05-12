package com.fftw.bloomberg.cmfp;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.Log;
import quickfix.MessageStore;

public class CmfSession
{
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final Logger pomsLog = LoggerFactory.getLogger("PomsMessageLog");

    private static Map<CmfSessionID, CmfSession> sessions = new HashMap<CmfSessionID, CmfSession>();

    /**
     * Session scheduling setting to specify first day of trading week.
     */
    public static final String SETTING_START_DAY = "StartDay";

    /**
     * Session scheduling setting to specify last day of trading week.
     */
    public static final String SETTING_END_DAY = "EndDay";

    /**
     * Session scheduling setting to specify starting time of the trading day.
     */
    public static final String SETTING_START_TIME = "StartTime";

    /**
     * Session scheduling setting to specify end time of the trading day.
     */
    public static final String SETTING_END_TIME = "EndTime";

    /**
     * Session scheduling setting to specify time zone for the session.
     */
    public static final String SETTING_TIMEZONE = "TimeZone";

    private CmfApplication cmfApplication;

    private CmfSessionID cmfSessionId;

    private String pricingNumber;

    private String specVersion;

    private boolean enabled;

    private IoSession ioSession;

    private MessageStore messageStore;

    private Log log = new SimpleLog(this);

    private final SessionSchedule sessionSchedule;

    public CmfSession (CmfSessionSettings cmfSettings, CmfSessionID sessionId,
        CmfApplication application, String pricingNumber, String specVersion)

    {
        this.cmfSessionId = sessionId;
        this.cmfApplication = application;
        this.pricingNumber = pricingNumber;
        this.specVersion = specVersion;

        SessionSchedule localSchedule = null;
        try
        {
            localSchedule = new SessionSchedule(cmfSettings, sessionId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.sessionSchedule = localSchedule;

        String path = null;

        try
        {

            if (cmfSettings.isSetting(sessionId, "FileStorePath"))
            {
                path = cmfSettings.getString(sessionId, "FileStorePath");
            }
            messageStore = new CmfFileStore(path, sessionId, false);
        }
        catch (ConfigError e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (FieldConvertError e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        registerCmfSession(this);
        setEnabled(true);
    }

    public String getPricingNumber ()
    {
        return pricingNumber;
    }

    public String getSpecVersion ()
    {
        return specVersion;
    }

    public CmfApplication getCmfApplication ()
    {
        return cmfApplication;
    }

    public CmfSessionID getCmfSessionID ()
    {
        return this.cmfSessionId;
    }

    static void registerCmfSession (CmfSession session)
    {
        synchronized (sessions)
        {
            sessions.put(session.getCmfSessionID(), session);
        }
    }

    public static void unregisterSessions (Collection<CmfSessionID> sessionIds)
    {
        synchronized (sessions)
        {
            for (CmfSessionID sessionId : sessionIds)
            {
                sessions.remove((CmfSessionID)sessionId);
            }
        }
    }

    public static CmfSession lookupSession (CmfSessionID sessionID)
    {
        synchronized (sessions)
        {
            return sessions.get(sessionID);
        }
    }

    public synchronized boolean sendMessage (CmfMessage message)
    {
        try
        {
            CmfHeader header = message.getMutableHeader();
            // header.setSequenceNumber(nextSequenceNumber());
            int sequenceNumber = messageStore.getNextSenderMsgSeqNum();
            header.setSequenceNumber(sequenceNumber);
            header.setTime(new Date());
            header.setVersion(specVersion);
            header.setPricingNumber(pricingNumber);
            header.setMessageLength(message.getTradeRecordLength());

            String messageStr = message.getWireMessage();
            ioSession.write(messageStr);
            messageStore.incrNextSenderMsgSeqNum();
                        
            log.onOutgoing(messageStr);

            // Only log outgoing trades here
            if (CmfConstants.TRADE.equals(header.getMessageType()))
            {
                pomsLog.info(messageStr);
                // We don't have replay, so don't store the messages
//                messageStore.set(sequenceNumber, messageStr);
            }

            // Until we figure out the correct place to put this - do it here
            // We only have 8 digits for the sequence number, we should be 
            // resetting when we disconnect, but we don't.  Reset here when
            // the sequence number gets too big!
            // Limiting this to 9000, should cover 1 day without duplicates - doesn't really
            // matter as BB is not using this number
            if (messageStore.getNextSenderMsgSeqNum() >= 9000) {
                messageStore.reset();
            }
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }

    public synchronized void setIoSession (IoSession session)
    {
        this.ioSession = session;
    }

    public void receivedMessage (int sequenceNumber)
    {
        try
        {
            messageStore.setNextTargetMsgSeqNum(sequenceNumber + 1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isLoggedOn ()
    {
        return true;
    }

    private synchronized void setEnabled (boolean enabled)
    {
        this.enabled = enabled;
    }

    public synchronized boolean isEnabled ()
    {
        return enabled;
    }

    public DemuxingProtocolCodecFactory getProtocolCodecFactory ()
    {
        return new CmfProtocolCodecFactory();
    }

    public boolean isSessionTime ()
    {
        return sessionSchedule.isSessionTime();
    }

    public Log getLog ()
    {
        return log;
    }

    private final static class SimpleLog implements Log
    {
        private CmfSession session;

        SimpleLog (CmfSession session)
        {
            this.session = session;
        }

        public void onOutgoing (String message)
        {
            session.logger.info(message);
        }

        public void onIncoming (String message)
        {
            session.logger.info(message);
        }

        public void onEvent (String text)
        {
            session.logger.info(text);
        }

        public void clear ()
        {
        }
    }
}
