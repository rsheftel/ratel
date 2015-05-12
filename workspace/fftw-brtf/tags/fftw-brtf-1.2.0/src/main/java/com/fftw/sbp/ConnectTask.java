package com.fftw.sbp;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Observable;

/**
 * Handle creating connections and reconnecting when connection drop or are closed.
 */
public class ConnectTask extends Observable implements Runnable {

    final Logger logger = LoggerFactory.getLogger("SessionProtocolLog");

    private final SocketAddress[] socketAddresses;
    private final ProtocolSession protocolSession;
    private final ProtocolSessionConfig sessionConfig;
    private final IoHandler ioHandler;

    private SocketConnector connector;

    private IoSession ioSession;

    private int socketAddressIndex = 0;
    private int failedConnectionCount = 0;
    private int notificationThreshhold = 5;

    private long lastConnectionTime;

    private long lastConnectionAttempt;


    public ConnectTask(ProtocolSession protocolSession, ProtocolSessionConfig sessionConfig) {
        this.protocolSession = protocolSession;
        this.sessionConfig = sessionConfig;
        this.socketAddresses = sessionConfig.getSocketAddresses();
        this.ioHandler = protocolSession.getIoHandler();

        notificationThreshhold = sessionConfig.getFailedConnectionThreshhold();

        connector = new SocketConnector();

        // To be compatible with MINA 2 we need to disable ThreadModel
        SocketConnectorConfig initiatorConfig = connector.getDefaultConfig();
        initiatorConfig.setThreadModel(ThreadModel.MANUAL);
        initiatorConfig.setConnectTimeout((int) sessionConfig.getConnectionTimeout());

        DefaultIoFilterChainBuilder filterChainBuilder = connector.getDefaultConfig().getFilterChain();
        // Add a logging filter
        // TODO allow this to be configured
//        filterChainBuilder.addLast("logger", new LoggingFilter());
        // Add the CPU-bound filter - our protocol specific logic
        filterChainBuilder.addLast("codec", new ProtocolCodecFilter(protocolSession.getProtocolCodecFactory()));
    }

    public long getLastConnectionTime() {
        return lastConnectionTime;
    }

    public void run() {
        try {
            // This is fired every second until cancelled
            if (shouldReconnect()) {
                connect();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Connection task fired, no need to reconnect");
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void connect() {
        SocketAddress socketAddress = getSocketAddress();
        logger.info("Attempting connection to " + socketAddress);
        lastConnectionAttempt = System.currentTimeMillis();
        ConnectFuture connectFuture = connector.connect(socketAddress, protocolSession.getIoHandler());

        connectFuture.join();
        try {
            ioSession = connectFuture.getSession();
            failedConnectionCount = 0;
            lastConnectionTime = System.currentTimeMillis();
            protocolSession.setIoSession(ioSession);
        }
        catch (RuntimeIOException e) {
            // Increase the failed connection count
            failedConnectionCount++;
            setChanged();
            logger.error("Failed attempt to connect to " + socketAddress);
            if (failedConnectionCount >= notificationThreshhold) {
                notifyObservers("Failed connectionAttemp=" + failedConnectionCount);
            }

            try {
                Throwable t = getRootCause(e);
                ioHandler.exceptionCaught(ioSession, t);
            } catch (Exception e1) {
                // this should not happen, if it does, let someone know
                logger.error("IoHanlder threw exception while processing connection exception", e1);
            }
        }

    }

    private boolean shouldReconnect() {
        return (ioSession == null || !ioSession.isConnected()) && isTimeForReconnect()
                && (protocolSession.isEnabled() && protocolSession.isActive());
    }

    private boolean isTimeForReconnect() {
        return System.currentTimeMillis() - lastConnectionAttempt >= sessionConfig.getReconnectInterval();
    }

    /**
     * Return the root cause of this exception.
     *
     * @param t any exception
     * @return the root exception or the exception itself
     */
    private Throwable getRootCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }

        return t;
    }

    /**
     * Return the connection that we want to connect to.
     *
     * @return
     */
    private SocketAddress getSocketAddress() {
        return socketAddresses[socketAddressIndex % socketAddresses.length];
    }


}
