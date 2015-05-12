package malbec.fix;

import static malbec.fix.FixClientStatus.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import malbec.fix.util.Slf4jLog;
import malbec.util.DateTimeUtil;
import malbec.util.EmailSender;
import malbec.util.EmailSettings;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Log;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionStateListener;

public class FixClientStateListener implements SessionStateListener, PropertyChangeListener {

    private static final String DISCONNECT_KEY = "Disconnect";

    private static final String CONNECTION_REFUSED_KEY = Slf4jLog.CONNECTION_REFUSED;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SessionID sessionID;

    private final EmailSettings emailSettings;

    private FixClientStatus status = DISCONNECTED;

    private boolean badDisconnect;
    private DateTime badDisconenctTime;
    
    private String logoutText;
    
    private boolean connectionRefused;

    private Map<String, DateTime> lastErrorReportMap = new HashMap<String, DateTime>();

    private long errorReportInterval = 1000 * 60 * 30; // 30 minute interval

    @SuppressWarnings("unused")
    private FixClientStateListener() {
        emailSettings = null;
        sessionID = null;
    }

    public FixClientStateListener(SessionID sessionID, EmailSettings emailSettings) {
        this.sessionID = sessionID;
        this.emailSettings = emailSettings;

        Session session = Session.lookupSession(getSessionID());

        if (session != null) {
            Log log = session.getLog();
            if (log != null && log instanceof Slf4jLog) {
                Slf4jLog tmpLog = (Slf4jLog) log;
                tmpLog.addPropertyChangeListener(this);
            }
        } else {
            log.warn("Failed to register property listener on session: " + sessionID);
        }
    }

    @Override
    public void onConnect() {
        if (status != DISCONNECTED && status != LOGGED_OUT) {
            logStateChangeWarning(status, CONNECTED);
        }
        status = CONNECTED;
        
    }

    @Override
    public void onDisconnect() {
        if (status != LOGGED_ON && status != CONNECTED) {
            logStateChangeWarning(status, DISCONNECTED);
        }
        status = DISCONNECTED;

        // If we are not expecting to be disconnected, send out and email
        Session mySession = Session.lookupSession(sessionID);

        if (mySession != null && mySession.isSessionTime() && !mySession.isLogoutSent()
                && shouldReport(DISCONNECT_KEY)) {
            // should be a remote server disconnect (either they crashed or the network is bad)
            badDisconnect = true;
            badDisconenctTime = new DateTime();

            log.error("Something bad happened, report it");
            EmailSender sender = new EmailSender(emailSettings.getAsProperties());

            StringBuilder sb = new StringBuilder(1024);
            sb.append("Disconnected from session ").append(sessionID.getTargetCompID()).append(".\n\n");
            if (logoutText != null) {
                sb.append(logoutText).append("\n");
            }
            sb.append("Sending email when reconnected, or after ");
            sb.append(this.errorReportInterval / (1000 * 60)).append(" minutes.\n");

            sender.sendMessage("Disconnected from " + sessionID.getTargetCompID(), sb.toString());
            reportedError(DISCONNECT_KEY);
        } else if (mySession == null) {
            log.error("Disconnect event without a valid session");
        }
    }

    /**
     * We do not want to spam ourselves with email, so make sure we have not reported an error recently.
     * 
     * @return
     */
    private boolean shouldReport(String errorType) {
        DateTime lastErrorReport = lastErrorReportMap.get(errorType);

        if (lastErrorReport == null) {
            return true;
        }

        return (lastErrorReport.getMillis() + errorReportInterval <= System.currentTimeMillis());
    }

    private void reportedError(String errorType) {
        lastErrorReportMap.put(errorType, new DateTime());
    }

    private void clearError(String errorType) {
        lastErrorReportMap.put(errorType, null);
    }
    
    @Override
    public void onLogon() {
        if (status != CONNECTED) {
            logStateChangeWarning(status, LOGGED_ON);
        }
        status = LOGGED_ON;
        logoutText = null;
        if (badDisconnect) {
            badDisconnect = false;
            log.error("Reconnected after bad disconnect");
            StringBuilder sb = new StringBuilder(1024);
            sb.append("Re-connected to ").append(sessionID.getTargetCompID()).append("\n\n");
            sb.append("Session disconnected at ").append(DateTimeUtil.format(badDisconenctTime));
            EmailSender sender = new EmailSender(emailSettings.getAsProperties());
            sender.sendMessage("Re-connected to " + sessionID.getTargetCompID(), sb.toString());
            clearError(DISCONNECT_KEY);
        } else if (connectionRefused) {
            connectionRefused = false;
            log.error("Connected after refused connections");
            StringBuilder sb = new StringBuilder(1024);
            sb.append("Connected to ").append(sessionID.getTargetCompID()).append("\n\n");
            sb.append("Connections were being refused.");
            EmailSender sender = new EmailSender(emailSettings.getAsProperties());
            sender.sendMessage("Connected to " + sessionID.getTargetCompID(), sb.toString());
            clearError(CONNECTION_REFUSED_KEY);
        }
    }

    /**
     * This are delegates from the <code>Application</code>.
     * 
     * @param sessionID
     */
    public void onLogon(SessionID sessionID) {
        if (status != CONNECTED) {
            logStateChangeWarning(status, LOGGED_ON);
        }
        status = LOGGED_ON;
        log.debug("Application - delegate");
    }

    @Override
    public void onLogout() {
        if (status != DISCONNECTED) {
            logStateChangeWarning(status, LOGGED_OUT);
        }
        status = LOGGED_OUT;
        log.debug("StateListener");
    }

    /**
     * This are delegates from the <code>Application</code>.
     * 
     * @param sessionID
     */
    public void onLogout(SessionID sessionID) {
        if (status != DISCONNECTED) {
            logStateChangeWarning(status, LOGGED_OUT);
        }

        status = LOGGED_OUT;
        log.debug("Application delegate");
    }

    /**
     * 
     * @param sessionID
     * @param logoutText
     */
    public void onLogout(SessionID sessionID, String logoutText) {
        // We have a logout event with text - figure out what it is
        log.error("Logout with text: " + logoutText);
        this.logoutText = logoutText;
    }
    
    @Override
    public void onHeartBeatTimeout() {
        log.error("Heartbeat timeout:" + sessionID);
    }

    @Override
    public void onMissedHeartBeat() {
        log.warn("Missed heartbeat: " + sessionID);
    }

    @Override
    public void onRefresh() {
        log.warn("OnRefresh: " + sessionID);
    }

    @Override
    public void onReset() {
        // This is pretty useless unless we are within a session.
        Session session = Session.lookupSession(sessionID);
        if (session != null && session.isSessionTime()) {
            log.warn("OnReset: " + sessionID);
        }
    }

    /**
     * 
     * @return
     */
    protected SessionID getSessionID() {
        return sessionID;
    }

    /**
     * 
     * @param currentStatus
     * @param newStatus
     */
    private void logStateChangeWarning(FixClientStatus currentStatus, FixClientStatus newStatus) {
        log.warn("Changing FixClient state from " + currentStatus + " to " + newStatus);
    }

    /**
     * This works with the Slf4jLog. QFJ will log events, if we are interested in these events we need to
     * change the logger to identify them and then provide notifications via the
     * <code>PropertyChangeSupport</code> class. This makes it easy for 'listeners' to receive these events.
     * All events of interest are funneled through here
     * 
     * @param event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getPropertyName();

        if (Slf4jLog.MSG_SEQ_NUM.equals(property)) {
            // we have an issue with the sequence numbers!!
            String msg = event.getNewValue().toString();
            log.error(msg);
            if (shouldReport(Slf4jLog.MSG_SEQ_NUM)) {
                EmailSender sender = new EmailSender(emailSettings.getAsProperties());
                sender.sendMessage("Message Sequence Error " + sessionID.getTargetCompID(), msg);
                reportedError(Slf4jLog.MSG_SEQ_NUM);
            }
        } else if (Slf4jLog.LOGON_STATE.equals(property)) {
            // We have an issue with re-connecting with invalid sequence numbers
            String msg = event.getNewValue().toString();
            log.error(msg);
            if (shouldReport(Slf4jLog.LOGON_STATE)) {
                EmailSender sender = new EmailSender(emailSettings.getAsProperties());
                sender.sendMessage("Logon State Error With " + sessionID.getTargetCompID(), msg);
                reportedError(Slf4jLog.LOGON_STATE);
            }
        } else if (Slf4jLog.CONNECTION_REFUSED.equals(property)) {
            String msg = event.getNewValue().toString();
            log.error(msg);
            if (shouldReport(CONNECTION_REFUSED_KEY) && !badDisconnect) {
                connectionRefused = true;
                EmailSender sender = new EmailSender(emailSettings.getAsProperties());
                StringBuilder sb = new StringBuilder(256);
                sb.append(msg).append("\n");
                
                sb.append("Will send a reminder in 30 minutes.");
                sender.sendMessage("Connection refused " + sessionID.getTargetCompID(), sb.toString());
                
                reportedError(CONNECTION_REFUSED_KEY);
            }
        }

    }
}
