package com.fftw.bloomberg.rtf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import malbec.fer.mapping.DatabaseMapper;
import malbec.util.FuturesSymbolUtil;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fftw.bloomberg.PositionRecord;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.rtf.client.RealTimeFeedClient;
import com.fftw.bloomberg.rtf.messages.DefaultRtfMessage;
import com.fftw.bloomberg.rtf.messages.RtfEndOfDay;
import com.fftw.bloomberg.rtf.messages.RtfHeader;
import com.fftw.bloomberg.rtf.messages.RtfMessage;
import com.fftw.bloomberg.rtf.messages.RtfMessageBody;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.rtf.types.RtfCommand;
import com.fftw.jms.JmsTopicPublisherSession;
import com.fftw.jms.client.JmsApplication;
import com.fftw.jms.client.QueueListener;
import com.fftw.recon.ReconAggregatingCache;
import com.fftw.sbp.SessionApplication;
import com.fftw.util.Emailer;
import com.fftw.util.Filter;
import com.fftw.util.SystematicFacade;

/**
 * Handle the business logic for the Bloomberg Realtime Feed
 */
public class RealtimeApplication implements SessionApplication<RealtimeProtocolSession, RtfMessage>,
    JmsApplication, RealtimeApplicationMBean {

    final Logger logger = LoggerFactory.getLogger(getClass().getName());
    final Logger positionLogger = LoggerFactory.getLogger("PositionMessagesLog");

    private String brokerUrl;
    private JmsTopicPublisherSession topicSession;
    private QueueListener commandListener;

    private String batchFilename;

    private Emailer emailer;

    private DatabaseMapper dbm = new DatabaseMapper(true);

    // When we receive the EOD these will be set
    private boolean receivedEod;
    private LocalDate nextTradingDay;

    private LocalDate currentTradingDay;
    private LocalDate previousTradingDay;

    private RealtimeSessionID sessionID;

    private BatchPositionCache batchPositionCache = new BatchPositionCache();
    // private OnlinePositionCache onlinePositionCache = new OnlinePositionCache();
    private OnlinePositionAggregateCache onlinePositionCache = new OnlinePositionAggregateCache();

    // All items in here should have their tags set to the EMPTY_STRING
    private ReconAggregatingCache reconOnlinePositionCache = new ReconAggregatingCache();

    public RealtimeApplication() {

    }

    public RealtimeApplication(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public synchronized void setSessionID(RealtimeSessionID sessionID) {
        this.sessionID = sessionID;
    }

    public synchronized void setBatchFilename(String filename) {
        this.batchFilename = filename;
    }

    public synchronized void setEmailer(Emailer emailer) {
        this.emailer = emailer;
    }

    public synchronized void setCurrentTradingDay(LocalDate currentTradingDay) {
        this.currentTradingDay = currentTradingDay;
    }

    public void setPreviousTradingDay(LocalDate day, boolean useCurrentTradingDay) {
        if (useCurrentTradingDay) {
            this.previousTradingDay = previouseClose(day);
        } else {
            setPreviousTradingDay(day);
        }
    }

    public void setPreviousTradingDay(LocalDate previousTradingDay) {
        this.previousTradingDay = previousTradingDay;
    }

    public void startPositionPublisher() throws JMSException {
        try {
            topicSession = new JmsTopicPublisherSession(this, brokerUrl);
            topicSession.start();
        } catch (JMSException e) {
            logger.error("Unable to connect to topic.", e);
            throw e;
        }
    }

    public void startCommandListener(String commandQueueName) throws JMSException {
        try {
            commandListener = new QueueListener(brokerUrl, commandQueueName, this);
            commandListener.initialize();
        } catch (JMSException e) {
            logger.error("Unable to start command listener", e);
        }
    }

    public synchronized boolean loadBatchFile() throws IOException {
        if (previousTradingDay != null) {
            return loadBatchFile(batchFilename, previousTradingDay, batchPositionCache);
        } else {
            return loadBatchFile(batchFilename, previouseClose(currentTradingDay), batchPositionCache);
        }
    }

    private boolean loadBatchFile(String filename, LocalDate batchFileDay, BatchPositionCache cacheToLoad)
        throws IOException {

        if (filename.contains("${date}")) {
            filename = filename.replace("${date}", batchFileDay.toString("MM-dd-yy"));
        }

        File batchFile = new File(filename);
        FileReader fr = new FileReader(batchFile);

        logger.info("Loading batchfile: " + batchFile.getAbsoluteFile());
        BufferedReader batchPositionReader = new BufferedReader(fr);

        String line = "";
        while ((line = batchPositionReader.readLine()) != null) {
            BatchPosition bp = BatchPosition.valueOf(batchFileDay, line);
            // cacheToLoad.addBatchPosition(bp);
            cacheToLoad.aggregateBatchPosition(bp);
        }
        batchPositionReader.close();
        
        return true;
    }

    private LocalDate previouseClose(LocalDate date) {

        LocalDate previous = date.minusDays(1);

        while (isWeekend(previous)) {
            previous = previous.minusDays(1);
        }
        return previous;
    }

    private boolean isWeekend(LocalDate ld) {
        int dayOfWeek = ld.dayOfWeek().get();

        return (DateTimeConstants.SATURDAY == dayOfWeek || DateTimeConstants.SUNDAY == dayOfWeek);
    }

    public void onCreate(RealtimeProtocolSession session) {
    // To change body of implemented methods use File | Settings | File Templates.
    }

    public void onLogon(RealtimeProtocolSession session) {
    // To change body of implemented methods use File | Settings | File Templates.
    }

    public void onLogout(RealtimeProtocolSession session) {
    // To change body of implemented methods use File | Settings | File Templates.
    }

    public void toAdmin(RealtimeProtocolSession session, RtfMessage message) {
    // To change body of implemented methods use File | Settings | File Templates.
    }

    public void fromAdmin(RealtimeProtocolSession session, RtfMessage message) {
    // To change body of implemented methods use File | Settings | File Templates.
    }

    public void toApp(RealtimeProtocolSession session, RtfMessage message) {
        // send the message to bloomberg
        session.sendMessage(message);
    }
    
    public boolean isConnected(RealtimeProtocolSession session) {
        return session.getIoSession() != null;
    }
    
    /**
     * Business logic for the Real-time feed.
     * <p/>
     * We take the records and publish them on JMS (ActiveMQ)
     * 
     * @param session
     * @param message
     */
    public void fromApp(RealtimeProtocolSession session, RtfMessage message) {
        logger.info("Received message-header:" + message.getHeader());
        logger.info("Received message-body:" + message.getBody());

        if (RtfCommand.Data == message.getHeader().getCommand()) {
            RtfMessageBody body = message.getBody();

            if (body.getMessageType() == '4') {
                // We have a data record, publish it to the topic
                RtfOnlinePosition onlinePosition = (RtfOnlinePosition) body;
                if ("EMF".equals(onlinePosition.getAccount())) {
                    logger.info("Not publishing EMF position");
                    return;
                }

                if (onlinePosition.isCommodity() || onlinePosition.isCurrency()) {
                    onlinePosition.setContractSize(getContractSize(onlinePosition));
                }

                if (onlinePosition.isEquity() || onlinePosition.isIndex()) {
                    onlinePosition.setExchangeTicker(getExchangeTicker(onlinePosition));
                }
                logger.info(onlinePosition.toRawString());
                RtfOnlinePosition aggregated = onlinePositionCache.addOnlinePosition(onlinePosition);
                if (isReconSecurity(onlinePosition)) {
                    RtfOnlinePosition reconPosition = reconOnlinePositionCache
                        .addOnlinePosition(onlinePosition);
                    topicSession.publishToTopicLastImage(reconPosition);
                }
                topicSession.publishPositionMessage(createJmsTopic(onlinePosition.getAccount()), aggregated);
                positionLogger.info(onlinePosition.toString());
            } else if (message.getBody().getMessageType() == '5') {
                processEod((RtfEndOfDay) body);
            }
        } else if (RtfCommand.Status == message.getHeader().getCommand()) {
            // Where do we publish heartbeat messages?
            topicSession.publishHeartBeat();
        }
    }

    private String getExchangeTicker(RtfOnlinePosition onlinePosition) {
        return SystematicFacade.lookupExchangeTicker(onlinePosition.getSecurityId());
    }

    private boolean isReconSecurity(RtfOnlinePosition onlinePosition) {

        if (onlinePosition.isEquity()) {
            return true;
        }

        if (onlinePosition.isIndex() || onlinePosition.isCommodity() || onlinePosition.isCurrency()) {
            String reconBid = onlinePosition.getReconBloombergId();
            String bloombergRoot = FuturesSymbolUtil.extractSymbolRoot(reconBid);
            String ricRoot = dbm.mapBloombergRootToRicRoot("REDI", bloombergRoot);
            if (ricRoot != null) {
                String monthYear = FuturesSymbolUtil.extractMaturityMonthFromSymbol(reconBid);
                onlinePosition.setRic(FuturesSymbolUtil.combineRootMaturityMonthYear(ricRoot, monthYear));
                return true;
            }
        }

        return false;
    }

    private BigDecimal getContractSize(RtfOnlinePosition onlinePosition) {
        return BigDecimal.valueOf(SystematicFacade.lookupContractSize(onlinePosition.getReconBloombergId(),
            onlinePosition.getProductCode()));
    }

    private String createJmsTopic(String jmsTopic) {

        return "position." + jmsTopic;
    }

    /**
     * Receive command messages from clients.
     * <p/>
     * The current commands that we understand are:
     * <ul>
     * <li>SendBatch</li>
     * <li>SendOnline</li>
     * <li>SendBatchAndOnline</li>
     * </ul>
     * 
     * @param txtMsg
     * @param session
     * @throws JMSException
     */
    public void fromQueue(TextMessage txtMsg, Session session) throws JMSException {
        // extract the command and perform the action
        // This would publish all the data to the 'Reply-to'
        String[] commands = txtMsg.getText().split(" ");

        Destination destination = txtMsg.getJMSReplyTo();
        if (destination != null) {
            try {
                MessageProducer producer = session.createProducer(destination);
                String destStr = destination.toString();

                if ("SendBatchAndOnline".equalsIgnoreCase(commands[0].trim())) {
                    Filter<BatchPosition> batchPositionFilter = createBatchFilter(commands);
                    int totalBatch = processBatchPositionRequest(session, destStr, producer,
                        batchPositionFilter);
                    Filter<RtfOnlinePosition> onlinePositionFilter = createOnlineFilter(commands);
                    int totalOnline = processOnlinePositionRequest(session, destStr, producer,
                        onlinePositionFilter);
                    sendEndOfMessagesCommand(session, producer, totalBatch + totalOnline);
                } else if ("SendBatch".equalsIgnoreCase(commands[0].trim())) {
                    // check if we need to filter the data
                    Filter<BatchPosition> positionFilter = createBatchFilter(commands);
                    int totalBatch = processBatchPositionRequest(session, destStr, producer, positionFilter);
                    sendEndOfMessagesCommand(session, producer, totalBatch);
                } else if ("SendOnline".equalsIgnoreCase(commands[0].trim())) {
                    Filter<RtfOnlinePosition> positionFilter = createOnlineFilter(commands);
                    int totalOnline = processOnlinePositionRequest(session, destStr, producer, positionFilter);
                    sendEndOfMessagesCommand(session, producer, totalOnline);
                }
            } catch (JMSException e) {
                // We usually end up here when the Queue nolonger exists - client closed connection
                logger.error("Unable to publish for batch request", e);
            }
        } else {
            logger.error("No ReplyTo desitination, ignoring request");
        }
    }

    private void sendEndOfMessagesCommand(Session session, MessageProducer producer, int recordsSent)
        throws JMSException {
        TextMessage onlinePositionMessage = session
            .createTextMessage(createEndOfMessagesCommandMessage(recordsSent));
        producer.send(onlinePositionMessage);
    }

    public synchronized void publishBatchPositions() throws JMSException {
        // republish all of the batch positions in the cache.
        // for each topic send a refresh/purge command and then the messages
        int totalSent = 0;

        Map<String, Boolean> resetTopic = new HashMap<String, Boolean>();

        for (BatchPosition batchPosition : batchPositionCache.values()) {
            String topic = batchPosition.getAccount();
            if ("EMF".equals(topic) || "BEMFS".equals(topic) || topic.startsWith("TEST_")
                || "DONOTUSE".equals(topic)) {
                continue; // skip EMF and TEST positions
            }
            if (resetTopic.get(topic) == null) {
                logger.info("Reseting topic " + topic);
                // topicSession.publishToTopicLastImage(batchPosition);
                topicSession.publishCommandMessage(createJmsTopic(topic), batchPosition);
                resetTopic.put(topic, Boolean.TRUE);
            }
            // topicSession.publishToTopicLastImage(batchPosition);
            topicSession.publishPositionMessage(createJmsTopic(topic), batchPosition);

            totalSent++;
        }
        logger.info("Total batch records published " + totalSent + " for " + previousTradingDay);
    }

    private int processBatchPositionRequest(Session session, String destination, MessageProducer producer,
        Filter<BatchPosition> positionFilter) throws JMSException {
        int totalSent = 0;
        for (BatchPosition batchPosition : batchPositionCache.values()) {
            if (positionFilter.accept(batchPosition)) {
                TextMessage batchPositionMessage = session
                    .createTextMessage(createPositionMessage(batchPosition));
                producer.send(batchPositionMessage);
                totalSent++;
            }
        }
        logger.info("Sent " + totalSent + " batch positions to " + destination + " for trading day "
            + previousTradingDay);
        return totalSent;
    }

    private int processOnlinePositionRequest(Session session, String destination, MessageProducer producer,
        Filter<RtfOnlinePosition> positionFilter) throws JMSException {
        int totalSent = 0;
        for (RtfOnlinePosition onlinePosition : onlinePositionCache.values()) {
            if (positionFilter.accept(onlinePosition)) {
                TextMessage onlinePositionMessage = session
                    .createTextMessage(createPositionMessage(onlinePosition));
                producer.send(onlinePositionMessage);
                totalSent++;
            }
        }
        logger.info("Sent " + totalSent + " online positions to " + destination);
        return totalSent;
    }

    private Filter<BatchPosition> createBatchFilter(String[] commands) {
        if (commands.length > 1) {
            // We have an array of Strings that are key=value pairs
            Map<String, String> filterFields = new HashMap<String, String>();

            for (int i = 1; i < commands.length; i++) {
                String keyValue = commands[i];
                String[] parts = keyValue.split("=");
                filterFields.put(parts[0], parts[1]);
            }

            return BatchPosition.createFilter(filterFields);
        } else {
            return BatchPosition.createFilter(Collections.<String, String> emptyMap());
        }
    }

    private Filter<RtfOnlinePosition> createOnlineFilter(String[] commands) {
        if (commands.length > 1) {
            // We have an array of Strings that are key=value pairs
            Map<String, String> filterFields = new HashMap<String, String>();

            for (int i = 1; i < commands.length; i++) {
                String keyValue = commands[i];
                String[] parts = keyValue.split("=");
                filterFields.put(parts[0], parts[1]);
            }

            return RtfOnlinePosition.createFilter(filterFields);
        } else {
            return RtfOnlinePosition.createFilter(Collections.<String, String> emptyMap());
        }
    }

    public boolean isEndOfDayReceived() {
        return receivedEod;
    }

    public synchronized boolean forceReload(String nextTradingDay) {
        try {
            LocalDate nextLocalTradingDate = new LocalDate(nextTradingDay);
            receivedEod = true;
            this.nextTradingDay = nextLocalTradingDate;
            fromReloadTimer();
        } catch (Exception e) {
            logger.error("Unable to force reload", e);
            return false;
        }
        return true;
    }

    public synchronized String getCurrentTradingDay() {
        return currentTradingDay.toString();
    }

    public synchronized String getPreviousTradingDay() {
        return previousTradingDay.toString();
    }

    public synchronized String getNextTradingDay() {
        if (receivedEod) {
            return nextTradingDay.toString();
        } else {
            return "";
        }

    }

    /**
     * Reload the data.
     * <p/>
     * Replace the caches with today's batch file and set the online feed to the next trading day.
     */
    public synchronized void fromReloadTimer() {

        try {
            if (receivedEod) {
                // Create new caches - we can rollback if there is an issue
                BatchPositionCache newBatchPositionCache = new BatchPositionCache();
                // OnlinePositionCache newOnlinePositionCache = new OnlinePositionCache();
                OnlinePositionAggregateCache newOnlinePositionCache = new OnlinePositionAggregateCache();

                // roll to the next day
                previousTradingDay = currentTradingDay;
                currentTradingDay = nextTradingDay;

                loadBatchFile(batchFilename, previousTradingDay, newBatchPositionCache);
                onlinePositionCache = newOnlinePositionCache;
                batchPositionCache = newBatchPositionCache;
                publishBatchPositions();
                RtfMessage overRideRequest = new DefaultRtfMessage(new RtfHeader(RtfCommand.Override,
                    currentTradingDay, 1), null);
                toApp(RealtimeSessionFactory.getInstance().findSession(sessionID), overRideRequest);

                // Wait for the next EOD record
                receivedEod = false;
            } else {
                // Reload time fired and no end-of-day record received!
                LocalTime localTime = new LocalTime();
                localTime = localTime.plusMinutes(30);

                try {
                    RealTimeFeedClient.rescheduleBatchReload(this, localTime);
                } catch (SchedulerException e) {
                    logger.error("Unable to reschedule job", e);
                }

                emailer.emailDeveloperAndBusinessErrorMessage("Batchfile reload failed",
                    "Reload time fired, but EOD record not received." + "\nRescheduled for " + localTime);
            }
        } catch (IOException e) {
            logger.error("Unable to reload batch positions.", e);
            emailer.emailDeveloperErrorMessage("Batchfile reload failed",
                "Reload time fired, but there was an error reloading the file."
                    + "\nManually restart the server for the next trading day to start.", e);
        } catch (JMSException e) {
            logger.error("Unable to publish batch positions", e);
            logger.error("Unable to reload batch positions.", e);
            emailer.emailDeveloperErrorMessage("Batchfile reload failed",
                "Reload time fired, but there was an error publishing positions.", e);
        }
    }

    public synchronized void processEod(RtfEndOfDay eod) {
        logger.info("Received EOD record.  Current trading day=" + currentTradingDay);
        logger.info("EOD record =" + eod.toString());

        if (currentTradingDay.equals(eod.getMessageDate())) {
            receivedEod = true;
            nextTradingDay = eod.getNextTradeDate();
        }
        // We need to load the position batch file and republish
        if (receivedEod) {
            logger.info("Received end of day: " + eod);

        }

    }

    public void update(Observable o, Object arg) {
        // Something that we are observing has changed
        logger.info("Received update from " + o.getClass().getName() + " with arg of " + arg);
    }

    public String createPositionMessage(PositionRecord positionRecord) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("messageType=").append(positionRecord.getPositionType());
        sb.append("|").append(positionRecord.toTextMessage("|"));

        return sb.toString();
    }

    public String createPurgeCommandMessage(PositionRecord positionRecord) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("messageType=Command|purgeRecordType=").append(positionRecord.getPositionType());
        return sb.toString();
    }

    public String createEndOfMessagesCommandMessage(int recordsSent) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("messageType=Command|endOfMessages=").append(recordsSent);
        return sb.toString();
    }

    public String createHeartBeatMessage() {
        return "messageType=HeartBeat";
    }


}
