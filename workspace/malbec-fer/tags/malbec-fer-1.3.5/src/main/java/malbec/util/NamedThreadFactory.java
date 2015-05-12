package malbec.util;

import java.util.concurrent.ThreadFactory;

/**
 * Provide threads that have meaningful names.
 * 
 */
public class NamedThreadFactory implements ThreadFactory {

    private final String threadName;
    
    public NamedThreadFactory(String threadName) {
        this.threadName = threadName;
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, threadName);
        thread.setDaemon(true);

        return thread;
    }

}
