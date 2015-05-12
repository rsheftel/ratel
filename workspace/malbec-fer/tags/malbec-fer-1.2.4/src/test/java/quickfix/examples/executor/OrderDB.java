package quickfix.examples.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.ClOrdID;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;

public class OrderDB {

    private static class DatabaseOrder {
        private Message newOrderSingle;
        private DatabaseOrder(Message newOrder) {
            this.newOrderSingle = newOrder;
        }
    }

    private static OrderDB instance = new OrderDB();
    private long orderIdCounter = System.currentTimeMillis() & 0xffff;

    private final Map<String, DatabaseOrder> orders = new HashMap<String, DatabaseOrder>();


    public static OrderDB getInstance() {
        return instance;
    }


    public String addNewOrder(Message newOrderSingle) {
        
        String clientOrderId = null; 
            
        try {
            clientOrderId = newOrderSingle.getString(ClOrdID.FIELD);
        } catch (FieldNotFound e) {
        }
        
        synchronized (instance) {
            if (!orders.containsKey(clientOrderId)) {
                
                String orderId = nextOrderId();
                newOrderSingle.setString(OrderID.FIELD, orderId);
                newOrderSingle.setChar(OrdStatus.FIELD, OrdStatus.NEW);

                DatabaseOrder order = new DatabaseOrder(newOrderSingle);
                orders.put(clientOrderId, order);
                
                return orderId;
            } else {
                // TODO should this be an exception?
                return null;
            }
        }
    }


    public String nextOrderId() {
        synchronized (instance) {
            return String.valueOf(++orderIdCounter);
        }
    }

    public OrdStatus getOrderStatus(String clientOrderId) {
        Message order = getOrder(clientOrderId);
        
        if (order != null) {
            try {
                return new OrdStatus(order.getChar(OrdStatus.FIELD));
            } catch (FieldNotFound e) {
            }
        }
        
        return null;
    }

    public String getOrderId(String clientOrderId) {
        Message order = getOrder(clientOrderId);
        
        if (order != null) {
            try {
                return order.getString(OrderID.FIELD);
            } catch (FieldNotFound e) {
            }
        }
        
        return null;
    }


    public Message getOrder(String clientOrderId) {
        DatabaseOrder order = orders.get(clientOrderId);
        
        if (order != null) {
            return order.newOrderSingle;
        }
        
        return null;
    }


    public void setOrderCancelled(String clientOrderId) {
        synchronized (instance) {
            Message order = getOrder(clientOrderId);
            order.setChar(OrdStatus.FIELD, OrdStatus.CANCELED);
        }
    }


    public void setOrderFilled(String clientOrderId) {
        synchronized (instance) {
            Message order = getOrder(clientOrderId);
            order.setChar(OrdStatus.FIELD, OrdStatus.FILLED);
        }
        
    }

    public Collection<Message> getAllOpenOrders() {
        List<Message> ordersToFill = new ArrayList<Message>(orders.size());

        for (DatabaseOrder order : orders.values()) {
            try {
                if (isOrder(order.newOrderSingle) && isFillable(order.newOrderSingle)) {
                    ordersToFill.add(order.newOrderSingle);
                }
            } catch (FieldNotFound e) {
                throw new FieldNotFoundException(e);
            }
        }

        return ordersToFill;
    }
    
    private boolean isOrder(Message order) throws FieldNotFound {
        String msgType = order.getHeader().getString(MsgType.FIELD);
        
        return (MsgType.ORDER_SINGLE.equals(msgType) || 
                msgType.equals(MsgType.ORDER_CANCEL_REPLACE_REQUEST));
    }
    
    private boolean isFillable(Message order) throws FieldNotFound {
        char orderStatus = order.getChar(OrdStatus.FIELD);
        return orderStatus == OrdStatus.NEW || orderStatus == OrdStatus.PARTIALLY_FILLED;
    }
}
