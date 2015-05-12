package malbec.fix.server;

import malbec.fix.FixClientStateListener;
import malbec.util.EmailSettings;

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
import quickfix.field.Text;
import quickfix.fix44.Logout;

public class FixServerApplication extends MessageCracker implements Application {

    private EmailSettings emailSettings;
    protected FixClientStateListener stateListener;
    
    private String userID;
    private String password;
    protected Logger fixLog;
    
    private Logger log = LoggerFactory.getLogger(getClass());

    public FixServerApplication(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
        fixLog = log;   // default to class level logger
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, RejectLogon {

        // Handle the Logout Message
        try {
            crack(message, sessionID);
        } catch (UnsupportedMessageType e) {
            log.error("Unable to crack message", e);
        }
    }

    /**
     * All messages funnel here.
     * 
     * Let them be 'cracked' then implement the message types of interest.
     * 
     * <code>onMessage(Logout)</code>
     */
    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
    IncorrectTagValue, UnsupportedMessageType {
        log.info("Received message, type=" + message);
        crack(message, sessionID);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        // do nothing
    }

    /**
     * Process logout messages
     */
    public void onMessage(Logout message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        if (message.isSetField(Text.FIELD)) {
            String txt = message.get(new Text()).getValue();
            stateListener.onLogout(sessionID, txt);
        }
    }
    
    @Override
    public void onCreate(SessionID sessionID) {
        Session mySession = Session.lookupSession(sessionID);
        stateListener = new FixClientStateListener(sessionID, emailSettings);
        mySession.addStateListener(stateListener);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        // We don't want duplicate code. Delegate to the listener
        stateListener.onLogon(sessionID);
    }

    @Override
    public void onLogout(SessionID sessionID) {
        stateListener.onLogout(sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        // ignore outbound admin messages
    }

    
    public void setFixLog(Logger log) {
        this.fixLog = log;
    }

    /**
     * @return the userID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
