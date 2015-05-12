package com.fftw.bloomberg.rtf.client;

import com.fftw.bloomberg.rtf.RealtimeSessionID;
import com.fftw.bloomberg.rtf.messages.RtfMessage;
import com.fftw.sbp.ProtocolIoHandler;
import com.fftw.sbp.ProtocolSession;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.io.IOException;

/**
 *
 */
public class RealTimeFeedClientHandler extends IoHandlerAdapter
        implements ProtocolIoHandler<RealtimeSessionID, RtfMessage> {

    final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private ProtocolSession<RealtimeSessionID, RtfMessage> session;

    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        logger.info("Session Created");
    }

    public void exceptionCaught(IoSession ioSession, Throwable cause) throws Exception {
        logger.error("Handler received exception", cause);

        boolean disconnectNeeded = false;
        if (cause instanceof ProtocolDecoderException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof IOException) {
            SocketAddress remoteAddress = ioSession.getRemoteAddress();
            String message = cause.getMessage();
            logger.error("socket exception (" + remoteAddress + "): " + message);
            disconnectNeeded = true;
        } /*else if (cause instanceof CriticalProtocolCodecException) {
            log.error("critical protocol codec error: " + cause.getMessage());
            disconnectNeeded = true;
        }*/
        else if (cause instanceof ProtocolCodecException) {
            String text = "protocol handler exception: " + cause.getMessage();
            logger.error(text);
        } else {
            logger.error("protocol handler exception", cause);
        }
        if (disconnectNeeded) {
//            if (session != null) {
//              session. disconnect();
//            } else {
            ioSession.close();
//            }
        }
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof RtfMessage) {
            RtfMessage msg = (RtfMessage) message;
            getProtocolSession().processMessage(msg);
        }
    }

    public void setProtocolSession(ProtocolSession<RealtimeSessionID, RtfMessage> session) {
        this.session = session;
    }

    public ProtocolSession<RealtimeSessionID, RtfMessage> getProtocolSession() {
        return session;
    }
}
