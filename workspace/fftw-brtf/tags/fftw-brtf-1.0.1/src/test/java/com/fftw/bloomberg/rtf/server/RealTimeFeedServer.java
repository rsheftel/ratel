package com.fftw.bloomberg.rtf.server;

import org.apache.mina.common.*;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fftw.bloomberg.rtf.filter.TradeFeedCodecFactory;

/**
 * This implements the Bloomber side of the Realtime Datafeed.
 */
public class RealTimeFeedServer {
    private static final int PORT = Integer.getInteger("server.port", 9898);

    private static SocketAcceptor acceptor;
    private static ExecutorService executor;

    public static void main(String[] args) throws IOException {
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        // Create an excutor to handle threading
        executor = Executors.newCachedThreadPool();


        acceptor = new SocketAcceptor();
        // To be compatible with MINA 2 we need to disable ThreadModel
        IoServiceConfig acceptorConfig = acceptor.getDefaultConfig();
        acceptorConfig.setThreadModel(ThreadModel.MANUAL);

        DefaultIoFilterChainBuilder filterChainBuilder = acceptor.getDefaultConfig().getFilterChain();
        // Add a logging filter
        filterChainBuilder.addLast("logger", new LoggingFilter());
        // Add the CPU-bound filter - our protocol specific logic
        filterChainBuilder.addLast("protocol", new ProtocolCodecFilter(new TradeFeedCodecFactory()));
        // add the thread pool
        filterChainBuilder.addLast("threadPool", new ExecutorFilter(executor));

        RealTimeFeedServerHandler serverHandler = new RealTimeFeedServerHandler();
        acceptor.bind(new InetSocketAddress(PORT), serverHandler);

        System.out.println("Real-time Feed server started on port " + PORT);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                stopServer();
            }
        });
    }

    private static void stopServer() {
        // Shut down the server.
        System.out.println("Unbinding server from port: " + PORT);
        acceptor.unbind(new InetSocketAddress(PORT));
        System.out.println("Stopping threads");
        executor.shutdown();
    }
}
