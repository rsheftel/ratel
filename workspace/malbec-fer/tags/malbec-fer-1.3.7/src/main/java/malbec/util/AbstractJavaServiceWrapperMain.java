package malbec.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Base class for Java applications that are to be run as Windows services via the Java Service Wrapper.
 */
public abstract class AbstractJavaServiceWrapperMain {

    final private Logger log = LoggerFactory.getLogger(getClass());
    
    private BeanFactory factory;

    protected AbstractJavaServiceWrapperMain() {
        setupShutdownHook(this);
        setupUncaughtExceptionHandler(this);
    }

    protected void loadBeanFactory(String filename) {
        factory = getBeanFactory(filename);
    }

    protected BeanFactory getBeanFactory() {
        return factory;
    }

    protected BeanFactory getBeanFactory(String filename) {
        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(filename));

        return factory;
    }

    protected void runLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Strange Error in runLoop", e);
            }
        }
    }
    
    protected void setupUncaughtExceptionHandler(final AbstractJavaServiceWrapperMain uncaughtExceptionHandler) {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                uncaughtExceptionHandler.uncaughtException(t, e);
            }
        });
    }

    protected void setupShutdownHook(final AbstractJavaServiceWrapperMain shutdownHook) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdownHook.systemShutdownHandler();
            }
        });
    }

    protected abstract void uncaughtException(Thread t, Throwable e);

    protected abstract void systemShutdownHandler();
}
