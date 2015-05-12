package malbec.fix;

import static malbec.util.DateTimeUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import malbec.fer.fix.FerFixClientApplication;
import malbec.fix.util.QfjHelper;
import malbec.fix.util.Slf4jLogFactory;
import malbec.util.EmailSettings;
import malbec.util.InvalidConfigurationException;
import malbec.util.TaskService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStore;
import quickfix.FileStoreFactory;
import quickfix.Group;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageStore;
import quickfix.MessageStoreFactory;
import quickfix.Session;
import quickfix.SessionFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.field.BeginSeqNo;
import quickfix.field.EndSeqNo;
import quickfix.field.MsgType;
import quickfix.field.NoPartyIDs;

/**
 * A FIX session is a connection to a counter-party.
 * 
 * QuickFIX/J uses (allows) one initiator to support multiple sessions. They provide a
 * <code>ThreadedSocketInitiator</code> to improve performance. This causes the events that are delivered to
 * the <code>Application</code> and the <code>SessionStateListener</code> to arrive in random order. For this
 * reason (and others) each <code>FixClientSession</code> will have it's own initiator that is single
 * threaded.
 * 
 */
public class FixClient implements FixClientMBean {

    public static final String CLIENT_END_DAY = "ClientEndDay";

    public static final String CLIENT_START_DAY = "ClientStartDay";

    public static final String CLIENT_END_TIME = "ClientEndTime";

    public static final String CLIENT_START_TIME = "ClientStartTime";

    private final String sessionName;

    private SessionID sessionID;
    /**
     * Store the session when we stop so we can reset the sequence numbers if need be
     */
    private Session oldSession;

    private EmailSettings emailSettings;

    private Logger log;

    private String loggerConfig;

    private SessionSettings qfjSettings;

    private Initiator initiator;

    private ClientSchedule schedule;

    private ScheduledFuture<?> monitorFuture;

    private FixClientApplication application;

    private Properties config;

    static {
        TaskService.getInstance().createAndAddSingleThreadScheduled("FixClient");
    }

    protected FixClient(String sessionName) {
        this.sessionName = sessionName;
        this.loggerConfig = this.sessionName;
        this.log = LoggerFactory.getLogger(loggerConfig);
    }

    public FixClient(String sessionName, FixClientApplication clientApp, Properties config) {
        this(sessionName);
        this.application = clientApp;
        this.config = config;

        clientApp.setFixLog(log);
        setPassword(clientApp, config);
        setRequiresLogon(clientApp, config);

        try {
            // setConfiguration(clientApp, config);
            createInitator();
            // Setup our own schedule
            schedule = new ClientSchedule();
            configureSchedule(schedule, config);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException("Cannot instantiate FixClient '" + sessionName + "'", e);
        }
    }

    private void setRequiresLogon(FixClientApplication clientApp, Properties config) {
        String requiresLogon = config.getProperty("RequiresLogon", "false");

        clientApp.setRequiresLogon(Boolean.valueOf(requiresLogon));
    }

    public Message createMessage(String msgType) {
        MessageFactory msgFactory = Session.lookupSession(sessionID).getMessageFactory();

        return msgFactory.create(sessionID.getBeginString(), msgType);
    }

    private void setPassword(FixClientApplication clientApp, Properties config) {
        String userID = config.getProperty("UserID");
        String password = config.getProperty("Password");
        clientApp.setUserID(userID);
        clientApp.setPassword(password);
    }

    Initiator getInitiator() {
        return initiator;
    }

    private void createInitatorSafe() {
        try {
            createInitator();
        } catch (InvalidConfigurationException e) {
            log.error("You should not be calling this method with errors", e);
        }
    }

    private void createInitator() throws InvalidConfigurationException {

        if (config.containsKey(SessionFactory.SETTING_CONNECTION_TYPE)
            && !"initiator".equals(config.getProperty(SessionFactory.SETTING_CONNECTION_TYPE))) {
            throw new InvalidConfigurationException("Connector type not set to 'initiator' - "
                + config.getProperty(SessionFactory.SETTING_CONNECTION_TYPE));
        }

        // add custom logging if not already specified -- allows us to have
        // different log files per client
        if (!config.containsKey(Slf4jLogFactory.SETTING_INMSG_CATEGORY)) {
            config.setProperty(Slf4jLogFactory.SETTING_INMSG_CATEGORY, loggerConfig);
        }
        if (!config.containsKey(Slf4jLogFactory.SETTING_OUTMSG_CATEGORY)) {
            config.setProperty(Slf4jLogFactory.SETTING_OUTMSG_CATEGORY, loggerConfig);
        }

        qfjSettings = QfjHelper.createSessionSettings(config);

        MessageStoreFactory msgStoreFactory = new MemoryStoreFactory();

        if (config.containsKey(FileStoreFactory.SETTING_FILE_STORE_PATH)) {
            log.info("Using FileStoreFactory");
            msgStoreFactory = new FileStoreFactory(qfjSettings);
        }

        MessageFactory messageFactory = new DefaultMessageFactory();
        LogFactory logFactory = new Slf4jLogFactory(qfjSettings);
        try {
            initiator = new SocketInitiator(application, msgStoreFactory, qfjSettings, logFactory,
                messageFactory);
        } catch (ConfigError e) {
            throw new InvalidConfigurationException("Unable to create initiator", e);
        }

        // Extract the session ID - we only have one, so take the first
        ArrayList<SessionID> allSessions = initiator.getSessions();
        sessionID = allSessions.get(0);
    }

    public FixClient(String sessionName, Properties config, EmailSettings emailSettings) {
        this(sessionName, new FerFixClientApplication(emailSettings), config);
    }

    public String getSessionName() {
        return sessionName;
    }

    public String getSessionIDString() {
        return sessionID.toString();
    }

    public String getSenderCompId() {
        return sessionID.getSenderCompID();
    }

    public String getTargetCompId() {
        return sessionID.getTargetCompID();
    }

    /**
     * Sets the logging configuration to use for QuickFIX/J events.
     * 
     * @param config
     */
    public void setLoggerConfig(String config) {
        this.loggerConfig = config;
        this.log = LoggerFactory.getLogger(loggerConfig);
    }

    public void start() {
        synchronized (schedule) {
            if (monitorFuture == null || monitorFuture.isCancelled()) {
                if (initiator == null) {
                    // To work around how QFJ works, create a new one to start
                    // Should only happen after we stop via JMX.
                    // QFJ 1.3.3 should have a fix for this
                    createInitatorSafe();
                    oldSession = null;
                }
                // start a monitor that ensures the client is running when it should be
                ScheduledExecutorService executor = (ScheduledExecutorService) TaskService.getInstance()
                    .getExecutor("FixClient");
                monitorFuture = executor.scheduleAtFixedRate(new ClientSessionMonitor(schedule, this, log),
                    0, 1, TimeUnit.SECONDS);
                StringBuilder sb = new StringBuilder(256);
                DateTime startTime = getClientStartTime();
                DateTime endTime = getClientEndTime();

                sb.append("Client starts on ").append(
                    startTime.property(DateTimeFieldType.dayOfWeek()).getAsText());
                sb.append(" at ").append(startTime.toLocalTime());
                sb.append(", ends on ").append(endTime.property(DateTimeFieldType.dayOfWeek()).getAsText());
                sb.append(" at ").append(endTime.toLocalTime());
                log.info(sb.toString());
                log.info("Client configured for: " + getClientStartTime() + " to " + getClientEndTime());
            }
        }
    }

    public void setConfiguration(Properties config) throws InvalidConfigurationException {
        emailSettings = new EmailSettings(config);

        setConfiguration(new FerFixClientApplication(emailSettings), config);

    }

    /**
     * Leaving this as package scope until I change the tests
     * 
     * @param app
     * @param config
     * @throws InvalidConfigurationException
     * @Deprecated
     */
    void setConfiguration(FixClientApplication app, Properties config) throws InvalidConfigurationException {
        this.application = app;
        this.config = config;

        createInitator();

        // Setup our own schedule
        schedule = new ClientSchedule();
        configureSchedule(schedule, config);
    }

    private void configureSchedule(ClientSchedule schedule, Properties config)
        throws InvalidConfigurationException {
        // extract the StartDay/EndDay/StartTime/EndTime
        // Do the time next
        String clientStartTime = config.getProperty(CLIENT_START_TIME);
        String clientEndTime = config.getProperty(CLIENT_END_TIME);

        if (clientStartTime == null || clientEndTime == null) {
            clientStartTime = config.getProperty(Session.SETTING_START_TIME);
            clientEndTime = config.getProperty(Session.SETTING_END_TIME);

            if (clientStartTime == null || clientEndTime == null) {
                throw new InvalidConfigurationException(
                    "Unable to configure client schedule, missing start/end time");
            }
        }
        // Add in a client schedule start date/time
        String clientStartDay = config.getProperty(CLIENT_START_DAY);
        String clientEndDay = config.getProperty(CLIENT_END_DAY);

        if (clientStartDay == null || clientEndDay == null) {
            clientStartDay = config.getProperty(Session.SETTING_START_DAY);
            clientEndDay = config.getProperty(Session.SETTING_END_DAY);
            if (clientStartDay == null && clientEndDay == null) {
                // If there is no schedule, we cannot assume Monday and Friday, Monday
                // is OK, but Friday will cause us to miss Friday, need to use Saturday
                clientStartDay = "Monday";
                if (clientEndTime.startsWith("00:00:00")) {
                    clientEndDay = "Saturday";
                } else {
                    clientEndDay = "Friday";
                }
            }
        }

        DateTime[] range = determineWeeklyRange(clientStartDay, clientStartTime, clientEndDay, clientEndTime);
        schedule.setStartTime(range[0]);
        schedule.setEndTime(range[1]);
    }

    public boolean isLoggedOn() {
        return (initiator != null && initiator.isLoggedOn());
    }

    public void stop() {
        // stop the session monitor from being called again
        if (monitorFuture != null) {
            monitorFuture.cancel(false);
            monitorFuture = null;

            stopInitiator();
            // We have been stopped, have the start create a new instance
            // QFJ 1.3.3 should have a fix for this
            initiator = null;

        }
    }

    private void closeFilesIfFileStore(MessageStore ms) {
        if (ms instanceof FileStore) {
            FileStore fs = (FileStore) ms;
            try {
                fs.closeFiles();
            } catch (IOException e) {
                log.error("Unable to close FileStore files", e);
            }
        }
    }

    public boolean isRunning() {
        // We only care if the future will not be scheduled to run again, we don't care that it has finished
        // its current iteration, or is in the middle of an iteration
        return (monitorFuture != null && !monitorFuture.isCancelled());
    }

    private static final class ClientSessionMonitor implements Runnable {

        private final ClientSchedule schedule;
        private final FixClient fixClient;
        private final Logger log;

        private boolean initiatorStarted;

        ClientSessionMonitor(ClientSchedule cs, FixClient fixClient, Logger log) {
            this.schedule = cs;
            this.fixClient = fixClient;
            this.log = log;
        }

        @Override
        public void run() {
            // Do we need to start the connection?
            if (schedule.isWithinSchedule() && !initiatorStarted) {
                try {
                    // If we are not the initial start, we need to create a new initiator
                    if (fixClient.getInitiator() == null) {
                        fixClient.createInitatorSafe();
                    }
                    fixClient.getInitiator().start();
                    initiatorStarted = true;
                    log.info("Starting client session");
                } catch (ConfigError e) {
                    log.error("Unable to start connection", e);
                }
                // Do we need to stop the connection
            } else if (!schedule.isWithinSchedule() && initiatorStarted) {
                // get the session so we can have it cleanup after being unregistered
                fixClient.stopInitiator();
                initiatorStarted = false;
                log.info("Stopping client session");
            } else { // we are either stopped or started, either way we are where we want to be.
                if (schedule.advanceSchedule()) {
                    log.info("Advancing schedule: " + schedule.toString());
                }
            }
        }
    }

    public void resetSequenceNumbers() {
        if (initiator != null) {
            // get the session for this client - we should only have one
            ArrayList<SessionID> sessionIDs = initiator.getSessions();

            for (SessionID sessionID : sessionIDs) {
                Session mySession = Session.lookupSession(sessionID);
                // ensure that the session has not be 'unregistered' - closed
                if (mySession != null) {
                    resetSessionSequenceNumbers(mySession);
                }
            }
        } else if (oldSession != null) {
            resetSessionSequenceNumbers(oldSession);
            // The reset re-opens the files, make sure we close them
            closeFilesIfFileStore(oldSession.getStore());
        } else {
            log.warn("No session, unable to reset sequence numbers: " + sessionID);
        }
    }

    public void stopInitiator() {
        oldSession = Session.lookupSession(sessionID);
        if (initiator != null) {
            initiator.stop();
        }
        if (oldSession != null) {
            MessageStore ms = oldSession.getStore();
            closeFilesIfFileStore(ms);
        }
        initiator = null;
    }

    private void resetSessionSequenceNumbers(Session oldSession) {
        try {
            MessageStore ms = oldSession.getStore();
            closeFilesIfFileStore(ms);
            ms.reset();
        } catch (IOException e) {
            log.error("Unable to reset sequence numbers", e);
        }
    }

    @Override
    public void setSenderSequenceNumber(int newSequenceNumber) {
        Session session = getCurrentOrLastSession();
        try {
            session.getStore().setNextSenderMsgSeqNum(newSequenceNumber);
        } catch (IOException e) {
            log.error("Unable to set sender sequence number", e);
        }
    }

    @Override
    public void setTargetSequenceNumber(int newSequenceNumber) {
        Session session = getCurrentOrLastSession();
        try {
            session.getStore().setNextTargetMsgSeqNum(newSequenceNumber);
        } catch (IOException e) {
            log.error("Unable to set target sequence number", e);
        }
    }

    @Override
    public int getSenderSequenceNumber() {
        Session session = getCurrentOrLastSession();
        if (session != null) {
            try {
                return session.getStore().getNextSenderMsgSeqNum();
            } catch (IOException e) {
                log.error("Unable to determine sender sequence number", e);
            }
        }
        return -1;
    }

    @Override
    public int getTargetSequenceNumber() {
        Session session = getCurrentOrLastSession();
        if (session != null) {
            try {
                return session.getStore().getNextTargetMsgSeqNum();
            } catch (IOException e) {
                log.error("Unable to determine target sequence number", e);
            }
        }
        return -1;
    }

    private Session getCurrentOrLastSession() {
        // get the session for this client - we should only have one
        if (initiator != null) {
            ArrayList<SessionID> sessionIDs = initiator.getSessions();

            for (SessionID sessionID : sessionIDs) {
                Session mySession = Session.lookupSession(sessionID);
                // ensure that the session has not be 'unregistered' - closed
                if (mySession != null) {
                    return mySession;
                }
            }
        } else if (oldSession != null) {
            return oldSession;
        }

        return null;
    }

    public boolean sendOrder(Message fixMessage) {
        // find the session and send the order
        Session ourSession = Session.lookupSession(sessionID);
        if (ourSession != null) {
            return ourSession.send(fixMessage);
        } else {
            return false;
        }
    }

    public Application getApplication() {
        return application;
    }

    public DateTime getClientStartTime() {
        return schedule.getStartTime();
    }

    public DateTime getClientEndTime() {
        return schedule.getEndTime();
    }

    public DateTime getSessionCreationTime() {
        Session session = Session.lookupSession(sessionID);
        if (session != null) {
            try {
                return new DateTime(session.getStartTime());
            } catch (IOException e) {
                log.error("Unable to retrieve session startTime", e);
            }
        }

        return null;
    }

    public Group createNumberOfPartiesGroup() {
        MessageFactory msgFactory = Session.lookupSession(sessionID).getMessageFactory();

        return msgFactory.create(sessionID.getBeginString(), "D", NoPartyIDs.FIELD);
    }

    public boolean isActiveSession() {
        Session session = Session.lookupSession(sessionID);

        return session.isSessionTime() && session.isLoggedOn();
    }

    @Override
    public void requestResend(int startSequenceNumber) {
        // getTargetSequenceNumber() -- replaced with zero (0) infinity
        requestResend(startSequenceNumber, 0);
    }

    @Override
    public void requestResend(int startSequenceNumber, int endSequenceNumber) {
        // ensure we are 0 or greater
        endSequenceNumber = Math.max(0, endSequenceNumber);
        startSequenceNumber = Math.max(1, startSequenceNumber);

        Session session = Session.lookupSession(sessionID);
        Message resendRequest = session.getMessageFactory().create(sessionID.getBeginString(),
            MsgType.RESEND_REQUEST);

        resendRequest.setInt(BeginSeqNo.FIELD, startSequenceNumber);
        resendRequest.setInt(EndSeqNo.FIELD, endSequenceNumber);

        StringBuilder logMessage = new StringBuilder(256);
        logMessage.append("Sending resend request for: ").append(startSequenceNumber);
        logMessage.append(" to ");
        if (endSequenceNumber == 0) {
            logMessage.append("infinity");
        } else {
            logMessage.append(endSequenceNumber).append(" total messages: ");
            logMessage.append(endSequenceNumber - startSequenceNumber + 1);
        }
        log.info(logMessage.toString());

        session.send(resendRequest);
    }

    @Override
    public String getScheduleSummary() {
        if (schedule != null) {
            return schedule.toString();
        }

        return "No schedule set";
    }

    public boolean isSessionTime() {
        Session session = Session.lookupSession(sessionID);

        return session.isSessionTime();
    }

}
