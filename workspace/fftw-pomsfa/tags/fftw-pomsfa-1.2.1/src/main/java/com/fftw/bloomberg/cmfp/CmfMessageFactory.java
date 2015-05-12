package com.fftw.bloomberg.cmfp;

import java.util.Date;

public class CmfMessageFactory
{

    private static CmfMessageFactory instance = new CmfMessageFactory();

    public static CmfMessageFactory getInstance ()
    {
        return instance;
    }

    public CmfMessage createHeartbeatAck (String pricingNumber, String specVersion)
    {
        CmfHeader header = createHeader(CmfConstants.HEARTBEAT_ACK);

        CmfMessage message = new CmfMessage(header, null);
        header.setMessageLength(message.getTradeRecordLength());

        return message;
    }

    public CmfMessage createTradeRecordAim ()
    {
        CmfHeader header = createHeader(CmfConstants.TRADE);
        CmfMessage message = new CmfMessage(header, new CmfAimTradeRecord(CmfConstants.TRADE));

        return message;
    }

    private CmfHeader createHeader (String msgType)
    {
        CmfHeader header = new CmfHeader(msgType);
        header.setDateTime(new Date());
        return header;
    }

    public CmfMessage createMessage (String priceNumber, String version, CmfTradeRecord tradeRecord)
    {
        CmfHeader header = new CmfHeader(priceNumber, CmfConstants.TRADE, version);

        return new CmfMessage(header, tradeRecord);
    }

    public CmfMessage createMessage (CmfTradeRecord tradeRecord)
    {

        return new CmfMessage(null, tradeRecord);
    }
}
