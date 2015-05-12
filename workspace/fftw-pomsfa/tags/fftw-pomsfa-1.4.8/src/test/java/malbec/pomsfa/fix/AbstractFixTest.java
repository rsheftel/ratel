package malbec.pomsfa.fix;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import malbec.AbstractBaseTest;
import malbec.fix.FixClient;
import malbec.fix.util.QfjHelper;
import malbec.util.IWaitFor;

import org.apache.mina.filter.codec.ProtocolCodecException;

import quickfix.Acceptor;
import quickfix.DefaultMessageFactory;
import quickfix.Initiator;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.mina.message.FIXMessageDecoder;
import quickfix.mina.message.FIXMessageDecoder.MessageListener;

public class AbstractFixTest extends AbstractBaseTest
{
    protected static final String UNIT_TEST_CLIENT = "UNIT_TEST_CLIENT";

    protected static final String UNIT_TEST_SERVER = "UNIT_TEST_SERVER";

    protected static final long MAX_LOGON_WAIT = 59000;

    protected static final long MAX_WAIT_TIME = 1000;

    /**
     * Create QuickFix/J session settings for testing
     * 
     * @return
     */
    protected SessionSettings createAcceptorSessionSettings ()
    {
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
        props.setProperty(Session.SETTING_END_TIME, "23:59:59");

        // port - we are accepting connections
        // props.setProperty(Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS,
        // "localhost");
        props.setProperty(Acceptor.SETTING_SOCKET_ACCEPT_PORT, "9001");

        // Connection type
        props.setProperty(SessionFactory.SETTING_CONNECTION_TYPE, "acceptor");

        return QfjHelper.createSessionSettings(props);
    }

    protected SessionID createAcceptorSessionId ()
    {
        return new SessionID("FIX.4.4", UNIT_TEST_SERVER, UNIT_TEST_CLIENT);
    }

    public static Properties createInitiatorSession ()
    {
        return createInitiatorSession("FIX.4.4");
    }

    /**
     * Create a FIX Initiator Session config for testing.
     * 
     * This is part of the application we are building so it does not follow the
     * QuickFIX/J <code>SessionSettings</code>.
     * 
     * @return
     */
    public static Properties createInitiatorSession (String beginString)
    {
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
        // props.setProperty(Session.SETTING_END_TIME, "23:59:59");
        props.setProperty(Session.SETTING_END_TIME, "00:00:00");

        props.setProperty(Session.SETTING_TIMEZONE, "US/Eastern");
        // Connection host:port
        props.setProperty(Initiator.SETTING_SOCKET_CONNECT_HOST, "localhost");
        props.setProperty(Initiator.SETTING_SOCKET_CONNECT_PORT, "9001");
        // Connection type
        props.setProperty(SessionFactory.SETTING_CONNECTION_TYPE, "initiator");

        props.setProperty(Session.SETTING_HEARTBTINT, "30");

        props.setProperty("UserID", "TestUser");

        // For developing on the weekend
        // props.setProperty(Session.SETTING_START_DAY, "Monday");
        // props.setProperty(Session.SETTING_END_DAY, "Sunday");

        return props;
    }

    public void waitForLogon (final FixClient fixClient)
    {
        waitForValue(new IWaitFor<Boolean>()
        {
            @Override
            public Boolean waitFor ()
            {
                return fixClient.isLoggedOn();
            }
        }, true, MAX_LOGON_WAIT);
    }
    
    
    public void waitForLogoff (final FixClient fixClient)
    {
        waitForValue(new IWaitFor<Boolean>()
        {
            @Override
            public Boolean waitFor ()
            {
                return !fixClient.isLoggedOn();
            }
        }, true, MAX_LOGON_WAIT);
    }

    protected List<String> readMessagesFromFile (String fileName)
        throws UnsupportedEncodingException, IOException, ProtocolCodecException,
        URISyntaxException
    {
        FIXMessageDecoder fmd = new FIXMessageDecoder();

        URL url = fmd.getClass().getClassLoader().getResource(fileName);
        List<String> fileMessages = fmd.extractMessages(new File(url.toURI()));
        return fileMessages;
    }

    protected List<Message> readFixMessagesFromFile (String fileName)
        throws UnsupportedEncodingException, IOException, ProtocolCodecException,
        URISyntaxException
    {
        FIXMessageDecoder fmd = new FIXMessageDecoder();

        URL url = fmd.getClass().getClassLoader().getResource(fileName);
        final List<Message> fileMessages = new ArrayList<Message>();
        
        fmd.extractMessages(new File(url.toURI()), new MessageListener() {
            DefaultMessageFactory dmf = new DefaultMessageFactory();
            
            @Override
            public void onMessage (String message)
            {
                try
                {
                    Message fixMessage = MessageUtils.parse(dmf, null, message);
                    fileMessages.add(fixMessage);
                }
                catch (InvalidMessage e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                
            }});
        
        return fileMessages;
    }
}
