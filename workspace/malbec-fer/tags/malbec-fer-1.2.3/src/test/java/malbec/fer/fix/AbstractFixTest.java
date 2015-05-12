package malbec.fer.fix;

import java.util.Properties;

import malbec.AbstractBaseTest;
import malbec.fix.ClientSchedule;
import malbec.fix.FixClient;
import malbec.fix.util.QfjHelper;
import malbec.fix.util.Slf4jLogFactory;
import malbec.util.IWaitFor;
import malbec.util.ScheduleUnit;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import quickfix.Acceptor;
import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.Session;
import quickfix.SessionFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

public abstract class AbstractFixTest extends AbstractBaseTest {

    protected static final String UNIT_TEST_CLIENT = "UNIT_TEST_CLIENT";
    protected static final String UNIT_TEST_SERVER = "UNIT_TEST_SERVER";
    protected static final long MAX_LOGON_WAIT = 10000;
    protected static final long MAX_WAIT_TIME = 1000;

    protected SocketAcceptor acceptor;

    @BeforeMethod(groups = { "usefix", "unittest" })
    public void startQuickFix() throws Exception {
        System.out.println("Starting fix server");
        // create a QuickFix/J acceptor that we can connect to for the tests
        SessionSettings qfjSettings = createAcceptorSessionSettings();

        Application app = new TestServerApplication();

        MessageStoreFactory messageStoreFactory = new MemoryStoreFactory();
        LogFactory logFactory = new Slf4jLogFactory(qfjSettings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        acceptor = new SocketAcceptor(app, messageStoreFactory, qfjSettings, logFactory, messageFactory);

//        acceptor.start();
        sleep(1000);
        System.out.println("Server is started");
    }

    @AfterMethod(groups = { "usefix", "unittest" })
    public void stopQuickFix() {
        System.out.println("Stopping FIX Server");
        acceptor.stop();
        waitForValue(new IWaitFor<Boolean>() {

            @Override
            public Boolean waitFor() {
                return acceptor.isLoggedOn();
            }
            
        }, false, MAX_LOGON_WAIT);
        
//        sleep(2000);
        System.out.println("FIX Server stopped");
    }

    /**
     * Create QuickFix/J session settings for testing
     * 
     * @return
     */
    protected SessionSettings createAcceptorSessionSettings() {
        Properties props = new Properties();
        // create a section for our test acceptor
        // Setup the session
        // FIX version
        props.setProperty(SessionSettings.BEGINSTRING, "FIX.4.4");
        // comp IDs
        props.setProperty(SessionSettings.SENDERCOMPID, UNIT_TEST_SERVER);
        props.setProperty(SessionSettings.TARGETCOMPID, UNIT_TEST_CLIENT);
        // start/end time
        props.setProperty(Session.SETTING_START_TIME, "00:00:00");
        props.setProperty(Session.SETTING_END_TIME, "00:00:00");

        // port - we are accepting connections
        // props.setProperty(Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS, "localhost");
        props.setProperty(Acceptor.SETTING_SOCKET_ACCEPT_PORT, "9001");

        // Connection type
        props.setProperty(SessionFactory.SETTING_CONNECTION_TYPE, "acceptor");

        return QfjHelper.createSessionSettings(props);
    }

    /**
     * This currently only supports FIX 4.4.  If we need other versions this needs to be
     * updated.
     * 
     * @return
     */
    public SessionSettings createExecutorSessionSettings() {
        Properties props = new Properties();
        // create a section for our test acceptor
        // Setup the session
        // FIX version
        props.setProperty(SessionSettings.BEGINSTRING, "FIX.4.4");
        // comp IDs
        props.setProperty(SessionSettings.SENDERCOMPID, "*");
        props.setProperty(SessionSettings.TARGETCOMPID, "*");
        // start/end time
        props.setProperty(Session.SETTING_START_TIME, "00:00:00");
        props.setProperty(Session.SETTING_END_TIME, "00:00:00");

        // port - we are accepting connections
        props.setProperty(Acceptor.SETTING_SOCKET_ACCEPT_PORT, "9001");

        // Connection type
        props.setProperty(SessionFactory.SETTING_CONNECTION_TYPE, "acceptor");
        
        props.setProperty(Session.SETTING_RESET_ON_LOGON, "Y");
        props.setProperty(Session.SETTING_USE_DATA_DICTIONARY, "Y");

        props.setProperty("FileStorePath", "target/data/executor");
        // Application specific settings
        props.setProperty("ValidOrderTypes", "1,2,3,4");
        props.setProperty("DefaultMarketPrice", "15");
        
        return QfjHelper.createSessionSettings(props);
    }

    protected Properties createInitiatorScheduledSessionWeekend() {
        Properties props = createInitiatorScheduledSession();
        props.setProperty(Session.SETTING_START_DAY, "Saturday");
        props.setProperty(Session.SETTING_END_DAY, "Sunday");

        return props;
    }

    protected Properties createInitiatorScheduledSession() {
        // Get the default values and then over-ride the schedule
        Properties props = createInitiatorSession("FIX.4.4");
        // 5:30 AM - 8:30 PM
        props.setProperty(Session.SETTING_START_TIME, "05:30:00");
        props.setProperty(Session.SETTING_END_TIME, "20:30:00");
        props.setProperty(ClientSchedule.Config.SCHEDULE_TYPE.toString(), ScheduleUnit.WEEKLY.toString());

        return props;
    }

    protected Properties createInitiatorClientScheduledSession() {
        // Get the default values and then over-ride the schedule
        Properties props = createInitiatorSession("FIX.4.4");
        // 5:30 AM - 8:30 PM
        props.setProperty(Session.SETTING_START_TIME, "05:30:00");
        props.setProperty(Session.SETTING_END_TIME, "20:30:00");

        
        props.setProperty(FixClient.CLIENT_START_TIME, "18:00:00");
        props.setProperty(FixClient.CLIENT_END_TIME, "20:45:00");

        props.setProperty(FixClient.CLIENT_START_DAY, "Sunday");
        props.setProperty(FixClient.CLIENT_END_DAY, "Friday");

        props.setProperty(ClientSchedule.Config.SCHEDULE_TYPE.toString(), ScheduleUnit.WEEKLY.toString());

        return props;
    }
    
    public static Properties createInitiatorSession() {
        return createInitiatorSession("FIX.4.4");
    }
    
    /**
     * Create a FIX Initiator Session config for testing.
     * 
     * This is part of the application we are building so it does not follow the QuickFIX/J
     * <code>SessionSettings</code>.
     * 
     * @return
     */
    public static Properties createInitiatorSession(String beginString) {
        Properties props = new Properties();
        // create a section for our test acceptor
        // Setup the session
        // FIX version
        props.setProperty(SessionSettings.BEGINSTRING, beginString);
        // comp IDs
        props.setProperty(SessionSettings.TARGETCOMPID, UNIT_TEST_SERVER);
        props.setProperty(SessionSettings.SENDERCOMPID, UNIT_TEST_CLIENT);
        // start/end time
        props.setProperty(Session.SETTING_START_TIME, "00:00:00");
        props.setProperty(Session.SETTING_END_TIME, "00:00:00");

        props.setProperty(Session.SETTING_TIMEZONE, "US/Eastern");
        // Connection host:port
        props.setProperty(Initiator.SETTING_SOCKET_CONNECT_HOST, "localhost");
        props.setProperty(Initiator.SETTING_SOCKET_CONNECT_PORT, "9001");
        // Connection type
        props.setProperty(SessionFactory.SETTING_CONNECTION_TYPE, "initiator");

        props.setProperty(Session.SETTING_HEARTBTINT, "30");

        return props;
    }

    public void waitForLogon(final FixClient fc) {
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return fc.isLoggedOn();
            }
        }, true, MAX_LOGON_WAIT);
    }

    public void waitForLogoff(final FixClient fc) {
        waitForValue(new IWaitFor<Boolean>() {
            @Override
            public Boolean waitFor() {
                return fc.isLoggedOn();
            }
        }, false, MAX_LOGON_WAIT);
    }

}
