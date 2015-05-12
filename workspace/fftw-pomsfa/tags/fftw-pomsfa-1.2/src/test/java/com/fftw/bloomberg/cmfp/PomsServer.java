package com.fftw.bloomberg.cmfp;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;

public class PomsServer
{
    private static final int PORT = Integer.getInteger("poms.port",9191);

    public static void main(String[] args) throws IOException {
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        PomsServerHandler handler = new PomsServerHandler("2465", "0300");
        SocketAcceptor acceptor = new SocketAcceptor();
        acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new CmfProtocolCodecFactory()));
        acceptor.bind(new InetSocketAddress(PORT), handler);
        System.out.println("server is listenig at port " + PORT);
        
        System.out.println("POMS server started.");
    }

}
