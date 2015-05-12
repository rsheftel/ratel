package malbec.fer.jms;

import javax.jms.Message;

/**
 * Interface for classes that will be treated as applications that pub/sub to JMS destinations.
 */
public interface IJmsApplication {

    /**
     * Messages that the application is receiving
     * @param message
     */
    void inboundApp(Message message);
    
    /**
     * Messages that your application is sending.
     * 
     * @param message
     */
    void outboundApp(Message message);
    
    /**
     * 
     * @return
     */
    boolean filterConsumers();
    
    /**
     * 
     * @param subject
     * @param messageBody
     * @return
     */
    boolean sendEmail(String subject, String messageBody);
}
