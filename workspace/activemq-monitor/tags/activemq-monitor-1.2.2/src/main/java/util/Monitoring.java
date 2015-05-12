package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class to handle waiting for things to occur.
 * 
 */
public class Monitoring {

    static final Logger log = LoggerFactory.getLogger(Monitoring.class.getName());
    
    private Monitoring() { }
    
    
    public static void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            log.debug("Interrupted sleep", e);
        }
    }
}
