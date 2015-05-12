package com.fftw.bloomberg.cmfp;

import java.io.Serializable;

public class CmfMessage implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -2644838310416066757L;

    private CmfHeader header;

    private CmfTradeRecord tradeRecord;

    public CmfMessage (CmfHeader header, CmfTradeRecord tradeRecord)
    {
        this.header = header;
        this.tradeRecord = tradeRecord;
    }

    public CmfHeader getMutableHeader ()
    {
        return header;
    }

    public String getWireMessage ()
    {
        String headerData = header.getWireMessage();

        String tradeData = tradeRecord != null ? tradeRecord.getProtocolMessage() : "";
        StringBuilder sb = new StringBuilder(headerData.length() + tradeData.length());
        sb.append(headerData).append(tradeData);

        return sb.toString();
    }

    public CmfTradeRecord getTradeRecord ()
    {
        return tradeRecord;
    }

    public int getTradeRecordLength ()
    {
        if (tradeRecord == null)
        {
            return 0;
        }
        else
        {
            return tradeRecord.getLength();
        }
    }
    
    public int getTotalLength() {
        return header.getLength() + getTradeRecordLength();
    }
}
