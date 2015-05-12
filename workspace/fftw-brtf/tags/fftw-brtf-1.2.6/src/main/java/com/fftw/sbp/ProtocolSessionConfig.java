package com.fftw.sbp;

import org.apache.mina.transport.socket.nio.SocketSessionConfig;

import java.net.SocketAddress;

/**
 * Configuration information for a protocol session.
 */
public interface ProtocolSessionConfig extends SocketSessionConfig {

    /**
     * The number of milliseconds the connection should wait before giving up.
     *
     * @return
     */
    long getConnectionTimeout();

    /**
     * The number of milliseconds the connection should wait before giving up.
     *
     * @param timeout
     */
    void setConnectionTimeout(long timeout);

    /**
     * How many milliseconds to wait between connection attempts.
     *
     * @return
     */
    long getReconnectInterval();

    /**
     * How many milliseconds to wait between connection attempts.
     *
     * @param interval
     */
    void setReconnectInterval(long interval);

    SocketAddress[] getSocketAddresses();

    void setSocketAddresses(SocketAddress[] addresses);

    int getFailedConnectionThreshhold();
    
}
