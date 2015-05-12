package malbec.fer;

import java.util.List;

import malbec.fix.IPropertyChangeSupport;

public interface IOrderDestination extends IPropertyChangeSupport {

    String getDestinationName();
    
    /**
     * Validate the order.  
     * 
     * Returns all of the errors that prevent this order from being processed by this destination.
     * 
     * @param order
     * @return
     */
    List<String> validateOrder(Order order);
    
    /**
     * Create an order suitable to be transported to this destination.
     * 
     * @param order
     * @return
     */
    ITransportableOrder createOrder(Order order);
    
    void start();
    
    void stop();
}
