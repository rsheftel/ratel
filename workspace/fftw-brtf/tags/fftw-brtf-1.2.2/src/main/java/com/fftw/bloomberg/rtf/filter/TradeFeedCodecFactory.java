package com.fftw.bloomberg.rtf.filter;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolDecoder;

/**
 * Provide encoder and decoder for the Real-time Bloomberg feed.
 *
 */
public class TradeFeedCodecFactory implements ProtocolCodecFactory {

    private final ProtocolEncoder encoder;
    private final ProtocolDecoder decoder;

    public TradeFeedCodecFactory() {
        encoder = new TradeFeedEncoder();
        decoder = new TradeFeedDecoder();
    }

    public ProtocolEncoder getEncoder() throws Exception {
        return encoder;
    }

    public ProtocolDecoder getDecoder() throws Exception {
        return decoder;
    }
}
