package malbec.fer;

/**
 * Provide a JMX interface
 */
public interface OrderDestinationMBean {

    boolean isForceToTicket();
    
    void setForceToTicket(boolean forceToTicket);
}
