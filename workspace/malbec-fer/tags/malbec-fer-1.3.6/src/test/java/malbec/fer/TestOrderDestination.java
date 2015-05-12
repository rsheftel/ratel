/**
 * 
 */
package malbec.fer;

import java.beans.PropertyChangeListener;
import java.util.List;

class TestOrderDestination implements IOrderDestination {
    private boolean forcedToTicket;
    
    @Override
    public ITransportableOrder createCancelOrder(CancelRequest cancelRequest) {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override
    public ITransportableOrder createOrder(Order order) {
        TestTransportableOrder tto = new TestTransportableOrder();

        if (order.getPlatform() == null && order.getStrategy() == null && order.getSecurityType() == null) {
            tto.addError("Unable to determine account");
        } else {
            order.setAccount(order.getPlatform() + "-" + order.getStrategy() + "-" + order.getSecurityType());
        }
        if (isForceToTicket()) {
            order.setExchange("TICKET");
        }
        return tto;
    }

    @Override
    public ITransportableOrder createReplaceOrder(CancelReplaceRequest cancelReplaceRequest) {
        if (true) {
            throw new UnsupportedOperationException("Implement me!");
        }
        return null;
    }

    @Override
    public String getDestinationName() {
        return "UNIT-TEST";
    }

    @Override
    public boolean isActiveSession() {
        // we are always active
        return true;
    }

    @Override
    public boolean isForceToTicket() {
        return forcedToTicket;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
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
        System.err.println("Ignoring PropertyChangeListener: " + listener.getClass().getName());
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
    public void setForceToTicket(boolean forceToTicket) {
        forcedToTicket = forceToTicket;
    }
}