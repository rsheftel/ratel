package malbec.pomsfa.fix;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import malbec.fix.FixClient;
import malbec.fix.util.Slf4jLogFactory;
import malbec.util.EmailSettings;
import malbec.util.IWaitFor;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import quickfix.Acceptor;
import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.field.SenderCompID;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.aggregator.ConversionStrategy;
import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.util.Emailer;

public class FeedAggregatorFixClientApplicationTest extends AbstractFixTest
{
    private static final class TestConversionStrategy implements ConversionStrategy
    {
        @Override
        public CmfMessage convertMessage (ExecutionReport message, Emailer mailer)
            throws FieldNotFound
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public CmfMessage convertMessage (quickfix.fix44.ExecutionReport message,
            Emailer mailer) throws FieldNotFound
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public TradingPlatform getPlatform ()
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    protected SocketAcceptor acceptor;

    @BeforeMethod(groups =
    {
        "unittest"
    })
    public void startQuickFix () throws Exception
    {
        System.out.println("Starting fix server");
        // create a QuickFix/J acceptor that we can connect to for the tests
        SessionSettings qfjSettings = createAcceptorSessionSettings();

        Application app = new Application()
        {

            @Override
            public void fromAdmin (Message arg0, SessionID arg1) throws FieldNotFound,
                IncorrectDataFormat, IncorrectTagValue, RejectLogon
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void fromApp (Message arg0, SessionID arg1) throws FieldNotFound,
                IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onCreate (SessionID arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onLogon (SessionID arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onLogout (SessionID arg0)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void toAdmin (Message arg0, SessionID arg1)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void toApp (Message arg0, SessionID arg1) throws DoNotSend
            {
                // TODO Auto-generated method stub

            }
        };

        MessageStoreFactory messageStoreFactory = new MemoryStoreFactory();
        LogFactory logFactory = new Slf4jLogFactory(qfjSettings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        acceptor = new SocketAcceptor(app, messageStoreFactory, qfjSettings, logFactory,
            messageFactory);

        // When running as 'unittest' this needs to be commented out
        acceptor.start();
        sleep(1000);
        SessionID sessionId = createAcceptorSessionId();
        System.out.println("Server is started - listening to port:"
            + qfjSettings.getString(sessionId, Acceptor.SETTING_SOCKET_ACCEPT_PORT));

    }

    @AfterMethod(groups =
    {
        "unittest"
    })
    public void stopQuickFix ()
    {
        System.out.println("Stopping FIX Server");
        acceptor.stop();
        waitForValue(new IWaitFor<Boolean>()
        {

            @Override
            public Boolean waitFor ()
            {
                return acceptor.isLoggedOn();
            }

        }, false, MAX_LOGON_WAIT);

        System.out.println("FIX Server stopped");
    }

    @Test(groups =
    {
        "unittest"
    })
    public void testClientStartup () throws Exception
    {
        FeedAggregatorFixClientApplication clientApp = new FeedAggregatorFixClientApplication(
            "TestClient", createInitiatorSession(), new EmailSettings(), new TestConversionStrategy()){

                @Override
                public SenderCompID getStrategyId ()
                {
                    return new SenderCompID("REDIRPT");
                }
            
        };

        FixClient fixClient = clientApp.getFixClient();
        assertFalse(fixClient.isLoggedOn());
        clientApp.startClient();
        waitForLogon(fixClient);
        assertTrue(fixClient.isLoggedOn());
     
        clientApp.stopClient();
        waitForLogoff(fixClient);
        assertFalse(fixClient.isLoggedOn());
    }

}
