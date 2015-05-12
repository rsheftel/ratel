package com.fftw.bloomberg.aggregator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.management.JMException;

import org.mule.extras.spring.config.SpringConfigurationBuilder;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.LifecycleException;
import org.mule.umo.lifecycle.Startable;
import org.mule.umo.lifecycle.Stoppable;
import org.mule.umo.manager.UMOManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.SLF4JLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.ThreadedSocketInitiator;
import quickfix.field.BeginString;
import quickfix.field.MsgSeqNum;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfApplication;
import com.fftw.bloomberg.cmfp.CmfSessionSettings;
import com.fftw.bloomberg.cmfp.CmfSocketInitiator;

/**
 * Startup class for the Bloomberg Feed Aggregator (BFA).
 * 
 * 
 */
public class AggregatorDriver implements Startable, Stoppable
{
    private final static Logger log = LoggerFactory.getLogger(AggregatorDriver.class);

    private static final String[] CONFIG_FILES =
    {
        "appcontext.xml", "pomsContext.xml", "tradeStationContext.xml", // "activemq-spring.xml",
        "pomsfa-mule-spring-config.xml"
    // "pomsfa-mule-config.xml"
    };

    private final Application fixApplication;

    private final SessionSettings fixSettings;

    private final MessageStoreFactory messageStoreFactory;

    private final LogFactory logFactory;

    private final MessageFactory messageFactory;

    private final CmfApplication cmfApplication;

    private final CmfSessionSettings cmfSettings;

    private ThreadedSocketInitiator initiator;

    private CmfSocketInitiator cmfInitiator;

    private static UMOManager manager;

    public AggregatorDriver (Application fixApplication, SessionSettings fixSettings,
        CmfApplication cmfApplication, CmfSessionSettings cmfSettings) throws ConfigError,
        FieldConvertError, JMException
    {
        this.fixApplication = fixApplication;
        this.fixSettings = fixSettings;
        // Create the FIX connections
        messageStoreFactory = new FileStoreFactory(fixSettings);
        // Ensure that we use log4j
        logFactory = new SLF4JLogFactory(fixSettings);
        messageFactory = new DefaultMessageFactory();

        this.cmfApplication = cmfApplication;

        this.cmfSettings = cmfSettings;

        // register the shutdown hook

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run ()
            {
                stopApp();
            }
        });
    }

    /**
     * @param args
     */
    public static void main (String[] args) throws Exception
    {
        log.info("Starting " + AggregatorApplication.class.getName());

        // Start Spring and load the embedded broker
        BeanFactory factory = getBeanFactory();
        factory.getBean("broker");
        // Start Mule
        SpringConfigurationBuilder builder = new SpringConfigurationBuilder();
        manager = builder.configure(buildConfigFileString(CONFIG_FILES));

    }

    private static BeanFactory getBeanFactory ()
    {
        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("activemq-spring.xml"));

        return factory;
    }

    public void start () throws UMOException
    {
        try
        {
            // Create the one initiator (handles multiple connections)
            initiator = new ThreadedSocketInitiator(fixApplication, messageStoreFactory,
                fixSettings, logFactory, messageFactory);

            // Start the CMFP (POMS) connection
            cmfInitiator = new CmfSocketInitiator(cmfApplication, cmfSettings);

            startInitiators();
        }
        catch (Exception e)
        {
            throw new LifecycleException(e, this);
        }
    }

    /**
     * This is used by the JVM shutdown hook.
     */
    private void stopApp ()
    {
        try
        {
            log.warn("JVM shutting down");
            stop(); // stop the connections
            if (manager != null)
            {
                manager.stop(); // stop mule
            }
        }
        catch (UMOException e)
        {
            e.printStackTrace();
        }
    }

    public void stop () throws UMOException
    {
        try
        {
            stopInitiators();
        }
        catch (Exception e)
        {
            throw new LifecycleException(e, this);
        }
    }

    void startInitiators () throws RuntimeError, ConfigError
    {
        // Start poms first
        cmfInitiator.start();

        if (fixApplication instanceof AggregatorApplication)
        {
            AggregatorApplication aa = (AggregatorApplication)fixApplication;
            aa.startExecutionProcessor();
        }

        initiator.start();
    }

    private void stopInitiators ()
    {
        // We need to get all of the sessions before we stop them, otherwise we
        // cannot reset the sequence numbers.
        Map<Session, Integer> sessionsToReset = null;
        // stop our execution processing thread
        if (fixApplication instanceof AggregatorApplication)
        {
            AggregatorApplication aa = (AggregatorApplication)fixApplication;
            aa.stopExecutionProcessor();
            sessionsToReset = getSessionSequenceNumberOnShutdown(aa.getExecutionQueue());
        }

        initiator.stop();

        // we may not have processed all of the messages, update our sequence
        // numbers
        if (sessionsToReset != null)
        {
            updateSessionSequenceNumbersOnShutdown(sessionsToReset);
            log.info("Reset sequence numbers based on map: " + sessionsToReset);
        }

        cmfInitiator.stop();
    }

    private void updateSessionSequenceNumbersOnShutdown (Map<Session, Integer> sessionsToReset)
    {
        for (Map.Entry<Session, Integer> entry : sessionsToReset.entrySet())
        {
            try
            {
                entry.getKey().getStore().setNextTargetMsgSeqNum(entry.getValue());
            }
            catch (IOException e)
            {
                log.error("Failed to reset sequence number for session " + entry.getKey() + " to "
                    + entry.getValue());
            }
        }
    }

    private Map<Session, Integer> getSessionSequenceNumberOnShutdown (
        BlockingQueue<ExecutionReport> messagesToProcess)
    {
        Map<Session, Integer> sessions = new HashMap<Session, Integer>();

        quickfix.fix42.ExecutionReport message = null;

        log.info("Draining local queue to find sequence numbers for reset");
        while ((message = messagesToProcess.poll()) != null)
        {
            try
            {
                String beginString = message.getHeader().getString(BeginString.FIELD);
                String senderCompID = message.getHeader().getString(SenderCompID.FIELD);
                String targetCompID = message.getHeader().getString(TargetCompID.FIELD);

                // The session is the opposite of this message
                // SessionID sessionID = new SessionID(beginString,
                // senderCompID, targetCompID);
                SessionID sessionID = new SessionID(beginString, targetCompID, senderCompID);
                Session session = Session.lookupSession(sessionID);

                if (!sessions.containsKey(session))
                {
                    int sequenceNumber = message.getHeader().getInt(MsgSeqNum.FIELD);
                    sessions.put(session, sequenceNumber);
                }
            }
            catch (FieldNotFound e)
            {
                log.error("Unable to create SessionID from message: " + message);
            }
        }

        return sessions;
    }

    private static String buildConfigFileString (String[] configFiles)
    {
        StringBuilder sb = new StringBuilder(configFiles.length * 20);
        boolean first = true;
        for (String fileName : configFiles)
        {
            if (!first)
            {
                sb.append(",");
            }
            sb.append(fileName);
            first = false;
        }

        return sb.toString();
    }
}
