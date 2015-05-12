package com.fftw.bloomberg.rtf.server;

import com.fftw.bloomberg.rtf.messages.DefaultRtfMessage;
import com.fftw.bloomberg.rtf.messages.DefaultRtfMessageBody;
import com.fftw.bloomberg.rtf.messages.RtfHeader;
import com.fftw.bloomberg.rtf.messages.RtfMessage;
import com.fftw.bloomberg.rtf.types.RtfCommand;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.joda.time.LocalDate;

/**
 * Handle events generated by receiving messages from the connection.
 * <p/>
 * This is the entry point for the server business logic.
 */
public class RealTimeFeedServerHandler extends IoHandlerAdapter {

    private int pricingNumber = 0;
    private int siteNumber = 0;

    /**
     * Specify the pricing and site number.
     *
     * @param price
     * @param site
     * @throws IllegalArgumentException if either value is zero (0)
     */
    public RealTimeFeedServerHandler(int price, int site) {
        if (price == 0 || site == 0) {
            throw new IllegalArgumentException("Must specify non-zero values: " + price + " : " + site);
        }

        pricingNumber = price;
        siteNumber = site;
    }

    /**
     * Use the default pricing and site number.
     * <p/>
     * The default value should be used by a server implementation.  Client implementations should
     * specify their pricing and site
     */
    public RealTimeFeedServerHandler() {

    }

    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
    }

    /**
     * A full message was received from the sender.
     *
     * @param session
     * @param message
     * @throws Exception
     */
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof RtfMessage) {
            System.out.println("Received RtfMessage: " + message.toString());

            RtfMessage msg = (RtfMessage) message;
            System.out.println("Header="+msg.getHeader());
        }
        super.messageReceived(session, message);
    }

    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
    }

    public void sessionOpened(IoSession session) throws Exception {
        // Send a conenct request
        RtfHeader connect = new RtfHeader(RtfCommand.Connect, new LocalDate(),0);

        session.write(new DefaultRtfMessage(connect));
    }

    public void sessionCreated(IoSession session) throws Exception {
        if (session.getTransportType() == TransportType.SOCKET)
            ((SocketSessionConfig) session.getConfig()).setReceiveBufferSize(2048);

        session.setIdleTime(IdleStatus.READER_IDLE, 10);
        session.setIdleTime(IdleStatus.WRITER_IDLE, 10);
    }


    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {

        if (IdleStatus.READER_IDLE.equals(status)) {
            // create and send a heartbeat message
            System.out.println("Sending Heartbeat");
            // The server does not send a pricing/site number
            RtfHeader heartbeat = new RtfHeader(RtfCommand.Status, new LocalDate(),0,0);

            session.write(new DefaultRtfMessage(heartbeat, new DefaultRtfMessageBody('6', "0")));
            System.out.println(heartbeat);
        } else if (IdleStatus.WRITER_IDLE.equals(status)) {
            System.out.println("Write session idle");
        }

    }
}
