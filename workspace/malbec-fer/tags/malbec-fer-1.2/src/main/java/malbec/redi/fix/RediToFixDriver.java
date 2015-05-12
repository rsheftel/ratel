package malbec.redi.fix;

import malbec.util.EmailSender;
import malbec.util.EmailSettings;
import malbec.util.StringUtils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class RediToFixDriver
{

    private static RediToFix rediToFix;

    /**
     * @param args
     */
    public static void main (String[] args)
    {
        setupShutdownHook();

        BeanFactory factory = getBeanFactory();
        EmailSettings emailSettings = (EmailSettings)factory.getBean("GlobalEmailSettings");
        setupUncaughtExceptionHandler(emailSettings);

        // Start the process
        rediToFix = (RediToFix)factory.getBean("RediToFix");
        runLoop();
    }

    private static void runLoop ()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static BeanFactory getBeanFactory ()
    {
        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("redicontext.xml"));

        return factory;
    }

    private static void setupShutdownHook ()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run ()
            {
                System.out.println("RediToFix Terminating!");
                if (rediToFix != null)
                {
                    rediToFix.stop();
                }
            }
        });
    }

    private static void setupUncaughtExceptionHandler (final EmailSettings emailSettings)
    {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {

            @Override
            public void uncaughtException (Thread t, Throwable e)
            {
                e.printStackTrace();

                EmailSender sender = new EmailSender(emailSettings.getAsProperties());
                String exceptionStr = StringUtils.exceptionToString(e);
                StringBuilder sb = new StringBuilder(exceptionStr.length() * 2);
                sb.append("Received an uncaught exception on ").append(t.getName()).append(".\n\n");
                sb.append(exceptionStr);

                sender.sendMessage("Redi Uncaught Exception", sb.toString());
            }
        });
    }
}
