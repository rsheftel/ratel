package malbec.fer;

import java.util.Map;

/**
 * Processes a message.
 * 
 */
public interface IMessageProcessor {
    void processMessage(IMessageProcessListener mpl, Map<String, String> message);
}
