package com.fftw.metadb.domain.jms;

/**
 * Used to track ConnectionEvents.
 * <p/>
 * There are events created by the <code>ConnectionTask</code>.  There information is transfered
 * using instances of these objects.
 */
public class ConnectionEvent {

    private ConnectionEventType eventType;

    private Exception exception;

    private String message;


    public ConnectionEvent(ConnectionEventType cet, Exception e, String msg) {
        this.eventType = cet;
        this.exception = e;
        this.message = msg;
    }

    public ConnectionEvent(ConnectionEventType cet, String msg) {
        this(cet, null, msg);
    }

    public ConnectionEventType getEventType() {
        return eventType;
    }

    public Exception getException() {
        return exception;
    }

    public String getMessage() {
        return message;
    }
}
