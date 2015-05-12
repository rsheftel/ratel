package malbec.fer.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import malbec.bloomberg.types.BBYellowKey;
import malbec.fer.IOrderDestination;
import malbec.fer.Order;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.util.FuturesSymbolUtil;

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
        if (!"EXCEL".equalsIgnoreCase(order.getClientAppName())) {
            String ticker = order.getSymbol();
            String tmpTicker = dbm.mapMarketToBloomberg(ticker);
            // We are now receiving some equity orders with internal tickers
            // This is only a guaranteed error if it is a futures
            if (tmpTicker == null && order.isFutures()) {
                errors.add("Unable to map MarketTicker to Bloomberg symbol: " + ticker);
            } else if (tmpTicker != null){
                ticker = tmpTicker;
            }
            order.setSymbol(ticker);
        }

        // TODO extract this to a method in case we need it to populate the existing orders
        if (order.isFutures()) {
            // map the YellowKey
            String symbolRoot = FuturesSymbolUtil.extractSymbolRoot(order.getSymbol());
            BBYellowKey yellowKey = dbm.lookupYellowKey(symbolRoot);
            order.setYellowKey(yellowKey);
        } else if (order.isEquity()) {
            order.setYellowKey(BBYellowKey.Equity);
        }
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
