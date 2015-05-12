package malbec.fer;

import java.beans.PropertyChangeEvent;
import java.util.Date;

import malbec.util.DateTimeUtil;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.ClOrdID;
import quickfix.field.CxlRejResponseTo;
import quickfix.field.OrderID;
import quickfix.field.OrigClOrdID;
import quickfix.field.TradeDate;
import quickfix.field.TransactTime;

@SuppressWarnings("serial")
public class FerFixEvent extends PropertyChangeEvent {

    private FixEventType eventType;
    
    private String errorMessage;
    
    public FerFixEvent(Object source, FixEventType eventType, Message fixMessage) {
        this(source, eventType, fixMessage, null);
    }

    public FerFixEvent(Object source, FixEventType eventType, Message fixMessage, Message referencedMessage) {
        super(source, eventType.toString(), referencedMessage, fixMessage);
        this.eventType = eventType;
    }
    
    public FixEventType getEventType() {
        return eventType;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getClientOrderId() {
        try {
            ClOrdID clientOrderID = new ClOrdID(getFixMessage().getString(ClOrdID.FIELD));
            return clientOrderID.getValue();
        } catch (FieldNotFound e) {
           // will not happen
        }
        
        return null;
    }
    
    public String getOrderID() {
        try {
            OrderID orderID = new OrderID(getFixMessage().getString(OrderID.FIELD));
            return orderID.getValue();
        } catch (FieldNotFound e) {
           // will not happen
        }
        
        return null;
    }
    
    public LocalDate getTradeDate() {
        try {
            Message fixMessage = getFixMessage();
            if (fixMessage.isSetField(TradeDate.FIELD)) {
                String tradeDate = fixMessage.getString(TradeDate.FIELD);
                return DateTimeUtil.getLocalDate(tradeDate);
            }
            
            if (fixMessage.isSetField(TransactTime.FIELD)) {
                String transactionTime = fixMessage.getString(TransactTime.FIELD);
                Date utcTime = DateTimeUtil.getDate(transactionTime);
                DateTime dtUtc = new DateTime(utcTime);
                return dtUtc.toLocalDate();
            }
            
            return new LocalDate();
        } catch (FieldNotFound e) {
           // will not happen
        }
        
        return null;
    }

    public Message getFixMessage() {
        return (Message) getNewValue();
    }

    public Message getReferencedMessage() {
        return (Message) getOldValue();
    }
    
    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Event(propertyName)="+ getPropertyName());
        sb.append(", clientOrderID="+ getClientOrderId());
        sb.append(", orderID="+ getOrderID());
        sb.append(", errorMessage="+ errorMessage);
        sb.append(", fixMessage="+ getFixMessage());
        
        return sb.toString();
    }

    public String getOriginalClientOrderId() {
        try {
            OrigClOrdID originalClientOrderId = new OrigClOrdID(getFixMessage().getString(OrigClOrdID.FIELD));
            return originalClientOrderId.getValue();
        } catch (FieldNotFound e) {
           // ignore
        }
        return null;
    }

    public String getInResponseTo() {
        try {
            CxlRejResponseTo rt = new CxlRejResponseTo(getFixMessage().getChar(CxlRejResponseTo.FIELD));
            return rt.toString();
        } catch (FieldNotFound e) {
            
        }
        return null;
    }
    
    
}
