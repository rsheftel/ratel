package malbec.fer;

import java.util.Map;

/**
 * Processes a message.
 * 
 */
public interface IMessageProcessor {
    Map<String, String> processMessage(IMessageProcessListener mpl, Map<String, String> message);
}
