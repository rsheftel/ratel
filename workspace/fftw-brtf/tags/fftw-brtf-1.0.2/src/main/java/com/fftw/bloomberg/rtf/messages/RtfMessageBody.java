package com.fftw.bloomberg.rtf.messages;

import org.joda.time.LocalDate;

/**
 * All Rtf message bodies must have these.
 * <p/>
 * The limiting message is the End-Of-Day.
 */
public interface RtfMessageBody {
    char getMessageType();

    LocalDate getMessageDate();

    int getMessageSequenceNumber();

    boolean hasRawMessage();

    String getRawMessage();
}
