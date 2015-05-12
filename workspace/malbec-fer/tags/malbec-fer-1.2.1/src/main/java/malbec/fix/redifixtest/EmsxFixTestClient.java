package malbec.fix.redifixtest;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import malbec.fer.fix.FerFixClientApplication;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;


public class EmsxFixTestClient {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        final FerFixClientApplication ta = new FerFixClientApplication(new EmailSettings());
        final FixClient fc = new FixClient("EmsxDropCopySession", ta, createInitiatorSession());
        // specify the logger to use (Log4j);
        fc.setLoggerConfig("Emsx");
      
        setupShutdownHook(fc);
        
        registerWithJmx(fc);
        fc.start();

        while (true) {
            Thread.sleep(20000);
        }
    }

    
    private static void registerWithJmx(FixClient fixClient) throws Exception {
        
        ObjectName beanName = new ObjectName(fixClient.getClass().getName() + ":name="
                + fixClient.getSessionName());
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        mbs.registerMBean(fixClient, beanName);
    }


    private static void setupShutdownHook(final FixClient fc) {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run ()
            {
                if (fc != null) {
                    System.out.println("FIX Client "+ fc.getSessionName() + " Terminating!");
                    fc.stop();
                }
            }
        });
    }
    
    public static Properties createInitiatorSession() throws IOException {
        Properties props = new Properties();
        
        props.load(EmsxFixTestClient.class.getClassLoader().getResourceAsStream("emsxtestclient.properties"));

        return props;
    }

}
