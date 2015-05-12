package malbec.fer.fix;

import static malbec.fer.FixEventType.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import malbec.fer.FerFixEvent;
import malbec.fer.FerRejectEvent;
import malbec.fer.Order;
import malbec.fer.util.OrderValidation;
import malbec.fix.FixClientApplication;
import malbec.fix.FixVersion;
import malbec.util.EmailSettings;
import malbec.util.IPropertyChangeSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.BusinessRejectReason;
import quickfix.field.ExecType;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.RefMsgType;
import quickfix.field.RefSeqNum;
import quickfix.field.RefTagID;
import quickfix.field.SessionRejectReason;
import quickfix.field.Text;

/**
 * This is the Fix Client for the FER engine.
 * 
 */
public class FerFixClientApplication extends FixClientApplication implements IPropertyChangeSupport {

    public static final int SEQUENCE_NUMBER_LOGON = 200;

    public static final int SEQUENCE_NUMBER = 201;

    final private Logger log = LoggerFactory.getLogger(getClass());

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public FerFixClientApplication(EmailSettings emailSettings, boolean requiresLogon, String userID,
        String password) {
        super(emailSettings, requiresLogon, userID, password);
    }

    public FerFixClientApplication(EmailSettings emailSettings) {
        this(emailSettings, false, null, null);
    }

    @Override
    public void onMessage(quickfix.fix44.ExecutionReport message, SessionID sessionID) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        onExecutionReportMessage(message, sessionID);
    }

    @Override
    public void onMessage(quickfix.fix42.ExecutionReport message, SessionID sessionID) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        onExecutionReportMessage(message, sessionID);
    }

    protected void onExecutionReportMessage(Message message, SessionID sessionID) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        try {
            // if this is an 'ACK' update the DB status
            char execType = message.getChar(ExecType.FIELD);
            char orderStatus = message.getChar(OrdStatus.FIELD);

            if (isFill(execType) && orderStatus != OrdStatus.NEW) {
                FerFixEvent pce = new FerFixEvent(this, Fill, message);
                pcs.firePropertyChange(pce);
                fixLog.info("Fired event: OrderUpdate-FILL");
            } else {
                if (OrdStatus.NEW == orderStatus) {
                    FerFixEvent ffe = new FerFixEvent(this, New, message);
                    pcs.firePropertyChange(ffe);
                    fixLog.info("Fired event: OrderUpdate-NEW");
                } else if (OrdStatus.PENDING_NEW == orderStatus) {
                    // Apollo send pending and then new
                    FerFixEvent ffe = new FerFixEvent(this, PendingNew, message);
                    pcs.firePropertyChange(ffe);
                    fixLog.info("Fired event: OrderUpdate-PENDING_NEW");
                } else if (OrdStatus.REJECTED == orderStatus) {
                    FerFixEvent ffe = new FerFixEvent(this, OrderRejected, message);
                    ffe.setErrorMessage(getRejectReason(message));
                    pcs.firePropertyChange(ffe);
                    fixLog.info("Fired event: OrderUpdate-REJECTED");
                } else if (OrdStatus.EXPIRED == orderStatus) {
                    FerFixEvent ffe = new FerFixEvent(this, Expired, message);
                    pcs.firePropertyChange(ffe);
                    fixLog.info("Fired event: OrderUpdate-EXPIRED");
                } else if (OrdStatus.CANCELED == orderStatus) {
                    FerFixEvent ffe = new FerFixEvent(this, Cancelled, message);
                    pcs.firePropertyChange(ffe);
                    fixLog.info("Fired event: OrderUpdate-CANCELLED");
                } else if (OrdStatus.PENDING_CANCEL == orderStatus) {
                    FerFixEvent ffe = new FerFixEvent(this, PendingCancel, message);
                    pcs.firePropertyChange(ffe);
                    fixLog.info("Fired event: OrderUpdate-PENDING-CANCEL");
                } else if (OrdStatus.REPLACED == orderStatus) {
                    FerFixEvent ffe = new FerFixEvent(this, Replaced, message);
                    pcs.firePropertyChange(ffe);
                    fixLog.info("Fired event: OrderUpdate-REPLACED");
                } else {
                    // TODO add execution bust
                    fixLog.warn("Unprocessed OrderStatus of " + orderStatus);
                }
            }
        } catch (Exception e) {
            // If anything bad happens here, we end-up in a loop
            log.error("Unexpected Error!", e);
            sendError("Exception in onMessage(ExecutionReport)", message, e);
        }
    }

    private boolean isFill(char execType) {
        return (execType == ExecType.FILL || execType == ExecType.PARTIAL_FILL || execType == ExecType.TRADE);
    }

    /**
     * Get the BusinessRejectReason from the FixEvent.
     * 
     * TODO Should this be in the FerFixEvent??
     * 
     * @param ferEvent
     * @return
     */
    private int getRejectReasonCode(Message fixMessage) {
        int rejectReason = 0; // Other
        if (fixMessage.isSetField(BusinessRejectReason.FIELD)) {
            try {
                rejectReason = fixMessage.getInt(BusinessRejectReason.FIELD);
            } catch (FieldNotFound e) {
                // Will not happen
            }
        }

        return rejectReason;
    }

    private String getRejectReason(Message fixMessage) {
        if (fixMessage.isSetField(Text.FIELD)) {
            try {
                // we should be checking field 371 for referenced tag
                String textMessage = fixMessage.getString(Text.FIELD);

                if (fixMessage.isSetField(RefTagID.FIELD)) {
                    int refTag = fixMessage.getInt(RefTagID.FIELD);
                    String refTagValue = fixMessage.getString(refTag);

                    return textMessage + "; Reference tag: " + refTag + "=" + refTagValue;
                } else if (fixMessage.isSetField(RefMsgType.FIELD)) {
                    String refMsgType = fixMessage.getString(RefMsgType.FIELD);

                    return textMessage + "; Reference message type: " + refMsgType;
                }

                return textMessage;
            } catch (FieldNotFound e) {
                // This will not happen!!!
            }
        }
        return null;
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
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
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
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public Message createOrder(Order order, List<String> errors) {
        return OrderValidation.createFixMessage(order, errors);
    }

    @Override
    public void onMessage(quickfix.fix44.BusinessMessageReject fixMessage, SessionID sessionID)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        onBusinessRejectMessage(fixMessage, sessionID);
    }

    @Override
    public void onMessage(quickfix.fix42.BusinessMessageReject fixMessage, SessionID sessionID)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        onBusinessRejectMessage(fixMessage, sessionID);
    }

    protected void onBusinessRejectMessage(Message fixMessage, SessionID sessionID) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        // These are sent by QFJ Executor when it does not understand a message type
        // Also sent by Redi when there is something wrong, normally follows a
        // Cancel Request Reject
        log.error("Received BusinessMessageReject: " + fixMessage);

        try {
            // If we received this, we must have sent a cancel request to an order that
            // was already cancelled. A mistake on our side. Update the order status.
            FerFixEvent ffe = null;
            // There are 8 possible reasons (Tag 380)
            int rejectReasonCode = getRejectReasonCode(fixMessage);
            switch (rejectReasonCode) {
                default:
                    log.error("Unknown BusinessMessageReject response: " + fixMessage);
                    ffe = new FerFixEvent(this, BusinessMessageReject, fixMessage);
                    ffe.setErrorMessage(getRejectReason(fixMessage));
                    break;
                case 3: // Unsupported Message Type
                    try {
                        // message type is in 372
                        // sequence number of message is in 45
                        // text message in 58
                        // RefMsgType refMsgType = new RefMsgType(fixMessage.getString(RefMsgType.FIELD));
                        RefSeqNum refSeqNum = new RefSeqNum(fixMessage.getInt(RefSeqNum.FIELD));
                        Message referencedMessage = getSentMessage(sessionID, refSeqNum);
                        ffe = new FerRejectEvent(this, BusinessMessageReject, fixMessage, referencedMessage,
                            rejectReasonCode);
                        ffe.setErrorMessage(getRejectReason(fixMessage));

                    } catch (FieldNotFound e) {
                        log.error("Unable to process Unsupported Message Type Error", e);
                        ffe = new FerFixEvent(this, BusinessMessageReject, fixMessage);
                    }
                    break;
            }

            pcs.firePropertyChange(ffe);
            fixLog.info("Fired event: " + BusinessMessageReject);
        } catch (Exception e) {
            // If anything bad happens here, we end-up in a loop
            log.error("Unexpected Error!", e);
            sendError("Exception in onMessage(OrderCancelReject)", fixMessage, e);
        }

    }

    private Message getSentMessage(SessionID sessionId, RefSeqNum refSeqNum) {
        List<String> messages = new ArrayList<String>();

        Session session = Session.lookupSession(sessionId);
        try {
            session.getStore().get(refSeqNum.getValue(), refSeqNum.getValue(), messages);

            if (messages.size() > 0) {
                String messageAsString = messages.get(0);
                Message fixMessage = MessageUtils.parse(session.getMessageFactory(), null, messageAsString);

                return fixMessage;
            }
        } catch (Exception e) {
            log.error("Unable to retrieve sent message:", e);
        }

        return null;
    }

    @Override
    public void onMessage(quickfix.fix44.OrderCancelReject message, SessionID sessionID)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        onOrderCancelRejectMessage(message, sessionID);
    }

    @Override
    public void onMessage(quickfix.fix42.OrderCancelReject message, SessionID sessionID)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        onOrderCancelRejectMessage(message, sessionID);
    }

    protected void onOrderCancelRejectMessage(Message message, SessionID sessionID) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        log.error("Received OrderCancelReject: " + message);

        try {
            // If we received this, we must have sent a cancel request to an order that
            // was already cancelled. A mistake on our side. Update the order status.
            FerFixEvent ffe = new FerFixEvent(this, CancelReject, message);
            ffe.setErrorMessage(getRejectReason(message));
            pcs.firePropertyChange(ffe);
            fixLog.info("Fired event: OrderUpdate-ORDER_CANCEL_REJECT");
        } catch (Exception e) {
            // If anything bad happens here, we end-up in a loop
            log.error("Unexpected Error!", e);
            sendError("Exception in onMessage(OrderCancelReject)", message, e);
        }
    }

    @Override
    public void onMessage(quickfix.fix44.Reject fixMessage, SessionID sessionId) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        onRejectMessage(fixMessage, sessionId, FixVersion.F44);
    }

    @Override
    public void onMessage(quickfix.fix42.Reject fixMessage, SessionID sessionId) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue {
        onRejectMessage(fixMessage, sessionId, FixVersion.F42);
    }

    /**
     * Treat 4.2 and 4.4 messages the same way.
     * 
     * Rejects are sent on failed logins with invalid sequence numbers, need to determine if that is the
     * reject reason.
     * 
     * @param fixMessage
     * @param sessionId
     * @throws FieldNotFound
     * @throws UnsupportedMessageType
     * @throws IncorrectTagValue
     */
    protected void onRejectMessage(Message fixMessage, SessionID sessionId, FixVersion version)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        log.error("Received Reject: " + fixMessage);

        try {
            RefSeqNum refSeqNum = new RefSeqNum(fixMessage.getInt(RefSeqNum.FIELD));
            Message referencedMessage = getSentMessage(sessionId, refSeqNum);
            int srr = 99;
            String errorMessage = getRejectReason(fixMessage);

            if (fixMessage.isSetField(SessionRejectReason.FIELD)) {
                srr = fixMessage.getInt(SessionRejectReason.FIELD);
            } else {
                String tmpErrMsg = errorMessage.toUpperCase();
                // if this is not FIX 4.4, we have less error codes
                if ("Tag specified out of required order".equalsIgnoreCase(errorMessage)) {
                    srr = 14; // FIX 4.4 value
                } else if (tmpErrMsg.contains("SEQUENCE") && tmpErrMsg.contains("NUMBER")) {
                    String msgType = referencedMessage.getHeader().getString(MsgType.FIELD);
                    if (MsgType.LOGON.equals(msgType)) {
                        srr = SEQUENCE_NUMBER_LOGON;
                    } else {
                        srr = SEQUENCE_NUMBER;
                    }
                }
            }
            FerRejectEvent ffe = new FerRejectEvent(this, MessageReject, fixMessage, referencedMessage, srr);
            ffe.setErrorMessage(errorMessage);
            pcs.firePropertyChange(ffe);

            fixLog.info("Fired event: OrderUpdate-MESSAGE_REJECT");
        } catch (Exception e) {
            // If anything bad happens here, we end-up in a loop
            log.error("Unexpected Error!", e);
            sendError("Exception in onMessage(Reject)", fixMessage, e);
        }
    }

}
