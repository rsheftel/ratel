package malbec.fix;

/**
 * Provide an interface for JMX.
 * 
 */
public interface FixClientMBean {

    /**
     * Start the FixClient
     */
    void start();

    /**
     * Stop the FixClient
     */
    void stop();
    
    /**
     * Reset the sequence numbers for this client.
     */
    void resetSequenceNumbers();

    /**
     * The next Target sequence number.
     * 
     * 
     * @return next number or -1 if there is an error
     */
    int getTargetSequenceNumber();
    
    /**
     * The next sender sequence number.
     * 
     * @return next number of -1 if there is an error
     */
    int getSenderSequenceNumber();
    
    /**
     * Get the name associated with the FixClient
     * @return
     */
    String getSessionName();
    
    /**
     * Whether or not this client is currently logged on
     * 
     * @return
     */
    boolean isLoggedOn();
    
    /**
     * Return the SessionID as a string.
     * 
     * @return
     */
    String getSessionIDString();

    void setTargetSequenceNumber(int newSequenceNumber);
    
    void setSenderSequenceNumber(int newSequenceNumber);
}
