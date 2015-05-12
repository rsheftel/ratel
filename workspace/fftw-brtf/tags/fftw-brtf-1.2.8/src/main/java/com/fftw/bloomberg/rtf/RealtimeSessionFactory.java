package com.fftw.bloomberg.rtf;

import com.fftw.bloomberg.rtf.client.RealTimeFeedClientHandler;
import com.fftw.bloomberg.rtf.messages.RtfMessage;
import com.fftw.sbp.ProtocolIoHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class RealtimeSessionFactory {

    private static RealtimeSessionFactory instance = new RealtimeSessionFactory();

    private Map<RealtimeSessionID, RealtimeProtocolSession> sessions = new HashMap<RealtimeSessionID, RealtimeProtocolSession>();

    private RealtimeSessionFactory() {
        // Prevent
    }

    public static RealtimeSessionFactory getInstance() {
        return instance;
    }


    public RealtimeProtocolSession createSession(RealtimeApplication app, Properties props, ProtocolIoHandler<RealtimeSessionID, RtfMessage> ioHandler) {

        RealtimeProtocolSession session = new RealtimeProtocolSession(app, props, new RealTimeFeedClientHandler());
        sessions.put(session.getSessionID(), session);

        return session;
    }

    public RealtimeProtocolSession findSession(RealtimeSessionID sessionID) {
        return sessions.get(sessionID);
    }
}
