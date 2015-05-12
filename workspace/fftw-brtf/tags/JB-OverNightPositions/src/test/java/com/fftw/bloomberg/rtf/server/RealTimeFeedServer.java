package com.fftw.bloomberg.rtf.server;

import org.apache.mina.common.*;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;

import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fftw.bloomberg.batch.messages.BatchPositionTest;
import com.fftw.bloomberg.rtf.filter.TradeFeedCodecFactory;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;

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

        // Create an executor to handle threading
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

        
        List<RtfOnlinePosition> onlinePositions = loadTestOnlineMessages();
        
        RealTimeFeedServerHandler serverHandler = new RealTimeFeedServerHandler(onlinePositions);
        acceptor.bind(new InetSocketAddress(PORT), serverHandler);
        
        System.out.println("Real-time Feed server started on port " + PORT);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                stopServer();
            }
        });
    }

    private static List<RtfOnlinePosition> loadTestOnlineMessages() throws IOException {
        List<RtfOnlinePosition> onlinePositions = new ArrayList<RtfOnlinePosition>();
        
        String filename = "/OnlineMessages-2008-06-04.txt";
        InputStream onlinePositionStream = BatchPositionTest.class.getResourceAsStream(filename);
        BufferedReader onlinePositionReader = new BufferedReader(new InputStreamReader(onlinePositionStream));

        String line = null;
        while ((line = onlinePositionReader.readLine()) != null) {
            String[] pairs = line.split(",");
            RtfOnlinePosition op = RtfOnlinePosition.valueOf(pairs);
            onlinePositions.add(op);
        }
        
        return onlinePositions;
    }

    private static void stopServer() {
        // Shut down the server.
        System.out.println("Unbinding server from port: " + PORT);
        acceptor.unbind(new InetSocketAddress(PORT));
        System.out.println("Stopping threads");
        executor.shutdown();
    }
}
