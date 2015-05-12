package com.fftw.bloomberg.rtf.messages;

/**
 * Basic/default implementation of a RtfMessage.
 */
public class DefaultRtfMessage implements RtfMessage {

    private RtfHeader header;
    private RtfMessageBody body;  // data

    public DefaultRtfMessage(RtfHeader header) {
        this.header = header;
    }

    public DefaultRtfMessage(RtfHeader header, RtfMessageBody body) {
        this.header = header;
        this.body = body;
    }

    public RtfHeader getHeader() {
        return header;
    }

    public RtfMessageBody getBody() {
        return body;
    }

    public void setBody(RtfMessageTradeBody body) {
        this.body = body;
    }
    
}
