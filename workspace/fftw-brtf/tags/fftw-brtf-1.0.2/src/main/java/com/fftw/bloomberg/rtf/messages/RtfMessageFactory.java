package com.fftw.bloomberg.rtf.messages;

import com.fftw.bloomberg.rtf.types.RtfCommand;
import static com.fftw.util.strings.FixedWidthExtractor.extractStringNoTrim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RtfMessageFactory {

    final Logger logger = LoggerFactory.getLogger(getClass().getName());
    final Logger missingFeature = LoggerFactory.getLogger("UnimplementedFeatureLog");

    public RtfMessage createMessage(String headerStr, String bodyStr) {
        RtfHeader header = RtfHeader.valueOf(headerStr);

        return new DefaultRtfMessage(header);
    }

    public RtfMessage createMessage(RtfHeader header, String bodyStr) {

        RtfMessageBody body = null;
        if (RtfCommand.Data == header.getCommand() && bodyStr != null && bodyStr.length() > 0) {
            // the first byte tells us the type of data
            char dataIdentifier = bodyStr.charAt(0);

            switch (dataIdentifier) {
                case '1': // Online trade feed
                    missingFeature.info("Online trade feed: " + bodyStr);
                    body = new DefaultRtfMessageTradeBody(dataIdentifier);
                    break;
                case '2': // Online new security or Online customer feed (??? TODO)
                    missingFeature.info("Online new security or customer feed: " + bodyStr);
                    body = new DefaultRtfMessageTradeBody(dataIdentifier);
                    break;
                case '3': // Online price feed
                    missingFeature.info("Online price feed: " + bodyStr);
                    body = new DefaultRtfMessageTradeBody(dataIdentifier);
                    break;
                case '4': // Online position feed
                    body = createOnlinePosition(header, bodyStr);
                    break;
                case '5': // End of day record
                    missingFeature.info("End of day record: " + bodyStr);
                    body = createEndOfDay(header, bodyStr);
                    break;

                case '8': // Online counter-party feed
                    missingFeature.info("Online counter-party feed: " + bodyStr);
                    body = new DefaultRtfMessageTradeBody(dataIdentifier);
                    break;
                case 'F': // Online analytic feed
                    missingFeature.info("Online analytic feed: " + bodyStr);
                    body = new DefaultRtfMessageTradeBody(dataIdentifier);
                    break;
                default:
                    logger.warn("Unknown date type: " + bodyStr);
                    body = new DefaultRtfMessageTradeBody(dataIdentifier);
                    break;
            }
        } else if (RtfCommand.Status == header.getCommand()) {
            logger.debug("received status message:" + bodyStr);
            body = new DefaultRtfMessageBody('6', bodyStr);
        }

        return new DefaultRtfMessage(header, body);
    }

    private RtfMessageTradeBody createOnlinePosition(RtfHeader messageHeader, String bodyStr) {
        // skip the first 1 + 38 bytes
        return RtfOnlinePosition.valueOf(messageHeader.getDate(), messageHeader.getSequenceNumber(),
                extractStringNoTrim(bodyStr, 40, 585));
    }

    private RtfMessageBody createEndOfDay(RtfHeader messageHeader, String bodyStr) {
        // skip the first 1 + 38 bytes
        return RtfEndOfDay.valueOf(messageHeader.getDate(), messageHeader.getSequenceNumber(),
                extractStringNoTrim(bodyStr, 40, 40 + 29));
    }

    public RtfHeader createHeader(String headerStr) {
        return RtfHeader.valueOf(headerStr);
    }
}
