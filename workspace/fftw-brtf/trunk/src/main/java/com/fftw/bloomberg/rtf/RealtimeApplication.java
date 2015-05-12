package com.fftw.bloomberg.rtf;

import static malbec.util.DateTimeUtil.isWeekend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import malbec.fer.mapping.DatabaseMapper;
import malbec.util.FuturesSymbolUtil;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fftw.bloomberg.OnlinePositionKey;
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
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.jms.JmsTopicPublisherSession;
import com.fftw.jms.client.JmsApplication;
import com.fftw.jms.client.QueueListener;
import com.fftw.positions.ISecurity;
import com.fftw.positions.Position;
import com.fftw.positions.cache.DefaultPositionAggregationStrategy;
import com.fftw.positions.cache.ExcelPositionCache;
import com.fftw.positions.cache.OnlineAccountStrategyAggregationStrategy;
import com.fftw.positions.cache.OnlinePositionAggregationCache;
import com.fftw.positions.cache.PositionAggregationCache;
import com.fftw.positions.cache.PrimeBrokerPositionAggregationStrategy;
import com.fftw.positions.cache.ReconAggregationStrategy;
import com.fftw.positions.cache.SecurityRootPositionAggregationStrategy;
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

    // The original position cache for the AIM Excel spreadsheet
    private ExcelPositionCache excelPositionCache = new ExcelPositionCache();

    private PositionAggregationCache rootPositionCache = createSecurityRootCache();
    // private SecurityRootPositionCache rootPositionCache = new SecurityRootPositionCache();
    // private PrimeBrokerPositionCache primeBrokerPositionCache = new PrimeBrokerPositionCache();
    private PositionAggregationCache primeBrokerPositionCache = createPrimeBrokerCache();
    
    private PositionAggregationCache defaultPositionCache = createDefaultCache();

    // Aggregated caches of the OnlinePositions
    // All items in here should have their tags set to the EMPTY_STRING
    private OnlinePositionAggregationCache<OnlinePositionKey> rediOnlineCache = new OnlinePositionAggregationCache<OnlinePositionKey>(
        new ReconAggregationStrategy());

    private OnlinePositionAggregationCache<OnlinePositionKey> tradingScreenOnlineCache = new OnlinePositionAggregationCache<OnlinePositionKey>(
        new OnlineAccountStrategyAggregationStrategy());
   
    public RealtimeApplication() {

    }

    private PositionAggregationCache createSecurityRootCache() {
        return createSecurityRootCache(Collections.<BatchPosition> emptyList());
    }

    private PositionAggregationCache createSecurityRootCache(Collection<BatchPosition> batchPositions) {
        return new PositionAggregationCache(new SecurityRootPositionAggregationStrategy(), batchPositions);
    }

    private PositionAggregationCache createPrimeBrokerCache() {
        return createPrimeBrokerCache(Collections.<BatchPosition> emptyList());
    }

    private PositionAggregationCache createDefaultCache() {
        return createDefaultCache(Collections.<BatchPosition> emptyList());
    }
    
    private PositionAggregationCache createPrimeBrokerCache(Collection<BatchPosition> batchPositions) {
        return new PositionAggregationCache(new PrimeBrokerPositionAggregationStrategy(), batchPositions);
    }

    private PositionAggregationCache createDefaultCache(Collection<BatchPosition> batchPositions) {
        return new PositionAggregationCache(new DefaultPositionAggregationStrategy(), batchPositions);
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

    public synchronized void setPreviousTradingDay(LocalDate day, boolean useCurrentTradingDay) {
        if (useCurrentTradingDay) {
            this.previousTradingDay = previouseClose(day);
        } else {
            setPreviousTradingDay(day);
        }
    }

    public synchronized void setPreviousTradingDay(LocalDate previousTradingDay) {
        this.previousTradingDay = previousTradingDay;
    }

    public synchronized void startPositionPublisher() throws JMSException {
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
        LocalDate tradingDay = previousTradingDay;

        if (previousTradingDay == null) {
            tradingDay = previouseClose(currentTradingDay);
        }
        List<BatchPosition> filePositions = loadBatchFile(batchFilename, tradingDay);
        excelPositionCache = new ExcelPositionCache(filePositions);
        rootPositionCache = createSecurityRootCache(filePositions);

        primeBrokerPositionCache = createPrimeBrokerCache(filePositions);
        defaultPositionCache = createDefaultCache(filePositions);
        
        //defaultByInternalSymbolPositionCache = createDefaultWithTranslatedSymbols(filePositions);

        return true;
    }

    List<BatchPosition> loadBatchFile(String filename, LocalDate batchFileDay) throws IOException {
        if (filename.contains("${date}")) {
            filename = filename.replace("${date}", batchFileDay.toString("MM-dd-yy"));
        }

        File batchFile = new File(filename);
        FileReader fr = new FileReader(batchFile);

        logger.info("Loading batchfile: " + batchFile.getAbsoluteFile());
        BufferedReader batchPositionReader = new BufferedReader(fr);

        List<BatchPosition> positions = new LinkedList<BatchPosition>();
        String line = "";
        while ((line = batchPositionReader.readLine()) != null) {
            BatchPosition bp = BatchPosition.valueOf(batchFileDay, line);
            // Only load Quantys fund data
            if ("QMF".equalsIgnoreCase(bp.getAccount())) {
                positions.add(bp);
            }
        }
        batchPositionReader.close();

        return positions;
    }

    private LocalDate previouseClose(LocalDate date) {
        LocalDate previous = date.minusDays(1);

        while (isWeekend(previous)) {
            previous = previous.minusDays(1);
        }
        return previous;
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
    public synchronized void fromApp(RealtimeProtocolSession session, RtfMessage message) {
        logger.info("Received message-header:" + message.getHeader());
        logger.info("Received message-body:" + message.getBody());

        if (RtfCommand.Data == message.getHeader().getCommand()) {
            RtfMessageBody body = message.getBody();

            if (body.getMessageType() == '4') {
                // We have a data record, publish it to the topic
                processDataRecord((RtfOnlinePosition) body);
            } else if (message.getBody().getMessageType() == '5') {
                processEod((RtfEndOfDay) body);
            }
        } else if (RtfCommand.Status == message.getHeader().getCommand()) {
            // Where do we publish heartbeat messages?
            topicSession.publishHeartBeat();
        }
    }

    private synchronized void processDataRecord(RtfOnlinePosition onlinePosition) {
        if (!"QMF".equals(onlinePosition.getAccount()) && !"TEST".equalsIgnoreCase(onlinePosition.getAccount())) {
            logger.info("Not publishing " + onlinePosition.getAccount() + " position");
            return;
        }

        // lookup the contract size from Bloomberg
        if (onlinePosition.isCommodity() || onlinePosition.isCurrency()) {
            onlinePosition.setContractSize(getContractSize(onlinePosition));
        }

        // Lookup the ticker - if this is a new position we do not have a ticker (the ticker
        // comes from the static data)
        if (onlinePosition.isEquity() || onlinePosition.isIndex() || onlinePosition.isCorporate()
            || (onlinePosition.isCurrency() && !onlinePosition.getPrimeBroker().equalsIgnoreCase("NO PB"))) {
            onlinePosition.setExchangeTicker(getExchangeTicker(onlinePosition));
        }
        logger.info(onlinePosition.toRawString());

        // Add the new record to all the caches
        RtfOnlinePosition aggregated = excelPositionCache.addOnlinePosition(onlinePosition.copy());
        
        Position securityRoot = rootPositionCache.addOnlinePosition(onlinePosition.copy());
        Position primeBroker = primeBrokerPositionCache.addOnlinePosition(onlinePosition.copy());
        Position defaultPosition = defaultPositionCache.addOnlinePosition(onlinePosition.copy());
        
        // Only add securities for Redi and Recon is interested in to the Redi cache
        if (isRediSecurity(onlinePosition)) {
            RtfOnlinePosition reconPosition = rediOnlineCache.addOnlinePosition(onlinePosition.copy());
            topicSession.publishRediMessage(reconPosition);
        }
        if (isTradingScreenSecurity(onlinePosition)) {
            RtfOnlinePosition tsPosition = tradingScreenOnlineCache.addOnlinePosition(onlinePosition.copy());
            topicSession.publishAccountStrategyMessage(tsPosition);
        }
        
        publishSecurityRootUpdate(securityRoot);
        publishPrimeBroker(primeBroker);
        publishDefaultPosition(defaultPosition);
        publishDefaultSymbolYelloKeyPosition(defaultPosition);

        topicSession.publishPositionMessage(createJmsTopic(onlinePosition.getAccount()), aggregated);
        positionLogger.info(onlinePosition.toString());
    }

    private boolean isTradingScreenSecurity(RtfOnlinePosition onlinePosition) {
        if (onlinePosition.getProductCode() == BBProductCode.Mortgage) {
            return false;
        }
        if ("MFPB".equalsIgnoreCase(onlinePosition.getPrimeBroker()) || 
            "MFFUT".equalsIgnoreCase(onlinePosition.getPrimeBroker()) ||
            "CITIFUT".equalsIgnoreCase(onlinePosition.getPrimeBroker())) {
            return true;
        }
        return false;
    }

    /**
     * This is used to get the online position when using the openPositon as part of the key
     * @param position
     * @return
     */
    @Deprecated
    protected RtfOnlinePosition getDefaultOnlinePosition(RtfOnlinePosition position) {
        return defaultPositionCache.getOnlinePosition(defaultPositionCache.getAggregateKeyForItem(position));
    }
    
    private void publishSecurityRoot(Position position) {
        topicSession.publishPositionMessage(createSecurityRootTopic(position), position.toTextMessage("|"));
    }

    private void publishPrimeBroker(Position batchPosition) {
        topicSession.publishPositionMessage(createPrimeBrokerTopic(batchPosition), batchPosition
            .toTextMessage("|"));
    }

    private void publishDefaultPosition(Position batchPosition) {
        topicSession.publishPositionMessage(createDefaultPositionTopic(batchPosition), batchPosition
            .toTextMessage("|"));
    }
    
    private void publishDefaultSymbolYelloKeyPosition(Position position) {
        topicSession.publishPositionMessage(createDefaultSymbolYellowKeyPositionTopic(position), position
            .toTextMessage("|"));
    }
    
    private void publishSecurityRootUpdate(Position onlinePosition) {
        String combinedPositionString = onlinePosition.toTextMessage("|");

        topicSession.publishPositionMessage(createSecurityRootTopic(onlinePosition), combinedPositionString);
    }

    private String getExchangeTicker(RtfOnlinePosition onlinePosition) {
        return SystematicFacade.lookupExchangeTicker(onlinePosition.getSecurityId());
    }

    private boolean isRediSecurity(RtfOnlinePosition onlinePosition) {
        // currently all equity trades -- this might be able to be MSPB
        if (onlinePosition.isEquity()) {
            return true;
        }

        // If we have a mapped RIC code we traded it via Redi
        if ("GSFUT".equalsIgnoreCase(onlinePosition.getPrimeBroker())) {
//        if (onlinePosition.isIndex() || onlinePosition.isCommodity() || onlinePosition.isCurrency()) {
            String reconBid = onlinePosition.getReconBloombergId();
            String bloombergRoot = FuturesSymbolUtil.extractSymbolRoot(reconBid);
            String ricRoot = dbm.mapBloombergRootToPlatformSendingRoot("REDI", bloombergRoot);
            
            if (ricRoot != null) {
                String monthYear = FuturesSymbolUtil.extractMaturityMonthFromSymbol(reconBid);
                onlinePosition.setRic(FuturesSymbolUtil.combineRootMaturityMonthYear(ricRoot, monthYear));
                return true;
            } else {
                logger.warn("Prime Broker is GSFUT, but no RIC code: " + bloombergRoot);
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

    public synchronized void publishExcelBatchPositions() throws JMSException {
        // republish all of the batch positions in the cache.
        // for each topic send a refresh/purge command and then the messages
        int totalSent = 0;

        Map<String, Boolean> resetTopic = new HashMap<String, Boolean>();

        for (BatchPosition batchPosition : excelPositionCache.batchValues()) {
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

    public synchronized void publishSecurityRootPositions() throws JMSException {
        int totalSent = 0;

        for (Position position : rootPositionCache.values()) {
            if (!"QMF".equals(position.getAccount())) {
                continue; // skip EMF and TEST positions
            }

            publishSecurityRoot(position);

            totalSent++;
        }
        logger.info("Total batch records published " + totalSent + " for " + previousTradingDay);
    }

    public synchronized void publishPrimeBrokerBatchPositions() throws JMSException {
        int totalSent = 0;

        for (Position batchPosition : primeBrokerPositionCache.values()) {
            if (!"QMF".equals(batchPosition.getAccount())) {
                continue; // skip EMF and TEST positions
            }

            publishPrimeBroker(batchPosition);

            totalSent++;
        }
        logger.info("Total batch records published " + totalSent + " for " + previousTradingDay);
    }

    public synchronized void publishDefaultPositionBatchPositions() throws JMSException {
        int totalSent = 0;

        for (Position batchPosition : defaultPositionCache.values()) {
            if (!"QMF".equals(batchPosition.getAccount())) {
                continue; // skip EMF and TEST positions
            }

            publishDefaultPosition(batchPosition);

            totalSent++;
        }
        logger.info("Total batch records published " + totalSent + " for " + previousTradingDay);
    }

    public synchronized void publishDefaultSymbolYelloKeyPositionBatchPositions() throws JMSException {
        int totalSent = 0;

        for (Position batchPosition : defaultPositionCache.values()) {
            if (!"QMF".equals(batchPosition.getAccount())) {
                continue; // skip EMF and TEST positions
            }

            publishDefaultSymbolYelloKeyPosition(batchPosition);

            totalSent++;
        }
        logger.info("Total batch records published " + totalSent + " for " + previousTradingDay);
    }
    
    private String createSecurityRootTopic(Position position) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Positions.SecurityRoot.");
        sb.append(position.getAccount()).append(".");
        sb.append(position.getLevel1TagName()).append(".");

        ISecurity security = position.getSecurity();

        sb.append(security.getProductCode().getShortString()).append(".");
        sb.append(security.getSecurityId());

        return sb.toString();
    }

    private String createPrimeBrokerTopic(Position position) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Positions.PrimeBroker.");
        sb.append(position.getAccount()).append(".");
        sb.append(position.getPrimeBroker()).append(".");

        ISecurity security = position.getSecurity();
        sb.append(security.getProductCode().getShortString()).append(".");

        // publish equities using the ticker instead, they should never be null,
        // as we look them up via the API when we receive a new OnlinePosition
        if (security.getProductCode() == BBProductCode.Equity && security.getTicker() != null) {
            sb.append(security.getTicker());
        } else {
            sb.append(security.getSecurityId());
        }

        return sb.toString();
    }

    private String createDefaultPositionTopic(Position position) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Positions.RawAim.");
        sb.append(position.getAccount()).append(".");

        ISecurity security = position.getSecurity();
        sb.append(security.getProductCode().getShortString()).append(".");

        // publish equities using the ticker instead, they should never be null,
        // as we look them up via the API when we receive a new OnlinePosition
        if (security.getProductCode() == BBProductCode.Equity && security.getTicker() != null) {
            sb.append(security.getTicker());
        } else {
            sb.append(security.getSecurityId());
        }

        sb.append(".").append(position.getOpenPosition());
        return sb.toString();
    }
    
    private String createDefaultSymbolYellowKeyPositionTopic(Position position) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Positions.");
        sb.append(position.getAccount()).append(".");
        sb.append(position.getLevel1TagName()).append(".");

        ISecurity security = position.getSecurity();
        // publish equities using the ticker instead, they should never be null,
        // as we look them up via the API when we receive a new OnlinePosition
        if (security.getProductCode() == BBProductCode.Equity && security.getTicker() != null) {
            sb.append(security.getTicker());
        } else {
            sb.append(security.getSecurityId());
        }

        sb.append("_").append(security.getProductCode().getShortString());
        
        return sb.toString();
    }
    
    private synchronized int processBatchPositionRequest(Session session, String destination,
        MessageProducer producer, Filter<BatchPosition> positionFilter) throws JMSException {
        int totalSent = 0;
        for (BatchPosition batchPosition : excelPositionCache.batchValues()) {
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

    private synchronized int processOnlinePositionRequest(Session session, String destination,
        MessageProducer producer, Filter<RtfOnlinePosition> positionFilter) throws JMSException {
        int totalSent = 0;
        for (RtfOnlinePosition onlinePosition : excelPositionCache.onlineValues()) {
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
                // roll to the next day
                previousTradingDay = currentTradingDay;
                currentTradingDay = nextTradingDay;

                List<BatchPosition> newFilePositions = loadBatchFile(batchFilename, previousTradingDay);
                excelPositionCache = new ExcelPositionCache(newFilePositions);
                rootPositionCache = createSecurityRootCache(newFilePositions);
                primeBrokerPositionCache = createPrimeBrokerCache(newFilePositions);
                defaultPositionCache = createDefaultCache(newFilePositions);

                rediOnlineCache = new OnlinePositionAggregationCache<OnlinePositionKey>(new ReconAggregationStrategy());
                
                tradingScreenOnlineCache = new OnlinePositionAggregationCache<OnlinePositionKey>(new OnlineAccountStrategyAggregationStrategy());

                publishExcelBatchPositions();
                publishSecurityRootPositions();
                publishDefaultPositionBatchPositions();
                publishDefaultSymbolYelloKeyPositionBatchPositions();
                
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
        } catch (Exception e) {
            logger.error("Unable to reload batch positions.", e);
            emailer.emailDeveloperErrorMessage("Batchfile reload failed",
                "Reload time fired, but there was an error loading positions.", e);
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
