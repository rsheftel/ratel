package malbec.fer.processor;

import static malbec.fer.FerretState.*;
import static malbec.fer.OrderStatus.*;

import static malbec.util.StringUtils.getLock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import malbec.fer.CancelRequest;
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

public class CancelRequestProcessor extends OrderProcessor {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Collection<OrderStatus> nonCancellable = new ArrayList<OrderStatus>();

    private final FerretRouter ferret;

    public CancelRequestProcessor(IDatabaseMapper dbm) {
        this(dbm, null);
    
    }

    public CancelRequestProcessor(IDatabaseMapper dbm, FerretRouter fr) {
        super(dbm);
        ferret = fr;
        initializeNonCancellableOrders();
    }

    private void initializeNonCancellableOrders() {
        nonCancellable.add(Cancelled);
        nonCancellable.add(PlatformRejected);
        nonCancellable.add(Replaced);
        nonCancellable.add(PendingCancel);
        nonCancellable.add(PendingNew);
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fer.processor.OrderProcessor#process(java.util.Map, java.util.Map)
     */
    @Override
    public Map<String, String> process(Map<String, String> sourceMessage,
        Map<String, IOrderDestination> orderDestinations, FerretState ferretState) {
        CancelRequest cancelRequest = new CancelRequest(sourceMessage);
        OrderDao dao = OrderDao.getInstance();
        Map<String, String> returnMessage = new HashMap<String, String>();
        MessageUtil.setUserOrderId(returnMessage, cancelRequest.getUserOrderId());
        MessageUtil.setOriginalUserOrderId(returnMessage, cancelRequest.getOriginalUserOrderId());

        // Lock on the order we are trying to cancel
        synchronized (getLock(cancelRequest.getOriginalUserOrderId())) {
            Order orderToCancel = dao.findOrderByUserOrderId(cancelRequest.getOriginalUserOrderId(),
                cancelRequest.getOrderDate());
            if (orderToCancel == null) {
                addErrorToMessage(returnMessage, "Order not found, cannot cancel "
                    + cancelRequest.getOriginalUserOrderId());
                MessageUtil.setStatus(returnMessage, Unknown.toString());

                return returnMessage;
            } else if (nonCancellable.contains(orderToCancel.getStatus())) {
                String orderStatus = orderToCancel.getStatus().toString();
                addErrorToMessage(returnMessage, "Order of status " + orderStatus + " cannot be cancelled "
                    + cancelRequest.getOriginalUserOrderId());
                MessageUtil.setStatus(returnMessage, PlatformRejected.toString());

                return returnMessage;
            }

            // Create a cancel request to persist
            CancelRequest requestToPersist = merge(cancelRequest, orderToCancel);

            // Determine our destination
            String platform = requestToPersist.getPlatform();
            IOrderDestination destination = determineDestination(orderDestinations, platform);

            boolean activeSession = destination != null ? destination.isActiveSession() : false;

            ITransportableOrder orderToSend = null;
            List<String> errors = new ArrayList<String>();

            if (activeSession) {
                destination.setForceToTicket(Ticket == ferretState || Stage == ferretState);
                orderToSend = destination.createCancelOrder(requestToPersist);

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
                if (/*Stage == ferretState ||*/ orderToCancel.getStatus() == New) {
                    dao.updateOrder(requestToPersist, Accepted);
                    MessageUtil.setStatus(returnMessage, Accepted.toString());
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
                    dao.updateOrder(requestToPersist, CancelRequested);
                    MessageUtil.setStatus(returnMessage, CancelRequested.toString());
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

    private CancelRequest merge(CancelRequest cancelRequest, Order sourceOrder) {
        Map<String, String> sourceMap = sourceOrder.toMap();
        sourceMap.putAll(cancelRequest.toMap());

        CancelRequest cr = new CancelRequest(sourceMap);
        cr.setStatus(OrderStatus.New);

        // We don't want to over-write these two fields
        cr.setUserOrderId(cancelRequest.getUserOrderId());
        cr.setClientOrderId(cancelRequest.getClientOrderId());

        // populate the fields from the database that track the FIX ClientOrderIds
        cr.setOriginalUserOrderId(sourceOrder.getUserOrderId());
        cr.setOriginalClientOrderId(sourceOrder.getClientOrderId());

        return cr;
    }

}
