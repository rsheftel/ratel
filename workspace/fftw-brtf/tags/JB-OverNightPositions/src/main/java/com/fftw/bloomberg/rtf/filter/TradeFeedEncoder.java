package com.fftw.bloomberg.rtf.filter;

import com.fftw.bloomberg.rtf.messages.RtfHeader;
import com.fftw.bloomberg.rtf.messages.RtfMessage;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import static java.nio.charset.Charset.forName;
import java.nio.charset.CharsetEncoder;

/**
 * Encode a real-time feed message.
 * <p/>
 * Messages have a <STX><header><... data of length from header><ETX>
 */
public class TradeFeedEncoder implements ProtocolEncoder {

    /**
     * ASCII start of text
     */
    static final byte STX = 0x02;
    
    /**
     * ASCII end of text
     */
    static final byte ETX = 0x03;

    /**
     * Character encoder
     */
    private CharsetEncoder encoder = forName("UTF-8").newEncoder();

    public void encode(IoSession ioSession, Object objectMessage, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {

        RtfMessage message = (RtfMessage) objectMessage;
        // TODO extract the body and determine the length.  This might not be necessary, as we
        // only send header messages
        // set the header length field and generate the protocol string
        if (message.getBody() != null) {
            
        }
        RtfHeader header = message.getHeader();
        header.setLength(45 + 2);
        String strMessage = message.getHeader().protocolString();

        // Allocate enough for the message and the two control bytes
        ByteBuffer buffer = ByteBuffer.allocate(header.getLength(), false);

        buffer.put(STX);
        buffer.putString(strMessage, encoder);
        buffer.put(ETX);

        buffer.flip();

        protocolEncoderOutput.write(buffer);
    }

    public void dispose(IoSession ioSession) throws Exception {
        // Nothing to dispose
    }
}
