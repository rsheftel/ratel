package malbec.fer;

public enum OrderStatus {
    New, Sent, Accepted, Executing, PendingNew, PendingCancel, Filled, Expired, Cancelled, Replaced,  
    Failed, FailedInsert, Invalid, Duplicate, 
    CancelRequestFailed, CancelRequested, CancelReplaceRequestFailed, CancelReplaceRequested, 
    PlatformRejected, FerretRejected, DoneForDay, Unknown;

    public static OrderStatus fromString(String str) {
        for (OrderStatus os : values()) {
            if (os.toString().equalsIgnoreCase(str)) {
                return os;
            }
        }
        return Unknown;
    }
}
