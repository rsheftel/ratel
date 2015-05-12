package malbec.fer.processor;

import static malbec.fer.OrderStatus.*;
import static malbec.util.StringUtils.getLock;
import static malbec.fer.FerretState.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import malbec.fer.FerretState;
import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.dao.OrderDao;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.util.MessageUtil;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the logic used by the Ferret to process an new order request and send to destination.
 * 
 */
public class OrderProcessor extends AbstractOrderRequestProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public OrderProcessor(IDatabaseMapper dbm) {
        super(dbm);
    }

    /**
     * Process the new order request
     * 
     * @param message
     */
    @Override
    public Map<String, String> process(Map<String, String> sourceMessage,
        Map<String, IOrderDestination> orderDestinations, FerretState ferretState) {
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

        List<String> errors = new ArrayList<String>();
        Map<String, String> returnMessage = new HashMap<String, String>();

        ITransportableOrder orderToSend = createTransportableOrder(order, orderDestinations, ferretState,
            errors, returnMessage);

        if (orderToSend == null) {
            return returnMessage;
        } else {
            // We need to prevent other threads from updating !!
            synchronized (getLock(order.getUserOrderId())) {
                OrderDao dao = OrderDao.getInstance();
                // We are pretty sure we have a valid order, save it and send it
                long orderID = dao.persistOrder(order);
                if (orderID != -1) {
                    return transportOrder(ferretState, order, returnMessage, orderToSend, dao);
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

    protected Map<String, String> transportOrder(FerretState ferretState, Order order,
        Map<String, String> returnMessage, ITransportableOrder orderToSend, OrderDao dao) {
        MessageUtil.setStatus(returnMessage, New.toString());
        MessageUtil.setDestination(returnMessage, order.getExchange());
        // Only send to market if we are in Ticket or DMA
        if (sendToMarket(ferretState)) {
            if (orderToSend.transport()) {
                MessageUtil.setStatus(returnMessage, Sent.toString());
                dao.updateOrder(order, Sent);
                MessageUtil.setDestination(returnMessage, order.getExchange());
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
        } else {
            // TODO Does anything go here? Reject orders should not get here - FerretRouter
            // eliminated
            log.error("Do something here!");
        }
        return returnMessage;
    }

    /**
     * Create a <code>TransportableOrder</code>.
     * 
     * The order is successful if the <code>errors</code> has not increased in size.
     * 
     * @param order
     * @param errors
     * @param returnMessage
     * @return
     */
    protected ITransportableOrder createTransportableOrder(Order order,
        Map<String, IOrderDestination> orderDestinations, FerretState ferretState, List<String> errors,
        Map<String, String> returnMessage) {
        // Determine our destination
        String platform = order.getPlatform();
        IOrderDestination destination = determineDestination(orderDestinations, platform);

        boolean activeSession = destination != null ? destination.isActiveSession() : false;

        ITransportableOrder orderToSend = null;
        MessageUtil.setUserOrderId(returnMessage, order.getUserOrderId());
        MessageUtil.setOrderDate(returnMessage, orderDateAsString(order.getOrderDate()));

        if (activeSession) {
            // This may over-ride setting the destination to Ticket outside of the Ferret
            destination.setForceToTicket(ferretState == Ticket || ferretState == Stage);
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
                StringBuilder sb = new StringBuilder(128);
                sb.append("No destination mapping for platform:").append(platform).append(
                    ".  Valid choices are: ");
                sb.append(buildPlatformList(orderDestinations));
                errors.add(sb.toString());
            } else if (!activeSession) {
                errors.add("Platform is not active: " + destination.getDestinationName());
            }

            // if (badKey) {
            // errors.add("Bad platform/strategy/security type - unable to map account");
            // }
            MessageUtil.setStatus(returnMessage, Invalid.toString());
            // send back a message about the invalid order
            MessageUtil.setUserOrderId(returnMessage, order.getUserOrderId());
            addErrorsToMessage(returnMessage, errors);

            return null;
        }

        return orderToSend;
    }

    private boolean sendToMarket(FerretState ferretState) {
        return Ticket == ferretState || DMA == ferretState;
    }

    private String orderDateAsString(Date orderDate) {
        return new LocalDate(orderDate).toString();
    }

}