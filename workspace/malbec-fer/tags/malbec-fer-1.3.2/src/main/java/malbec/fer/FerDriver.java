package malbec.fer;

import java.io.File;

import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.StringUtils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class FerDriver {

    private static EmailSettings emailSettings;

    private static FerretRouter ferRouter;

    private static BeanFactory getBeanFactory() {
        XmlBeanFactory parentFactory = new XmlBeanFactory(new ClassPathResource("ferretschedule.xml"));
        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("fercontext.xml"), parentFactory);

        return factory;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        setupShutdownHook();
        setupJacob();
        BeanFactory factory = getBeanFactory();
        emailSettings = (EmailSettings) factory.getBean("GlobalEmailSettings");
        setupUncaughtExceptionHandler();

        // Start the router
        ferRouter = (FerretRouter) factory.getBean("FerRouter");
        runLoop();
    }

    private static void setupJacob() {
        // System.setProperty("java.library.path", new File(".").getAbsolutePath());
        System.out.println(new File(".").getAbsolutePath());
    }

    private static void runLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void setupUncaughtExceptionHandler() {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();

                EmailSender sender = new EmailSender(emailSettings.getAsProperties());
                String exceptionStr = StringUtils.exceptionToString(e);
                StringBuilder sb = new StringBuilder(exceptionStr.length() * 2);
                sb.append("Received an uncaught exception on ").append(t.getName()).append(".\n\n");
                sb.append(exceptionStr);

                sender.sendMessage("FER Uncaught Exception", sb.toString());
            }
        });
    }

    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("FER Terminating!");
                if (ferRouter != null) {
                    ferRouter.stop();
                }
            }
        });
    }

}
