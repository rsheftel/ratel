package malbec.fer;

public enum FixEventType {
    New, PendingNew, OrderRejected, Fill, Expired, Cancelled, PendingCancel, Replaced, CancelReject, 
    BusinessMessageReject, MessageReject,
    
    Unknown;

    public static FixEventType fromString(String str) {
        for (FixEventType fet : values()) {
            if (fet.toString().equalsIgnoreCase(str)) {
                return fet;
            }
        }
        return Unknown;
    }
}
