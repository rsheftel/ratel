package malbec.fix;

import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.StringUtils;

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
import quickfix.field.MsgType;
import quickfix.field.Password;
import quickfix.field.Text;
import quickfix.field.Username;

public class FixClientApplication extends MessageCracker implements Application {

    private EmailSettings emailSettings;
    protected FixClientStateListener stateListener;

    private boolean requiresLogon;

    private String userID;
    private String password;
    protected Logger fixLog;

    private Logger log = LoggerFactory.getLogger(getClass());

    public FixClientApplication(EmailSettings emailSettings, Boolean requiresLogon, String userID, String password) {
        this.emailSettings = emailSettings;
        this.requiresLogon = requiresLogon;
        this.userID = userID;
        this.password = password;
        fixLog = log;   // default to class level logger
    }

    /**
     * @return the requiresLogon
     */
    public boolean isRequiresLogon() {
        return requiresLogon;
    }

    /**
     * @param requiresLogon the requiresLogon to set
     */
    public void setRequiresLogon(boolean requiresLogon) {
        this.requiresLogon = requiresLogon;
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

    @Override
    public void onMessage(quickfix.fix44.Logout message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        onLogoutMessage(message, sessionID);
    }

    @Override
    public void onMessage(quickfix.fix42.Logout message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        onLogoutMessage(message, sessionID);
    }
    
    /**
     * Process logout messages
     */
    protected void onLogoutMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        if (message.isSetField(Text.FIELD)) {
            String txt = message.getString(Text.FIELD);
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
        if (requiresLogon && (userID != null || password != null)) {
            final Message.Header header = message.getHeader();
            try {
                if (header.getField(new MsgType()).valueEquals(MsgType.LOGON)) {
                    if (userID!= null) {
                        message.setField(new Username(userID));
                    }
                    if (password != null) {
                        message.setField(new Password(password));
                    }
                }
            } catch (FieldNotFound e) {
                log.error("Message Type field not found in the message... This can't be good!!!");
            }
        }
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

    protected void sendError(String subject, Message message, Exception e) {
        StringBuilder sb = new StringBuilder(512);
        sb.append(subject).append("\n");
        sb.append("Raw message: " + message);
        sb.append("\n\n");

        sb.append("Exception stack:\n");
        sb.append(StringUtils.exceptionToString(e));

        EmailSender sender = new EmailSender(emailSettings.getAsProperties());
        sender.sendMessage("FER - Unable to process event", sb.toString());
    }

}
