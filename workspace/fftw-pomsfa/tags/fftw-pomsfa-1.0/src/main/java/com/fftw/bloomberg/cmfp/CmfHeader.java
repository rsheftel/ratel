package com.fftw.bloomberg.cmfp;

import static com.fftw.bloomberg.cmfp.CmfConstants.SPACES_EIGHT;
import static com.fftw.bloomberg.cmfp.CmfConstants.SPACES_FOUR;
import static com.fftw.bloomberg.cmfp.CmfConstants.SPACES_SIX;
import static com.fftw.bloomberg.cmfp.CmfConstants.SPACES_TWO;

import java.io.Serializable;
import java.util.Date;

import com.fftw.bloomberg.util.FixedWidthFormatter;

public class CmfHeader implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 4820313078184392226L;

    public static final int FIXED_LENGTH = 37;
    
    private String pricingNumber = SPACES_FOUR; // 4

    private String messageType= SPACES_TWO; // 2

    private String messageLength = "00000"; // 5

    private String sequenceNumber = SPACES_EIGHT; // 8

    private String version = SPACES_FOUR; // 4

    private String date = SPACES_EIGHT; // 8

    private String time = SPACES_SIX; // 6

    
    private CmfHeader () {
        
    }

    public CmfHeader (String msgType)
    {
        messageType = msgType;
    }

    
    public CmfHeader (String pn, String msgType, String version)
    {
        pricingNumber = pn;
        messageType = msgType;
        this.version = version;
    }

    public CmfHeader (int pn, String msgType, float version)
    {
        pricingNumber = FixedWidthFormatter.formatNumber(pn, 4);
        messageType = msgType;
        this.version = FixedWidthFormatter.formatNumber(version, 2, 2);
    }
    
    public void setMessageLength (int length)
    {
        messageLength = String.format("%05d", length);
    }

    public void setSequenceNumber (int seqNumber)
    {
        sequenceNumber = String.format("%08d", seqNumber);
    }

    public void setDate (Date date)
    {
        this.date = String.format("%1$tY%1$tm%1$td", date);
    }

    public void setTime (Date date)
    {
        time = String.format("%1$tH%1$tM%1$tS", date);
    }

    public void setDateTime (Date date)
    {
        setDate(date);
        setTime(date);
    }

    public String toString ()
    {
        StringBuilder sb = new StringBuilder(128);

        sb.append(pricingNumber).append("-");
        sb.append(messageType).append("-");
        sb.append(messageLength).append("-");
        sb.append(sequenceNumber).append("-");
        sb.append(version).append("-");
        sb.append(date).append("-");
        sb.append(time);

        return sb.toString();
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public int getLength() {
        
        return Integer.parseInt(messageLength);
    }
    
    public int getSequenceNumber() {
        return Integer.parseInt(sequenceNumber);
    }
    
    public String getWireMessage() {
        StringBuilder sb = new StringBuilder(64);
        
        sb.append(pricingNumber);
        sb.append(messageType);
        sb.append(messageLength);
        sb.append(sequenceNumber);
        sb.append(version);
        sb.append(date);
        sb.append(time);

        return sb.toString();
    }

    public static CmfHeader createFromString(String headerString) {
        CmfHeader header = new CmfHeader();
        header.pricingNumber = headerString.substring(0, 4);
        header.messageType = headerString.substring(4, 6);
        header.messageLength = headerString.substring(6, 11);
        header.sequenceNumber = headerString.substring(11, 19);
        header.version = headerString.substring(19, 23);
        header.date = headerString.substring(23, 31);
        header.time = headerString.substring(31, 37);
        
        return header;
    }
    
    public static void main (String[] args)
    {
//        CmfHeader header = new CmfHeader("1234", "04", "0300");
        CmfHeader header = new CmfHeader(1234, "04", 1.987f);
        header.setMessageLength(100);
        header.setDate(new Date());
        header.setTime(new Date());
        header.setSequenceNumber(1980);
        System.out.println(header);
        
        String rawMessage = header.getWireMessage();
        CmfHeader header2 = CmfHeader.createFromString(rawMessage);
        
        System.out.println(header2);
    }

    public void setPricingNumber (String pricingNumber)
    {
        this.pricingNumber = pricingNumber;
    }

    public void setVersion (String version)
    {
        this.version = version;
    }
}
