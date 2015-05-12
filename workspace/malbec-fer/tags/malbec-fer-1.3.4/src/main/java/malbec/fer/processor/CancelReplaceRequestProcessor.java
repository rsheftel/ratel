package malbec.fer.processor;

import static malbec.fer.FerretState.*;

import static malbec.fer.OrderStatus.*;

import static malbec.util.StringUtils.getLock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.FerStageEvent;
import malbec.fer.FerretRouter;
import malbec.fer.FerretState;
import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.OrderStatus;
import malbec.fer.dao.OrderDao;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.util.MessageUtil;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelReplaceRequestProcessor extends OrderProcessor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Collection<OrderStatus> nonReplacable = new ArrayList<OrderStatus>();

    private final FerretRouter ferret;

    public CancelReplaceRequestProcessor(IDatabaseMapper dbm) {
       this(dbm, null);
    }
    
    public CancelReplaceRequestProcessor(IDatabaseMapper dbm, FerretRouter fe) {
        super(dbm);
        ferret = fe;
        initializeNonReplacableOrders();
    }

    private void initializeNonReplacableOrders() {
        nonReplacable.add(Cancelled);
        nonReplacable.add(PlatformRejected);
        nonReplacable.add(Replaced);
        nonReplacable.add(PendingCancel);
        nonReplacable.add(PendingNew);
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fer.processor.OrderProcessor#process(java.util.Map, java.util.Map)
     */
    @Override
    public Map<String, String> process(Map<String, String> sourceMessage,
        Map<String, IOrderDestination> orderDestinations, FerretState ferretState) {

        CancelReplaceRequest cancelReplaceRequest = new CancelReplaceRequest(sourceMessage);
        OrderDao dao = OrderDao.getInstance();
        Map<String, String> returnMessage = new HashMap<String, String>();
        MessageUtil.setUserOrderId(returnMessage, cancelReplaceRequest.getUserOrderId());
        MessageUtil.setOriginalUserOrderId(returnMessage, cancelReplaceRequest.getOriginalUserOrderId());
        MessageUtil.setOrderDate(returnMessage, new LocalDate(cancelReplaceRequest.getOrderDate()).toString());

        // TODO ensure that we received the correctly formatted request
        // Lock on the order we are trying to cancel
        if (cancelReplaceRequest.getOriginalUserOrderId() == null) {
            addErrorToMessage(returnMessage, "Missing OriginalOrderId" );
            MessageUtil.setStatus(returnMessage, FerretRejected.toString());
            return returnMessage;
        }
        
        synchronized (getLock(cancelReplaceRequest.getOriginalUserOrderId())) {
            Order orderToCancel = dao.findOrderByUserOrderId(cancelReplaceRequest.getOriginalUserOrderId(),
                cancelReplaceRequest.getOrderDate());
            if (orderToCancel == null) {
                addErrorToMessage(returnMessage, "Order not found, cannot cancel "
                    + cancelReplaceRequest.getOriginalUserOrderId());
                MessageUtil.setStatus(returnMessage, Unknown.toString());

                return returnMessage;
            } else if (nonReplacable.contains(orderToCancel.getStatus())) {
                String orderStatus = orderToCancel.getStatus().toString();
                addErrorToMessage(returnMessage, "Order of status " + orderStatus + " cannot be replaced "
                    + cancelReplaceRequest.getOriginalUserOrderId());
                MessageUtil.setStatus(returnMessage, PlatformRejected.toString());

                return returnMessage;
            }

            // Create a cancel request to persist
            CancelReplaceRequest requestToPersist = merge(cancelReplaceRequest, orderToCancel);

            // Determine our destination
            String platform = requestToPersist.getPlatform();
            IOrderDestination destination = determineDestination(orderDestinations, platform);

            boolean activeSession = destination != null ? destination.isActiveSession() : false;

            ITransportableOrder orderToSend = null;
            List<String> errors = new ArrayList<String>();

            if (activeSession) {
                destination.setForceToTicket(Ticket == ferretState || Stage == ferretState);
                convertMarketSymbol(requestToPersist, errors);
                orderToSend = destination.createReplaceOrder(requestToPersist);

                if (orderToSend != null) {
                    List<String> orderErrors = orderToSend.errors();
                    if (orderErrors.size() > 0) {
                        errors.addAll(orderErrors);
                    }
                } else {
                    errors.add("Unable to create order");
                    log.error("Unable to create order for " + destination.getDestinationName());
                }
                if (errors.size() > 0) {
                    addErrorsToMessage(returnMessage, errors);
                    return returnMessage;
                }
                if (errors.size() > 0) {
                    addErrorsToMessage(returnMessage, errors);
                    return returnMessage;
                }
            } else {
                addErrorToMessage(returnMessage, "Session not active");
                return returnMessage;
            }

            long orderId = dao.persistOrder(requestToPersist);
            if (orderId != -1) {
                if (Stage == ferretState || orderToCancel.getStatus() == New) {
                    dao.updateOrder(requestToPersist, New);
                    MessageUtil.setStatus(returnMessage, New.toString());
                    // if we are in Stage, the original order is probably not live, so cancel it
                    dao.updateOrder(orderToCancel, Cancelled);
                    returnMessage.put("ORIGINALORDERSTATUS", Cancelled.toString());
                    if (ferret != null) {
                        FerStageEvent fse = new FerStageEvent(this, "CANCELLED", orderToCancel.getClientOrderId());
                        ferret.propertyChange(fse);
                    }
                    return returnMessage;
                }

                if (orderToSend.transport()) {
                    dao.updateOrder(requestToPersist, CancelReplaceRequested);
                    MessageUtil.setStatus(returnMessage, CancelReplaceRequested.toString());
                } else {
                    MessageUtil.setStatus(returnMessage, CancelReplaceRequestFailed.toString());
                    List<String> sendErrors = orderToSend.errors();

                    if (sendErrors.size() > 0) {
                        String combinedErrors = addErrorsToMessage(returnMessage, sendErrors);
                        dao.updateOrder(requestToPersist, CancelReplaceRequestFailed, combinedErrors);
                    } else {
                        returnMessage.put("ERROR_1", "Failed to send cancel order");
                        dao.updateOrder(requestToPersist, CancelReplaceRequestFailed);
                    }
                }
                return returnMessage;
            } else {
                MessageUtil.setStatus(returnMessage, requestToPersist.getStatus().toString());
                return returnMessage;
            }
        }
    }

    /**
     * Combine the request with the original order.
     * 
     * This populates the ClientOrderId and OriginalClientOrderId from the original order.
     * 
     * @param cancelReplaceRequest
     * @param sourceOrder
     * @return
     */
    private CancelReplaceRequest merge(CancelReplaceRequest cancelReplaceRequest, Order sourceOrder) {
        // Take the original order and replace fields from the request
        Map<String, String> sourceMap = sourceOrder.toMap();
        sourceMap.putAll(cancelReplaceRequest.toMap());

        CancelReplaceRequest cr = new CancelReplaceRequest(sourceMap);
        cr.setStatus(OrderStatus.New);

        // We don't want to over-write these two fields
        cr.setUserOrderId(cancelReplaceRequest.getUserOrderId());
        cr.setClientOrderId(cancelReplaceRequest.getClientOrderId());

        // populate the fields from the database that track the FIX ClientOrderIds
        cr.setOriginalUserOrderId(sourceOrder.getUserOrderId());
        cr.setOriginalClientOrderId(sourceOrder.getClientOrderId());

        return cr;
    }

}
