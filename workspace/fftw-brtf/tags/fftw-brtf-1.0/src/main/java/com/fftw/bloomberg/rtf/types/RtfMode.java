package com.fftw.bloomberg.rtf.types;

/**
 * The real-time feed is either working in online or batch mode.
 * <p/>
 * These modes can be intermixed during a session.  Each 'packet' will identify whether
 * it is part of an online or batch exchange.
 */
public enum RtfMode {
    Online, Batch;

    public static RtfMode valueOf(int modeCode) {
        return values()[modeCode];
    }
}
