package com.fftw.bloomberg.rtf;

import com.fftw.sbp.ProtocolSessionConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfigImpl;

import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class RealtimeSessionConfig extends SocketSessionConfigImpl implements ProtocolSessionConfig {

    private long connectionTimeout = 30;  // Defaul to 30 milliseconds
    private long reconnectInterval = 30 * 1000;  // Default to 30 seconds
    private SocketAddress[] socketAddresses;

    private Properties loadedConfiguration;

    private int failedConnectionThreshhold = 4;


    public RealtimeSessionConfig(Properties props) {
        loadedConfiguration = props;

        configureAddresses(loadedConfiguration, 5);

        configureConnection(loadedConfiguration);
    }

    private void configureConnection(Properties props) {
        // Check the configuration for connection propert over-rides

        String timeout = props.getProperty("connection.timeout");
        if (timeout != null) {
            setConnectionTimeout(Long.parseLong(timeout));
        }

        String reconnectInterval = props.getProperty("connection.reconnect.interval");
        if (reconnectInterval != null) {
            setReconnectInterval(Long.parseLong(reconnectInterval));
        }

        String failedConnection = props.getProperty("connection.failure.threshhold");
        if (failedConnection != null) {
            failedConnectionThreshhold = Integer.parseInt(failedConnection);
        }

    }

    private void configureAddresses(Properties props, int levels) {
        String hostKey = "session.host";
        String portKey = "session.port";
        List<SocketAddress> addresses = new ArrayList<SocketAddress>();

        for (int i = 0; i < levels; i++) {
            if (i > 0) {
                hostKey = "session.host" + "." + i;
                portKey = "session.port" + "." + i;
            }

            String host = props.getProperty(hostKey);
            String port = props.getProperty(portKey);
            if (host != null && port != null) {
                addresses.add(new InetSocketAddress(host, Integer.parseInt(port)));
            } else if (host == null && port == null) {
                continue;
            } else {
                throw new IllegalArgumentException("Must specify host and port in pairs.");
            }
        }
        if (addresses.size() == 0) {
            throw new IllegalArgumentException("Must specify at least one connection.");
        }
        socketAddresses = addresses.toArray(new SocketAddress[addresses.size()]);
    }

    public long getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(long timeout) {
        this.connectionTimeout = timeout;
    }

    public long getReconnectInterval() {
        return this.reconnectInterval;
    }

    public void setReconnectInterval(long interval) {
        this.reconnectInterval = interval;
    }

    public SocketAddress[] getSocketAddresses() {
        return socketAddresses;
    }

    public void setSocketAddresses(SocketAddress[] addresses) {
        this.socketAddresses = addresses;
    }

    public int getFailedConnectionThreshhold() {
        return failedConnectionThreshhold;
    }
}
