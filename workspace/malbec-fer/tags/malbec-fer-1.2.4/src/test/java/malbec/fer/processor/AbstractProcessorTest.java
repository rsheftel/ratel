package malbec.fer.processor;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.CancelRequest;
import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.mapping.DatabaseMapper;

public class AbstractProcessorTest {

    protected static final class TestTransportableOrder implements ITransportableOrder {
            @Override
            public List<String> errors() {
                return Collections.<String> emptyList();
            }
    
            @Override
            public boolean transport() {
                return true;
            }
        }

    protected Map<String, IOrderDestination> createTestDestinations() {
        Map<String, IOrderDestination> destinations = new HashMap<String, IOrderDestination>();
        destinations.put("TEST", new IOrderDestination() {
    
            private boolean forcedToTicket;
            
            @Override
            public ITransportableOrder createCancelOrder(CancelRequest cancelRequest) {
                return new TestTransportableOrder();
            }
    
            @Override
            public ITransportableOrder createOrder(Order order) {
                order.setAccount("TEST");
                if (isForceToTicket()) {
                    order.setExchange("TICKET");
                } else {
                    order.setExchange("DMA");
                }
                return new TestTransportableOrder();
            }
    
            @Override
            public ITransportableOrder createReplaceOrder(CancelReplaceRequest crr) {
                return new TestTransportableOrder();
            }
    
            @Override
            public String getDestinationName() {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
                return null;
            }
    
            @Override
            public boolean isActiveSession() {
                return true;
            }
    
            @Override
            public void start() {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
    
            }
    
            @Override
            public void stop() {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
    
            }
    
            @Override
            public List<String> validateOrder(Order order) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
                return null;
            }
    
            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
    
            }
    
            @Override
            public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
    
            }
    
            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
    
            }
    
            @Override
            public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
    
            }
    
            @Override
            public boolean isForceToTicket() {
                return forcedToTicket;
            }
    
            @Override
            public void setForceToTicket(boolean forceToTicket) {
                forcedToTicket = forceToTicket;
            }
    
        });
    
        return destinations;
    }

    protected DatabaseMapper createDatabaseMapper() {
        DatabaseMapper dbm = new DatabaseMapper(true);
        dbm.addAccountMapping("TEST", "TEST.EQUITY", "EQUITY", "TEST-ACCOUNT");
        return dbm;
    }

}
