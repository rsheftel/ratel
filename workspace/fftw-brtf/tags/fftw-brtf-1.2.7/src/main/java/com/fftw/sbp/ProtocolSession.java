package com.fftw.sbp;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.joda.time.DateTime;

/**
 * The session used to interact with the protocol.
 */
public interface ProtocolSession<ID,M> {
    void setSchedule(SessionSchedule schedule);

    void setHeartbeatInterval(int interval);

    int getHeartbeatInterval();

    DateTime getStartTime();

    ID getSessionID();

    boolean isActive();

    boolean isEnabled();

    ProtocolSessionConfig getSessionConfig();

    IoHandler getIoHandler();

    void setIoHandler(ProtocolIoHandler<ID, M> ioHandler);

    ProtocolCodecFactory getProtocolCodecFactory();

    void processMessage(M message);

    void setIoSession(IoSession ioSession);

    void sendMessage(M message);

}
