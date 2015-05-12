package malbec.util;

import java.util.concurrent.ThreadFactory;

/**
 * Provide threads for the <code>FixClient</code> that has a meaningful name.
 * 
 */
public class NamedThreadFactory implements ThreadFactory {

    private String threadName;
    
    private NamedThreadFactory() {}
    
    public NamedThreadFactory(String threadName) {
        this();
        this.threadName = threadName;
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, threadName);
        thread.setDaemon(true);

        return thread;
    }

}
