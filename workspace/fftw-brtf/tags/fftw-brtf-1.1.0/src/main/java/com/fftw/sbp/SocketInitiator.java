package com.fftw.sbp;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class SocketInitiator {

    private final ScheduledExecutorService executor;
    private final ConnectTask connectionTask;
    private ProtocolSessionConfig sessionConfig;

    private Future<?> reconnectFuture;

    public SocketInitiator(SessionApplication app, ProtocolSession session, ProtocolSessionConfig sessionConfig, String timerThreadName) {
        this.executor = Executors.newSingleThreadScheduledExecutor(new TimerThreadFactory(timerThreadName));
        this.sessionConfig = sessionConfig;

        connectionTask = new ConnectTask(session, sessionConfig);
        connectionTask.addObserver(app);

    }

    public SocketInitiator(SessionApplication app, ProtocolSession session, ProtocolSessionConfig sessionConfig) {
        this(app, session, sessionConfig, "Timer");
    }

    /**
     * Start the connection.
     */
    public synchronized void start() {
        if (reconnectFuture == null) {
            // wait for 1 second and every reconnect interval
            reconnectFuture = executor.scheduleWithFixedDelay(connectionTask, 1, sessionConfig.getReconnectInterval(), TimeUnit.SECONDS);
        }
    }

    /**
     * Stop the reconnect logic.
     * <p/>
     * The task to ensure that we are connected is cancelled.  If we are
     * connected, the connection left in tact.
     */
    public synchronized void stop() {
        if (reconnectFuture != null) {
            reconnectFuture.cancel(true);
        }
    }

    private static class TimerThreadFactory implements ThreadFactory {

        private String threadName;

        public TimerThreadFactory(String threadName) {
            this.threadName = threadName;
        }

        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(true);
            return thread;
        }

    }
}
