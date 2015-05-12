package malbec.fer.processor;

import static malbec.fer.OrderStatus.*;
import static malbec.util.StringUtils.getLock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.dao.OrderDao;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.MessageUtil;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the logic used by the Feret to process an new order request and send to destination.
 * 
 */
public class OrderProcessor extends BaseOrderRequestProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public OrderProcessor(DatabaseMapper dbm) {
        super(dbm);
    }

    /**
     * Process the new order request
     * 
     * @param message
     */
    public Map<String, String> process(Map<String, String> sourceMessage, Map<String, IOrderDestination> orderDestinations) {
        // assumes that the trade has passed the security checks
        // map the account from the strategy
        // determine the destination
        // determine if the session is active
        // create the transportable order
        // persist order
        // transport the order

        // any errors need to be return to the caller - error message maps require:
        // clientOrderId
        // error_x or status
        Order order = new Order(sourceMessage);

//        String account = lookupAccount(order.getPlatform(), order.getStrategy(), order.getSecurityType());
//        order.setAccount(account);
//        boolean badKey = account == null;

        // Determine our destination
        String platform = order.getPlatform();
        IOrderDestination destination = determineDestination(orderDestinations, platform);

        boolean activeSession = destination != null ? destination.isActiveSession() : false;

        ITransportableOrder orderToSend = null;
        List<String> errors = new ArrayList<String>();
        Map<String, String> returnMessage = new HashMap<String, String>();
        MessageUtil.setUserOrderId(returnMessage, order.getUserOrderId());
        MessageUtil.setOrderDate(returnMessage, orderDateAsString(order.getOrderDate()));
        

        if (activeSession) {
            orderToSend = destination.createOrder(order);

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

        if (destination == null || platform == null || errors.size() > 0 || !activeSession) {
            if (platform == null) {
                errors.add("No platform specified");
            } else if (destination == null) {
                errors.add("No destination mapping for platform.  Valid choices are: "
                        + buildPlatformList(orderDestinations));
            } else if (!activeSession) {
                errors.add("Destination is not active: " + destination.getDestinationName());
            }

//            if (badKey) {
//                errors.add("Bad platform/strategy/security type - unable to map account");
//            }
            MessageUtil.setStatus(returnMessage, Invalid.toString());
            // send back a message about the invalid order
            MessageUtil.setUserOrderId(returnMessage, order.getUserOrderId());
            addErrorsToMessage(returnMessage, errors);

            return returnMessage;
        } else {
            // We need to prevent other threads from updating !!
            synchronized (getLock(order.getClientOrderId())) {
                OrderDao dao = OrderDao.getInstance();
                // We are pretty sure we have a valid order, save it and send it
                long orderID = dao.persistOrder(order);
                if (orderID != -1) {
                    if (orderToSend.transport()) {
                        MessageUtil.setStatus(returnMessage, Sent.toString());
                        dao.updateOrder(order, Sent);
                        // TODO this should be on exceptions
                        // sendPersistenceError(order, "Unable to update status", e);
                    } else {
                        MessageUtil.setStatus(returnMessage, Failed.toString());
                        List<String> sendErrors = orderToSend.errors();

                        if (sendErrors.size() > 0) {
                            String combinedErrors = addErrorsToMessage(returnMessage, sendErrors);
                            dao.updateOrder(order, Failed, combinedErrors);
                            // TODO this should be on exceptions
                            // sendPersistenceError(order, "Unable to update status", e);
                        } else {
                            returnMessage.put("ERROR_1", "Failed to send order");
                            dao.updateOrder(order, Failed);
                        }
                    }
                    return returnMessage;
                } else {
                    // Tried to save a duplicate order
                    addErrorToMessage(returnMessage, "Unable to persist order, " + order.getStatus());
                    MessageUtil.setStatus(returnMessage, Duplicate.toString());
                    MessageUtil.setReplyTo("Originator", returnMessage);
                    return returnMessage;
                }
            }
        }
    }

    private String orderDateAsString(Date orderDate) {
        return new LocalDate(orderDate).toString();
    }

}
