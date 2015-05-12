package malbec.fer.processor;

import static malbec.fer.FerretState.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import malbec.fer.FerretState;
import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.dao.OrderDao;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.util.MessageUtil;

import static malbec.fer.OrderStatus.*;
import static malbec.util.StringUtils.getLock;

public class StagedOrderProcessor extends OrderProcessor {

    public StagedOrderProcessor(IDatabaseMapper dbm) {
        super(dbm);
    }

    @Override
    public Map<String, String> process(Map<String, String> sourceMessage,
        Map<String, IOrderDestination> orderDestinations, FerretState ferretState) {
        if (Ticket == ferretState || DMA == ferretState) {
            String userOrderId = MessageUtil.getUserOrderId(sourceMessage);
            LocalDate orderDate = extractOrderDate(sourceMessage);
            OrderDao dao = OrderDao.getInstance();
            synchronized (getLock(userOrderId)) {
                Order orderToRelease = dao.findOrderByUserOrderId(userOrderId, orderDate);
                if (orderToRelease == null) {
                    addErrorToMessage(sourceMessage, "Order not found, cannot release "
                        + userOrderId);
                    MessageUtil.setStatus(sourceMessage, Unknown.toString());

                    return sourceMessage;
                }
                
                if (orderToRelease.getStatus() != New) {
                    addErrorToMessage(sourceMessage, "Order is not in Staged status");
                    MessageUtil.setStatus(sourceMessage, orderToRelease.getStatus().toString());
                    
                    MessageUtil.setDestination(sourceMessage, deteremineDestination(orderToRelease.getExchange()));

                    return sourceMessage;
                }
                
                // TODO Find platform and send
                List<String> errors = new ArrayList<String>();
                Map<String, String> returnMessage = new HashMap<String, String>();

                ITransportableOrder orderToSend = createTransportableOrder(orderToRelease, orderDestinations, ferretState,
                    errors, returnMessage);
                
                return transportOrder(ferretState, orderToRelease, returnMessage, orderToSend, dao);
            }
        } else {
            MessageUtil.setStatus(sourceMessage, FerretRejected.toString());
        }

        return sourceMessage;
    }

    private static LocalDate extractOrderDate(Map<String, String> sourceMessage) {
        String ods = MessageUtil.getOrderDate(sourceMessage);

        try {
            return new LocalDate(ods);
        } catch (IllegalArgumentException e) {}

        return null;
    }
}
