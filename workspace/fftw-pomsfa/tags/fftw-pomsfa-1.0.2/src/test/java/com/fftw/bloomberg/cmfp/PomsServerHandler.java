package com.fftw.bloomberg.cmfp;

import java.util.Date;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;

public class PomsServerHandler extends IoHandlerAdapter
{

    private int sequenceNumber = 1;
    private int missedHeartBeatCount = 0;

    public void exceptionCaught (IoSession session, Throwable t) throws Exception
    {
        t.printStackTrace();
        session.close();
    }

    /**
     * Process messages from a CMFP Client (POTS client).
     * 
     */
    public void messageReceived (IoSession session, Object msg) throws Exception
    {

        CmfMessage message = (CmfMessage) msg;
        CmfHeader header = message.getMutableHeader();
        
        if (CmfConstants.HEARTBEAT_ACK.equals(header.getMessageType())) {
            decreaseHeartbeatMissed();
            System.out.println("Received heartbeat ack");
        } else if (CmfConstants.TRADE.equals(header.getMessageType())) {
            System.out.println("Received Trade Information");
            CmfMessage tradeReceipt = createTradeReceipt("2465", "0300");
            sendMessage(session, tradeReceipt);
            System.out.println("Sending Trade Accept");
            CmfMessage tradeAccept = createTradeAccept("2465", "0300");
            sendMessage(session, tradeAccept);
            
        }
    }

    private CmfMessage createTradeAccept (String priceNumber, String version)
    {
        CmfHeader header = createHeader(priceNumber, version, CmfConstants.ACCEPT_REJECT);

        // Add the rest of the record
        CmfTradeRecord tradeRecord = new CmfAimTradeRecord(CmfConstants.ACCEPT_REJECT);
        tradeRecord.setReturnCode(0);

        CmfMessage message = new CmfMessage(header, tradeRecord);

        return message;
    }

    public void sessionCreated (IoSession session) throws Exception
    {
        System.out.println("Session created...");

        if (session.getTransportType() == TransportType.SOCKET)
            ((SocketSessionConfig)session.getConfig()).setReceiveBufferSize(2048);

        session.setIdleTime(IdleStatus.READER_IDLE, 10);
        session.setIdleTime(IdleStatus.WRITER_IDLE, 10);
        missedHeartBeatCount = 0;
    }

    @Override
    public void sessionOpened (IoSession session) throws Exception
    {
        System.out.println("Session opened");
        super.sessionOpened(session);
    }

    @Override
    public void sessionIdle (IoSession session, IdleStatus status) throws Exception
    {
        System.out.println("Idle connection:" + status);
        if (closeDueToMissedHeartbeat()) {
            session.close();
            System.out.println("Closing session due to missed heartbeats");
            return;
        }
        if (IdleStatus.READER_IDLE.equals(status))
        {
            // create and send a heartbeat message
            System.out.println("Sending Heartbeat");
            CmfMessage heartbeat = createHeartbeat("2465", "0300");

            sendMessage(session, heartbeat);
            increaseHeartbeatMissed();
            System.out.println(heartbeat.getMutableHeader());
        } else if (IdleStatus.WRITER_IDLE.equals(status)) {
            System.out.println("Write session idle");
        }

    }

    private synchronized boolean closeDueToMissedHeartbeat ()
    {
        return missedHeartBeatCount >= 10;
    }

    private synchronized void increaseHeartbeatMissed ()
    {
        missedHeartBeatCount++;
    }
    
    private synchronized void decreaseHeartbeatMissed ()
    {
        missedHeartBeatCount--;
        
    }
    

    private void sendMessage (IoSession session, CmfMessage heartbeat)
    {
        CmfHeader header = heartbeat.getMutableHeader();
        int recordLength = heartbeat.getTradeRecordLength();
        header.setMessageLength(recordLength);
        header.setSequenceNumber(nextSequenceNumber());
        session.write(heartbeat.getWireMessage());
    }

    
    private CmfMessage createHeartbeat (String priceNumber, String version)
    {
        CmfHeader header = createHeader(priceNumber, version, CmfConstants.HEARTBEAT);

        // Add the rest of the record

        CmfMessage message = new CmfMessage(header, null);

        return message;
    }

    private CmfMessage createTradeReceipt (String priceNumber, String version)
    {
        CmfHeader header = createHeader(priceNumber, version, CmfConstants.TRADE_RECEIPT);

        // Add the rest of the record

        CmfMessage message = new CmfMessage(header, null);

        return message;
    }
    
    private CmfHeader createHeader (String priceNumber, String version, String msgType)
    {
        CmfHeader header = new CmfHeader(priceNumber, msgType, version);
        header.setDateTime(new Date());
        return header;
    }

    private synchronized int nextSequenceNumber ()
    {
        return sequenceNumber++;
    }

    @Override
    public void sessionClosed (IoSession session) throws Exception
    {
        System.out.println("Session closed!!!");
    }

}