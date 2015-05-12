package malbec.fer.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import malbec.fer.IOrderDestination;
import malbec.fer.Order;
import malbec.fer.mapping.IDatabaseMapper;

public abstract class AbstractOrderRequestProcessor implements IOrderRequestProcessor {

    protected final IDatabaseMapper dbm;
    
    protected AbstractOrderRequestProcessor(IDatabaseMapper dbm) {
        this.dbm = dbm;
    }

    protected IOrderDestination determineDestination(Map<String, IOrderDestination> orderDestinations, String platform) {
        if (platform == null) {
            return null;
        }
        return orderDestinations.get(platform.toUpperCase());
    }

    /**
     * Add the list of strings that are errors to the map
     * 
     * @param message
     * @param errors
     * @return
     */
    protected String addErrorsToMessage(Map<String, String> message, List<String> errors) {
        StringBuilder sb = new StringBuilder(1024);
        int i = 1;
    
        // TODO check for existing ERROR_x keys and start from there
        for (String error : errors) {
            message.put("ERROR_" + i, "Failed to send order: " + error);
            sb.append(error).append(";");
            i++;
        }
    
        return sb.toString();
    }

    protected void addErrorToMessage(Map<String, String> returnMessage, String error) {
        List<String> errors = new ArrayList<String>();
        errors.add(error);
        addErrorsToMessage(returnMessage, errors);
    }

    protected String buildPlatformList(Map<String, IOrderDestination> orderDestinations) {
        StringBuffer sb = new StringBuffer(128);
    
        boolean first = true;
        for (String platformKey : orderDestinations.keySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(platformKey);
            first = false;
        }
    
        return sb.toString();
    }

    protected void convertMarketSymbol(Order order, List<String> errors) {
        // tomahawk sends Malbec 'Market' tickers, convert to Bloomberg first
        if (isFuturesOrder(order) && !"EXCEL".equalsIgnoreCase(order.getClientAppName())) {
            String ticker = order.getSymbol();
            String tmpTicker = dbm.mapMarketToBloomberg(ticker);
            if (tmpTicker == null) {
                errors.add("Unable to map MarketTicker to Bloomberg symbol: " + ticker);
            } else {
                ticker = tmpTicker;
            }
            order.setSymbol(ticker);
        }
    }

    protected boolean isFuturesOrder(Order order) {
        return "futures".equalsIgnoreCase(order.getSecurityType());
    }

    public static String deteremineDestination(String exchange) {
        if (exchange == null) {
            return "UNKNOWN";
        }

        if ("TICKET".equalsIgnoreCase(exchange) || "TKTS".equalsIgnoreCase(exchange)) {
            return "TICKET";
        } else {
            return "DMA";
        }
    }
    
}
