package malbec.fer;

import java.util.Map;

import org.joda.time.LocalDate;

/**
 * Interested in the results of processing a message.
 * 
 */
public interface IMessageProcessListener {

    // TODO change the return types back to void
    int broadcastStatus(String userOrderId, LocalDate orderDate, Map<String, String> messageMap);

    int broadcastStatus(String userOrderId, LocalDate orderDate, Map<String, String> messageMap, String errorMessage);

    void sendResponse(Map<String, String> messageMap, String errorMessage);

}
