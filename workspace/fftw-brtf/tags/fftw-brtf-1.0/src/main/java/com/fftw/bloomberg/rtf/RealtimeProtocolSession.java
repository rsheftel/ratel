package com.fftw.bloomberg.rtf;

import com.fftw.bloomberg.rtf.filter.TradeFeedCodecFactory;
import com.fftw.bloomberg.rtf.messages.DefaultRtfMessage;
import com.fftw.bloomberg.rtf.messages.RtfHeader;
import com.fftw.bloomberg.rtf.messages.RtfMessage;
import com.fftw.bloomberg.rtf.messages.RtfMessageFactory;
import com.fftw.bloomberg.rtf.types.RtfCommand;
import com.fftw.bloomberg.rtf.types.RtfMode;
import com.fftw.sbp.MemoryMessageStore;
import com.fftw.sbp.MessageStore;
import com.fftw.sbp.ProtocolIoHandler;
import com.fftw.sbp.ProtocolSession;
import com.fftw.sbp.ProtocolSessionConfig;
import com.fftw.sbp.SessionApplication;
import com.fftw.sbp.SessionSchedule;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 *
 */
public class RealtimeProtocolSession implements ProtocolSession<RealtimeSessionID, RtfMessage> {

    final Logger logger = LoggerFactory.getLogger(getClass().getName());
    final Logger onlineLogger = LoggerFactory.getLogger("OnlineMessageLog");
    final Logger unimplementedLogger = LoggerFactory.getLogger("UnimplementedFeatureLog");

    private SessionSchedule schedule;
    private RealtimeSessionID sessionID;
    private SessionApplication application;

    private int pricingNumber;
    private int siteNumber;

    private IoSession ioSession;

    private ProtocolCodecFactory codecFactory = new TradeFeedCodecFactory();

    private ProtocolSessionConfig sessionConfig;

    private ProtocolIoHandler ioHandler;

    private MessageStore messageStore;

    private int heartbeatInterval;

    private LocalDate connectDate;

    private RtfMessageFactory messageFactory = new RtfMessageFactory();

    RealtimeProtocolSession(RealtimeApplication app, Properties props, ProtocolIoHandler ioHandler) {

        schedule = new SessionSchedule(props);
        messageStore = configureMessageStore(props);
        this.ioHandler = ioHandler;
        this.application = app;

        extractSiteAndPricing(props);
        ioHandler.setProtocolSession(this);
        sessionID = new RealtimeSessionID(pricingNumber, siteNumber, RtfMode.Online);
        app.setSessionID(getSessionID());
    }

    private void extractSiteAndPricing(Properties props) {
        try {
            pricingNumber = Integer.parseInt(props.getProperty("session.pricingNumber"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Missing or incorrect pricing number");
        }
        try {
            siteNumber = Integer.parseInt(props.getProperty("session.siteNumber"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Missing or incorrect site number");
        }
    }

    private MessageStore configureMessageStore(Properties props) {

        boolean persitMessages = Boolean.valueOf(props.getProperty("store.persist", "false"));

        if (persitMessages) {
            return null;
        } else {
            return new MemoryMessageStore();
        }
    }


    public void setSchedule(SessionSchedule schedule) {
        this.schedule = schedule;
    }

    public void setHeartbeatInterval(int interval) {
        this.heartbeatInterval = interval;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public DateTime getStartTime() {
        return schedule.getStartTime();
    }

    public RealtimeSessionID getSessionID() {
        return sessionID;
    }

    public boolean isActive() {
        return schedule.isSessionActive();
    }

    public boolean isEnabled() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ProtocolSessionConfig getSessionConfig() {
        return sessionConfig;
    }

    public IoHandler getIoHandler() {
        return ioHandler;
    }

    public void setIoHandler(ProtocolIoHandler ioHandler) {
        this.ioHandler = ioHandler;
    }

    public void setIoSession(IoSession ioSession) {
        this.ioSession = ioSession;
    }

    public ProtocolCodecFactory getProtocolCodecFactory() {
        return codecFactory;
    }

    /**
     * Process in the incoming message.
     * <p/>
     * We handle the following messages: Connect,
     * The rest are passed to the application.
     *
     * @param message
     */
    public void processMessage(RtfMessage message) {
        RtfHeader header = message.getHeader();

        logger.debug("Received message:" + header);

        if (header.getMode() == RtfMode.Online) {
            onlineLogger.info(header.toString());
            onlineLogger.info(""+message.getBody());
            switch (header.getCommand()) {
                case Connect:

                    messageStore.setNextReceiverSeqNum(header.getSequenceNumber());
                    connectDate = header.getDate();
                    // Send back an Accept
                    RtfHeader acceptHeader = new RtfHeader(RtfCommand.Accept, connectDate, messageStore.getNextReceiverSeqNum());
                    RtfMessage acceptMessage = new DefaultRtfMessage(acceptHeader);
                    ioSession.write(acceptMessage);
                    break;
                case Status:
                    // send back Status
                    logger.info("Received status/hearbeat: " + header);
                    RtfHeader statustHeader = new RtfHeader(RtfCommand.Status, new LocalDate(), pricingNumber, siteNumber);
                    RtfMessage statusMessage = messageFactory.createMessage(statustHeader, null);
                    ioSession.write(statusMessage);
                    // the application does not need to know
                    //return;
                    // Pass the heartbeat to the App so it can send it to clients
                    break;

                case Data:
                    logger.info("Received data: " + header);
                    RtfHeader ackHeader = new RtfHeader(RtfCommand.Ack, header.getDate(), header.getSequenceNumber());
                    ioSession.write(messageFactory.createMessage(ackHeader, null));
                    break;
            }
        } else {
            // TODO handle batch requests
            unimplementedLogger.info("Batch message received" + header.toString());
        }

        application.fromApp(this, message);
    }

    public void sendMessage(RtfMessage message) {
        ioSession.write(message);
    }

}
