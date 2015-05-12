package malbec.fer.fix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import malbec.fer.Order;
import malbec.fer.util.OrderValidation;
import malbec.fix.FixClientStateListener;
import malbec.fix.IPropertyChangeSupport;
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
import quickfix.field.OrdStatus;
import quickfix.fix44.ExecutionReport;

/**
 * This is the Fix Client for the FER engine.
 * 
 */
public class FerFixClientApplication extends MessageCracker implements Application, IPropertyChangeSupport {

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private EmailSettings emailSettings;
    private FixClientStateListener stateListener;

    private Logger log = LoggerFactory.getLogger(getClass());

    private FerFixClientApplication() {}

    public FerFixClientApplication(EmailSettings emailSettings) {
        this();
        this.emailSettings = emailSettings;
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, RejectLogon {
    // Ignore inbound admin messages
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
        if (log.isInfoEnabled()) {
            log.info("Received message, type=" + message);
        }
        crack(message, sessionID);
    }

    @Override
    public void onMessage(ExecutionReport message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        // if this is an 'ACK' update the DB status
        OrdStatus orderStatus = message.get(new OrdStatus());
        if (OrdStatus.NEW == orderStatus.getValue()) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this, "OrderUpdate-NEW", null, message);
            pcs.firePropertyChange(pce);
            log.info("Fired event: OrderUpdate-NEW");
        } else if (OrdStatus.PENDING_NEW == orderStatus.getValue()) {
            // Apollo send pending and then new
            PropertyChangeEvent pce = new PropertyChangeEvent(this, "OrderUpdate-PENDING_NEW", null, message);
            pcs.firePropertyChange(pce);
            log.info("Fired event: OrderUpdate-PENDING_NEW");
        } else {
            log.warn("Unprocessed OrderStatus of " + orderStatus);
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

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    // Do nothing. We can stop the message from being sent by throwing the 'DoNotSend' exception
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public Message createOrder(Order order, List<String> errors) {
        return OrderValidation.createFixMessage(order, errors);
    }
}
