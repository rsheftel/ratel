package com.fftw.bloomberg.cmfp.filter;

import java.nio.charset.Charset;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fftw.bloomberg.cmfp.CmfAimTradeRecord;
import com.fftw.bloomberg.cmfp.CmfHeader;
import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;

//public class CmfDecoder extends CumulativeProtocolDecoder
public class CmfDecoder implements MessageDecoder
{
    private final Logger log = LoggerFactory.getLogger(CmfDecoder.class);

    protected boolean doDecode (IoSession session, ByteBuffer in, ProtocolDecoderOutput out)
        throws Exception
    {
        int start = in.position();
        if (in.remaining() >= 37)
        {
            String headerString = in.getString(CmfHeader.FIXED_LENGTH, Charset.forName("UTF-8")
                .newDecoder());
            CmfHeader header = CmfHeader.createFromString(headerString);

            if (log.isDebugEnabled())
            {
                log.debug("Received header:" + header);
            }

            int recordLength = header.getLength();
            if (in.remaining() >= recordLength)
            {
                CmfTradeRecord record = null;
                if (recordLength > 0)
                {
                    if (in.remaining() >= recordLength)
                    {

                        String tradeRecordStr = in.getString(recordLength, Charset.forName("UTF-8")
                            .newDecoder());
                        try
                        {
                            record = CmfAimTradeRecord.createFromString(tradeRecordStr);
                            if (log.isDebugEnabled())
                            {
                                log.debug("Received trade record: " + tradeRecordStr);
                            }
                        }
                        catch (NumberFormatException e)
                        {
                            log.error("Parsing TradeRecord: ",e);
                        }
                    }
                    else
                    {
                        // Put everything back in the buffer
                        in.position(start);
                        return false;
                    }
                    // record = new CmfAimTradeRecord();
                }
                CmfMessage message = new CmfMessage(header, record);
                out.write(message);
                return true;
            }

        }
        in.position(start);
        return false;
    }

    public MessageDecoderResult decodable (IoSession session, ByteBuffer in)
    {
        if (in.remaining() >= 37)
        {
            return MessageDecoderResult.OK;
        }
        return MessageDecoderResult.NEED_DATA;
    }

    public MessageDecoderResult decode (IoSession session, ByteBuffer in, ProtocolDecoderOutput out)
        throws Exception
    {
        int start = in.position();
        if (in.remaining() >= 37)
        {
            String headerString = in.getString(CmfHeader.FIXED_LENGTH, Charset.forName("UTF-8")
                .newDecoder());
            CmfHeader header = CmfHeader.createFromString(headerString);

            log.info("Received header:" + header);

            int recordLength = header.getLength();
            if (in.remaining() >= recordLength)
            {
                CmfTradeRecord record = null;
                if (recordLength > 0)
                {
                    if (in.remaining() >= recordLength)
                    {
                        String tradeRecordStr = in.getString(recordLength, Charset.forName("UTF-8")
                            .newDecoder());
                        try
                        {
                            record = CmfAimTradeRecord.createFromString(tradeRecordStr);
                            log.info("Received trade record: " + tradeRecordStr);
                        }
                        catch (NumberFormatException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        // Put everything back in the buffer
                        in.position(start);
                        return MessageDecoderResult.NEED_DATA;
                    }
                    // record = new CmfAimTradeRecord();
                }
                CmfMessage message = new CmfMessage(header, record);
                out.write(message);
                return MessageDecoderResult.OK;
            }

        }
        in.position(start);
        return MessageDecoderResult.NEED_DATA;
    }

    public void finishDecode (IoSession session, ProtocolDecoderOutput out) throws Exception
    {
        // TODO Auto-generated method stub

    }

}
