package malbec.fer;

import quickfix.Message;

@SuppressWarnings("serial")
public class FerRejectEvent extends FerFixEvent {

    private int rejectReasonCode;
    
    public FerRejectEvent(Object source, FixEventType eventType, Message fixMessage,
            Message referencedMessage, int resaon) {
        super(source, eventType, fixMessage, referencedMessage);
        this.rejectReasonCode = resaon;
    }

    public FerRejectEvent(Object source, FixEventType eventType, Message fixMessage, int reason) {
        this(source, eventType, fixMessage, null, reason);
    }

    public int getRejectReasonCode() {
        return rejectReasonCode;
    }
}
