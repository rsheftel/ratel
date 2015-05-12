package com.fftw.bloomberg.rtf.messages;

import org.joda.time.LocalDate;

/**
 *
 */
public class DefaultRtfMessageBody implements RtfMessageBody {

    private char messageType;
    private int messageSequenceNumber;
    private LocalDate messageDate;
    private boolean rawMessageSet;
    private String rawMessage;

    public DefaultRtfMessageBody(char messageType, String rawMessage) {
        this.messageType = messageType;
        this.rawMessage = rawMessage;

        rawMessageSet = rawMessage != null;
    }

    public char getMessageType() {
        return messageType;
    }

    public int getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public LocalDate getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(LocalDate messageDate) {
        this.messageDate = messageDate;
    }

    public boolean hasRawMessage() {
        return rawMessageSet;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public String toString() {
        return hasRawMessage() ?  rawMessage : "";
    }
}


