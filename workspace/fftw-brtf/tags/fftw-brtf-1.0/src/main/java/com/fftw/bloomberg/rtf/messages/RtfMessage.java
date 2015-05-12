package com.fftw.bloomberg.rtf.messages;

/**
 * Lowest common interface for all Realtime Feeds Messages
 * 
 */
public interface RtfMessage {

    RtfHeader getHeader();

    RtfMessageBody getBody();

    void setBody(RtfMessageTradeBody body);
}
