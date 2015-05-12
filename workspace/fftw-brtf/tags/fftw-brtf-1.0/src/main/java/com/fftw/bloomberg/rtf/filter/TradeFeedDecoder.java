package com.fftw.bloomberg.rtf.filter;

import com.fftw.bloomberg.rtf.types.RtfCommand;
import com.fftw.bloomberg.rtf.messages.RtfHeader;
import com.fftw.bloomberg.rtf.messages.RtfMessage;
import com.fftw.bloomberg.rtf.messages.RtfMessageFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Decode a stream of bytes into a RtfMessage.
 */
public class TradeFeedDecoder extends CumulativeProtocolDecoder {

    final Logger logger = LoggerFactory.getLogger(getClass().getName());
    final Logger rawMessagesLogger = LoggerFactory.getLogger("RawMessagesLog");
    private static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

    private RtfMessageFactory messageFactory = new RtfMessageFactory();

    /**
     * Decode the buffer into RtfMessages.
     * <p/>
     * We must receive at least 46 bytes to determine what to do with the rest of the data.
     *
     * @param session
     * @param in
     * @param out
     * @return
     * @throws Exception
     */
    protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.remaining() >= 46) {
            // Remember the initial position.
            int start = in.position();

            int stx = in.get(); // first byte should be STX

            if (TradeFeedEncoder.STX == stx) {
                String headerStr = in.getString(45, decoder);
                RtfHeader header = messageFactory.createHeader(headerStr);

                int packetLength = header.getLength();
                int bytesLeft = packetLength - (1 + 45);  // STX + header size

                if (in.remaining() >= bytesLeft) {
                    String dataRecord = in.getString(bytesLeft - 1, decoder); // don't read the ETX
                    logger.debug(dataRecord);
                    int etx = in.get();
                    if (TradeFeedEncoder.ETX != etx) {
                        // todo this is an error, we need to do something!
                        logger.error("Expecting ETX but received '" + etx + "'");
                    }

                    RtfMessage message = null;
                    if (header.getCommand() == RtfCommand.Data) {
                        // The first byte identifies the type of record, the next 38
                        // are uknown, then the record starts
                        message = messageFactory.createMessage(header, dataRecord);
                        if (message.getBody().getMessageType() != '6') {
                            rawMessagesLogger.info(dataRecord);
                        }
                    } else {
                        message = messageFactory.createMessage(header, dataRecord);
                    }

                    out.write(message);
                    return true;
                } else {
                    // We don't have enough bytes, wait for more
                    in.position(start);
                    return false;
                }

            } else {
                // todo This is probably some kind of error.
                logger.error("Expecting STX but received '" + stx + "'");
                return false;
            }
        } else {
            return false;
        }
    }
}
