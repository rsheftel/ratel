package com.fftw.bloomberg.aggregator;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.mina.NetworkingOptions;

import com.fftw.bloomberg.cmfp.CmfConstants;
import com.fftw.bloomberg.cmfp.CmfHeader;
import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfMessageFactory;
import com.fftw.bloomberg.cmfp.CmfSession;
import com.fftw.bloomberg.cmfp.mina.CmfSessionConnector;

public class CmfProtocolHandler extends IoHandlerAdapter
{
    private final Logger log = LoggerFactory.getLogger(CmfProtocolHandler.class);

    private final NetworkingOptions networkingOptions;

    private final CmfSession cmfSession;

    public CmfProtocolHandler (CmfSession cmfSession, NetworkingOptions networkingOptions)
    {
        this.cmfSession = cmfSession;
        this.networkingOptions = networkingOptions;
    }

    public void sessionOpened (IoSession session)
    {
        // Set reader idle time to 10 seconds.
        // sessionIdle(...) method will be invoked when no data is read
        // for 10 seconds.
        session.setIdleTime(IdleStatus.READER_IDLE, 10);
        session.setIdleTime(IdleStatus.WRITER_IDLE, 10);
        cmfSession.setIoSession(session);
    }

    public void sessionClosed (IoSession session)
    {
        // Print out total number of bytes read from the remote peer.
        System.err.println("Total " + session.getReadBytes() + " byte(s)");

    }

    public void sessionIdle (IoSession session, IdleStatus status)
    {
    }

    public void messageReceived (IoSession session, Object message)
    {
        // The message will be a CmfMessage
        if (message instanceof CmfMessage)
        {
            messageReceived(session, (CmfMessage)message);
        }
        else
        {
            log.info("Ignoring message:" + message.getClass());
        }
    }

    protected void messageReceived (IoSession session, CmfMessage message)
    {

        CmfHeader header = message.getMutableHeader();
        log.debug("Received message: " + header);
        int seqNum = header.getSequenceNumber();
        cmfSession.receivedMessage(seqNum);

        if (CmfConstants.HEARTBEAT.equals(header.getMessageType()))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Received heartbeat: " + header);
            }
            // Respond with a heartbeat ack
            CmfMessage heartbeatAck = CmfMessageFactory.getInstance().createHeartbeatAck(
                cmfSession.getPricingNumber(), cmfSession.getSpecVersion());

            if (sendMessage(heartbeatAck))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Sent heartbeat ACK: " + heartbeatAck.getMutableHeader());
                }
            }
            else
            {
                log.warn("Failed to send heartbeat ACK");
            }

            return;
        }
        else if (CmfConstants.TRADE_RECEIPT.equals(header.getMessageType())
            || CmfConstants.ACCEPT_REJECT.equals(header.getMessageType()))
        {
            // must send Trade ACK and ACCEPT/REJECT message to application

            cmfSession.getCmfApplication().fromApp(message, cmfSession.getCmfSessionID());
        }
    }

    private boolean sendMessage (CmfMessage message)
    {
        return cmfSession.sendMessage(message);
    }

    @Override
    public void exceptionCaught (IoSession session, Throwable cause) throws Exception
    {
        // TODO Auto-generated method stub
        super.exceptionCaught(session, cause);
    }

    public void sessionCreated (IoSession ioSession) throws Exception
    {

        IoSessionConfig cfg = ioSession.getConfig();
        if (cfg instanceof SocketSessionConfig)
        {
            SocketSessionConfig sCfg = (SocketSessionConfig)cfg;
            sCfg.setKeepAlive(true);
        }
        networkingOptions.apply(ioSession);
        ioSession.setAttribute(CmfSessionConnector.CMF_SESSION, cmfSession);
        // cmfSession.setResponder(new IoSessionResponder(session));
        log.info("MINA session created: " + ioSession.getLocalAddress());
    }
}