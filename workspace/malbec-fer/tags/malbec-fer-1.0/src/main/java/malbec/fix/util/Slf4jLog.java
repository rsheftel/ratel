package malbec.fix.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.slf4j.Logger;
import org.slf4j.spi.LocationAwareLogger;

import quickfix.SLF4JLog;
import quickfix.SessionID;

/**
 * Copy of <code>quickfix.SLF4JLog</code> so that we can use a newer version of SLF4J and add some features
 * to get information from the session.
 */
public class Slf4jLog extends SLF4JLog {

    public static final String CONNECTION_REFUSED = "ConnectionRefused";
    public static final String MSG_SEQ_NUM = "MsgSeqNum";
    public static final String LOGON_STATE = "LogonState";
    

    private String logPrefix;
    private String callerFqcn;

    // Cheat and use PropertyChangeSupport
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public Slf4jLog(SessionID sessionID, String eventCategory, String incomingMsgCategory,
            String outgoingMsgCategory, boolean prependSessionID, boolean logHeartbeats, String inCallerFQCN) {
        super(sessionID, eventCategory, incomingMsgCategory, outgoingMsgCategory, prependSessionID,
                logHeartbeats, inCallerFQCN);
        // Duplicate local variables from parent
        logPrefix = prependSessionID ? (sessionID + ": ") : null;
        callerFqcn = inCallerFQCN;
    }

    /**
     * Provide a hook into the events that QFJ sends from the session.
     * 
     * @param text
     */
    @Override
    public void onEvent(String text) {
        super.onEvent(text);
        // logged exceptions will have the text starting with 'quickfix.SessionException' (AFAIK).

        if (text.contains("quickfix.SessionException")) {
//            System.err.println(text);
            String msg = text.replace("quickfix.SessionException", "").trim();
            
            if (msg.contains("MsgSeqNum too low")) {
                // check if this is an error in sequence numbers - there is no easy way to get this :(
                pcs.firePropertyChange(new PropertyChangeEvent(this, MSG_SEQ_NUM, "", msg));
            } else if (msg.contains("Logon state is not valid for message")) {
                // Logon state is not valid for message
                pcs.firePropertyChange(new PropertyChangeEvent(this, LOGON_STATE, "", msg));
            }
        } else if (text.contains("Connection refused")) {
            pcs.firePropertyChange(new PropertyChangeEvent(this, CONNECTION_REFUSED, "", text));
        }
    }

    @Override
    protected void log(Logger log, String text) {
        if (log.isInfoEnabled()) {
            String message = logPrefix != null ? (logPrefix + text) : text;
            if (log instanceof LocationAwareLogger) {
                LocationAwareLogger la = (LocationAwareLogger) log;
                la.log(null, callerFqcn, LocationAwareLogger.INFO_INT, message, null);
            } else {
                log.info(message);
            }
        }
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    public boolean hasListeners(String propertyName) {
        return pcs.hasListeners(propertyName);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

}
