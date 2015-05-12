package com.fftw.bloomberg.cmfp.mina.initiator;

import java.io.IOException;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import quickfix.ConfigError;
import quickfix.LogUtil;
import quickfix.SystemTime;
import quickfix.mina.CompositeIoFilterChainBuilder;
import quickfix.mina.NetworkingOptions;
import quickfix.mina.ProtocolFactory;

import com.fftw.bloomberg.aggregator.CmfProtocolHandler;
import com.fftw.bloomberg.cmfp.CmfSession;

public class IoSessionInitiator
{
    private final ScheduledExecutorService executor;

    private final ConnectTask reconnectTask;

    private Future<?> reconnectFuture;

    public IoSessionInitiator (CmfSession qfSession, SocketAddress[] socketAddresses,
        long reconnectIntervalInSeconds, ScheduledExecutorService executor,
        NetworkingOptions networkingOptions, IoFilterChainBuilder userIoFilterChainBuilder)
        throws ConfigError
    {
        this.executor = executor;
        try
        {
            reconnectTask = new ConnectTask(socketAddresses, userIoFilterChainBuilder, qfSession,
                reconnectIntervalInSeconds * 1000L, networkingOptions);
        }
        catch (GeneralSecurityException e)
        {
            throw new ConfigError(e);
        }
    }

    private static class ConnectTask implements Runnable
    {
        private final SocketAddress[] socketAddresses;

        private final IoConnector ioConnector;

        private final CmfSession cmfSession;

        private final long reconnectIntervalInMillis;

        private final CmfProtocolHandler ioHandler;

        private IoSession ioSession;

        private long lastReconnectAttemptTime;

        private long lastConnectTime;

        private int nextSocketAddressIndex;

        private int connectionFailureCount;

        public ConnectTask (SocketAddress[] socketAddresses,
            IoFilterChainBuilder userIoFilterChainBuilder, CmfSession quickfixSession,
            long reconnectIntervalInMillis, NetworkingOptions networkingOptions) throws ConfigError,
            GeneralSecurityException
        {
            this.socketAddresses = socketAddresses;
            this.cmfSession = quickfixSession;
            this.reconnectIntervalInMillis = reconnectIntervalInMillis;
            ioConnector = ProtocolFactory.createIoConnector(socketAddresses[0]);
            CompositeIoFilterChainBuilder ioFilterChainBuilder = new CompositeIoFilterChainBuilder(
                userIoFilterChainBuilder);

            DemuxingProtocolCodecFactory codec = quickfixSession.getProtocolCodecFactory();
            ioFilterChainBuilder.addLast(codec.getClass().getSimpleName(), new ProtocolCodecFilter(
                codec));

            IoServiceConfig serviceConfig = ioConnector.getDefaultConfig();
            serviceConfig.setFilterChainBuilder(ioFilterChainBuilder);
            serviceConfig.setThreadModel(ThreadModel.MANUAL);
            ioHandler = new CmfProtocolHandler(quickfixSession, networkingOptions);
        }

        public synchronized void run ()
        {
            if (shouldReconnect())
            {
                connect();
            }
        }

        private void connect ()
        {
            lastReconnectAttemptTime = SystemTime.currentTimeMillis();
            try
            {
                final SocketAddress nextSocketAddress = getNextSocketAddress();
                ConnectFuture connectFuture = ioConnector.connect(nextSocketAddress, ioHandler);
                connectFuture.join();
                ioSession = connectFuture.getSession();
                connectionFailureCount = 0;
                lastConnectTime = System.currentTimeMillis();
            }
            catch (Throwable e)
            {
                while (e.getCause() != null)
                {
                    e = e.getCause();
                }
                if ((e instanceof IOException) && (e.getMessage() != null))
                {
                    cmfSession.getLog().onEvent(e.getMessage());
                }
                else
                {
                    String msg = "Exception during connection";
                    LogUtil.logThrowable(cmfSession.getLog(), msg, e);
                }
                connectionFailureCount++;
            }
        }

        void disconnect() {
            ioSession.close();
        }
        
        private SocketAddress getNextSocketAddress ()
        {
            SocketAddress socketAddress = socketAddresses[nextSocketAddressIndex];
            nextSocketAddressIndex = (nextSocketAddressIndex + 1) % socketAddresses.length;
            return socketAddress;
        }

        private boolean shouldReconnect ()
        {
            return (ioSession == null || !ioSession.isConnected()) && isTimeForReconnect()
                && (cmfSession.isEnabled() && cmfSession.isSessionTime());
        }

        private boolean isTimeForReconnect ()
        {
            return SystemTime.currentTimeMillis() - lastReconnectAttemptTime >= reconnectIntervalInMillis;
        }

        // TODO JMX Expose reconnect property

        public synchronized int getConnectionFailureCount ()
        {
            return connectionFailureCount;
        }

        public synchronized long getLastReconnectAttemptTime ()
        {
            return lastReconnectAttemptTime;
        }

        public synchronized long getLastConnectTime ()
        {
            return lastConnectTime;
        }
    }

    public synchronized void start ()
    {
        if (reconnectFuture == null)
        {
            reconnectFuture = executor
                .scheduleWithFixedDelay(reconnectTask, 1, 1, TimeUnit.SECONDS);
        }
    }

    public synchronized void stop ()
    {
        if (reconnectFuture != null)
        {
            reconnectFuture.cancel(true);
        }
        
        reconnectTask.disconnect();
    }
}
