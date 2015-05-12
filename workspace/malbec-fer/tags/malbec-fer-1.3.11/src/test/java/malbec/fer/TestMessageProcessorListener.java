/**
 * 
 */
package malbec.fer;

import java.util.HashMap;
import java.util.Map;

import malbec.util.MessageUtil;

import org.joda.time.LocalDate;

final class TestMessageProcessorListener implements IMessageProcessListener {
    private final Map<String, String> ordersWithErrors = new HashMap<String, String>();
    private final Map<String, Map<String, String>> processedOrders = new HashMap<String, Map<String, String>>();

    @Override
    public int broadcastStatus(String userOrderId, LocalDate orderDate, Map<String, String> messageMap) {
        if (messageMap.containsKey("ERROR_1")) {
            ordersWithErrors.put(userOrderId, messageMap.get("ERROR_1"));
            return ordersWithErrors.size();
        }

        processedOrders.put(userOrderId, messageMap);
        return processedOrders.size();
    }

    @Override
    public int broadcastStatus(String userOrderId, LocalDate orderDate, Map<String, String> messageMap, String errorMessage) {
        if (errorMessage != null && errorMessage.length() > 0) {
            ordersWithErrors.put(userOrderId, errorMessage);
        }

        return ordersWithErrors.size();
    }

    @Override
    public void sendResponse(Map<String, String> messageMap, String errorMessage) {
        String userOrderId = MessageUtil.getUserOrderId(messageMap);
        ordersWithErrors.put(userOrderId, errorMessage);
    }

    public int getMessagesWithErrorCount() {
        return ordersWithErrors.size();
    }

    public String getErrorMessageFor(String userOrderId) {
        return ordersWithErrors.get(userOrderId);
    }

    public void reset() {
        ordersWithErrors.clear();
        processedOrders.clear();
    }
}