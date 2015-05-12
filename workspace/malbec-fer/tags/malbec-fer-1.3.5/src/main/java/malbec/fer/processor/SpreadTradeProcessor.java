package malbec.fer.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import malbec.fer.FerretState;
import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.SpreadTrade;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.util.MessageUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO This needs to be re-evaluated
 */
public class SpreadTradeProcessor extends AbstractOrderRequestProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public SpreadTradeProcessor(IDatabaseMapper dbm) {
        super(dbm);
    }

    @Override
    public Map<String, String> process(Map<String, String> sourceMessage,
        Map<String, IOrderDestination> orderDestinations, FerretState ferretState) {

        SpreadTrade spreadTrade = new SpreadTrade(sourceMessage);
        String platform = spreadTrade.getPlatform();

        // lookup the account based on the platform, strategy and security type
        String leg1Account = lookupAccount(platform, spreadTrade.getLeg1().getStrategy(), spreadTrade
            .getLeg1().getSecurityType());
        String leg2Account = lookupAccount(platform, spreadTrade.getLeg2().getStrategy(), spreadTrade
            .getLeg2().getSecurityType());
        boolean badKey = (leg1Account == null) && (leg2Account == null);

        // Determine our destination
        IOrderDestination destination = determineDestination(orderDestinations, platform);
        List<String> errors = new ArrayList<String>();
        ITransportableOrder orderToSend = null;

        // TODO this should already be checked by the time we get here
        // boolean canTradeOnPlatform = canClientTradeOnPlatform(spreadTrade.getClientHostname(), platform);
        boolean canTradeOnPlatform = true;

        boolean activeSesison = destination != null ? destination.isActiveSession() : false;

        String orderRequestType = MessageUtil.getMessageType(sourceMessage);

        System.out.println(orderRequestType);

        if (destination != null && canTradeOnPlatform && activeSesison) {
            // Are we a new/cancel/replace request
            // orderToSend = destination.createTransportableOrder(spreadTrade, orderRequestType);

            if (orderToSend != null) {
                List<String> orderErrors = orderToSend.errors();
                if (orderErrors.size() > 0) {
                    errors.addAll(orderErrors);
                }
            } else {
                errors.add("Unable to create order");
                log.error("Unable to create order for " + destination.getDestinationName());
            }
        }

        if (destination == null || platform == null || badKey || errors.size() > 0 || !canTradeOnPlatform
            || !activeSesison) {
            if (platform == null) {
                errors.add("No platform specified");
            } else if (destination == null) {
                errors.add("No destination mapping for platform.  Valid choices are: "
                    + buildPlatformList(orderDestinations));
            } else if (!canTradeOnPlatform) {
                errors.add("Cannot send order from " + spreadTrade.getClientHostname() + " to " + platform);
            } else if (!activeSesison) {
                errors.add("Destination is not active: " + destination.getDestinationName());
            }

            if (badKey) {
                errors.add("Bad platform/strategy/security type");
            }
            Map<String, String> returnMessage = new HashMap<String, String>();

            returnMessage.put("STATUS", "INVALID");
            // send back a message about the invalid order
            // // jmsApp.broadcastStatus(order.getClientOrderID(), message, errors);
        } else {
            // TODO Lots to be done here!!
            /*
             * if ("NEWORDER".equals(orderRequestType)) { //// processNewOrder(jmsApp, order, orderToSend,
             * message); } else if ("CANCELORDER".equals(orderRequestType)) { ////processCancelOrder(jmsApp,
             * order, orderToSend, message); } else if ("REPLACEORDER".equals(orderRequestType)) {
             * ////processReplaceOrder(jmsApp, order, orderToSend, message); }
             */
            System.out.println("Finish this up!!");
        }
        // TODO finish this
        return null;
    }

    protected String lookupAccount(String platform, String strategy, String securityType) {
        return dbm.lookupAccount(platform, strategy, securityType);
    }
}
