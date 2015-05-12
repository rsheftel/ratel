package malbec.pomsfa.fix;

import static malbec.util.FerretIntegration.createEmailer;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import malbec.fer.dao.FixFillDao;
import malbec.fix.FixClient;
import malbec.fix.FixClientApplication;
import malbec.fix.message.FixFill;
import malbec.fix.message.FixFillFactory;
import malbec.util.EmailSettings;

import org.joda.time.LocalDate;
import org.mule.extras.client.MuleClient;
import org.mule.extras.client.RemoteDispatcher;
import org.mule.umo.UMOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.Account;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.field.PossDupFlag;
import quickfix.field.PossResend;
import quickfix.field.SenderCompID;

import com.fftw.bloomberg.aggregator.ConversionStrategy;
import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.cmfp.dao.CmfTradeRecordDAO;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.util.Emailer;

/**
 * Default implementation for the FIX Clients within Pomsfa.
 * 
 * 
 */
public class FeedAggregatorFixClientApplication extends FixClientApplication
{
    final private Logger log = LoggerFactory.getLogger(getClass());

    private FixClient fixClient;

    private CmfTradeRecordDAO dao;

    private ConversionStrategy conversionStrategy;

    private Emailer emailer;

    private boolean useRemote = false;

    private boolean sendToProd = false;

    private String pomsDestinationUri = "vm://poms.outbound.queue";

    private FeedAggregatorFixClientApplication (EmailSettings emailSettings, Boolean requiresLogon,
        String userID, String password)
    {
        super(emailSettings, requiresLogon, userID, password);
    }

    public FeedAggregatorFixClientApplication (String name, Properties config,
        EmailSettings emailSettings, ConversionStrategy conversionStrategy)
    {
        this(emailSettings, false, null, null);
        this.conversionStrategy = conversionStrategy;
        fixClient = new FixClient(name, this, config);
        emailer = createEmailer(emailSettings);
        registerJmxBeans();
        
        if (conversionStrategy == null) {
            throw new NullPointerException("Unable to determine conversion strategy for " + getStrategyId());
        }
    }

    protected void setConversionStrategy(ConversionStrategy conversionStrategy) {
        this.conversionStrategy = conversionStrategy;
    }
    
    public void startClient ()
    {
        fixClient.start();
    }

    public void stopClient ()
    {
        fixClient.stop();
    }

    public void registerJmxBeans ()
    {
        // Register our MBeans
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try
        {
            ObjectName beanName = new ObjectName(fixClient.getClass().getName() + ":name="
                + fixClient.getSessionName());
            unregisterBeanIfRequired(mbs, beanName);
            mbs.registerMBean(fixClient, beanName);
        }
        catch (Exception e)
        {
            log.error("Unable to register MBean for " + fixClient.getSessionName(), e);
        }
    }

    /**
     * This may seem backwards, but the conversion is driven off the execution
     * report, so the sender/target comp ids are the reverse of the client
     * 
     * @return
     */
    public SenderCompID getStrategyId ()
    {
        return new SenderCompID(fixClient.getTargetCompId());
    }

    private void unregisterBeanIfRequired (MBeanServer mbs, ObjectName beanName)
        throws InstanceNotFoundException, MBeanRegistrationException
    {
        // Ensure we have not already registered, if so, re-register
        if (mbs.isRegistered(beanName))
        {
            log.warn("Previously registered MBean '" + beanName
                + "' being re-registered, replacing instance");
            mbs.unregisterMBean(beanName);
        }
    }

    public FixClient getFixClient ()
    {
        return fixClient;
    }

    public void onMessage (quickfix.fix44.ExecutionReport message, SessionID sessionID)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue
    {
        onExecutionReport((Message)message, sessionID);
    }

    public void onMessage (quickfix.fix42.ExecutionReport message, SessionID sessionID)
        throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue
    {
        onExecutionReport((Message)message, sessionID);
    }

    public CmfMessage onExecutionReport (Message message, SessionID sessionID) throws FieldNotFound,
        UnsupportedMessageType, IncorrectTagValue
    {
        if (isValidExecution(message))
        {
            persistFixFill(message);

            if (!alreadySent(getPlatform(), message))
            {
                CmfMessage cmfMessage = convertMessage(message, emailer);
                if (cmfMessage != null)
                {
                    dispatchMessage(cmfMessage);
                    return cmfMessage;
                }
                else
                {
                    // if the record is null, we already processed or do not
                    // want to process
                    log.info("Already processed or unable to process execution:" + message);
                }
            }
        } else {
            log.warn("Ignoring report: " + message);
        }
        
        return null;
    }

    private CmfMessage convertMessage (Message message, Emailer emailer) throws FieldNotFound
    {
        try {
            if (message instanceof quickfix.fix44.ExecutionReport)
            {
                return conversionStrategy.convertMessage((quickfix.fix44.ExecutionReport)message,
                    emailer);
            }
            else if (message instanceof quickfix.fix42.ExecutionReport)
            {
                return conversionStrategy.convertMessage((quickfix.fix42.ExecutionReport)message,
                    emailer);
            }

        } catch (IllegalArgumentException e) {
            log.error("Unable to process FIX message", e);
        }
        return null;
    }

    private void dispatchMessage (CmfMessage cmfMessage)
    {
        try
        {
            MuleClient client = new MuleClient();
            if (useRemote)
            {
                if (sendToProd)
                {
                    RemoteDispatcher dispatcher = client.getRemoteDispatcher("tcp://nysrv56:60504");
                    dispatcher.sendRemote(pomsDestinationUri, cmfMessage.getWireMessage(), null);
                    // dispatcher.sendRemote(destinationUri, cmfMessage, null);
                }
                else
                {
                    RemoteDispatcher dispatcher = client
                        .getRemoteDispatcher("tcp://localhost:60504");
                    dispatcher.sendRemote(pomsDestinationUri, cmfMessage, null);
                }
            }
            else
            {
                // Send as a string so we don't have to deal with serialization
                // - this is missing an execution id, so we cannot use it for
                // anything other than testing
                // client.send(destinationUri,cmfMessage.getWireMessage(),
                // null);

                // Sent as an CmfRecord
                client.send(pomsDestinationUri, cmfMessage, null);
                // send to ActiveMQ as well

            }
        }
        catch (UMOException e)
        {
            log.error("Could not send message to POMS sender", e);
            emailer.emailErrorMessage("Unable to send trade.  Trade not sent to POMS.", cmfMessage
                .getTradeRecord(), true);
        }
    }

    protected TradingPlatform getPlatform ()
    {
        // TODO When we convert/add more FIX clients this
        // needs to be changed
        return TradingPlatform.TradingScreen;
    }

    private void persistFixFill (Message message)
    {
        // Record the execution as it comes in
        FixFill fill = FixFillFactory.valueOf(message);
        if (insertFixFill(fill) == -1)
        {
            if (fill.isPossibleDuplicate())
            {
                log.warn("Duplicate execution report: " + message);
            }
            else
            {
                log.error("Unable to convert FIX ExecutionReport: " + message);
            }
        }
    }

    protected boolean alreadySent (TradingPlatform platform, Message message) throws FieldNotFound
    {
        if (message.getHeader().isSetField(PossDupFlag.FIELD)
            || message.getHeader().isSetField(PossResend.FIELD))
        {
            try
            {
                CmfTradeRecord dbTradeRecord = dao.findByExecId(new LocalDate(), platform
                    .toString(), message.getString(ExecID.FIELD));

                if (dbTradeRecord != null
                    && (dbTradeRecord.getAcceptRejectTimestamp() != null || dbTradeRecord
                        .getReceiptTimestamp() != null))
                {
                    // we sent this item and it was received/accepted/rejected
                    log.info("Received possDup FIX message that was dup.  Not resending. "
                        + dbTradeRecord.toString());
                    return true;
                }
            }
            catch (SQLException e)
            {
                // this will not happen as we are using Spring
            }
        }
        return false;
    }

    protected boolean isValidExecution (Message message)
    {
        try
        {
            ExecType execType = new ExecType(message.getChar(ExecType.FIELD));
            OrdStatus orderStatus = new OrdStatus(message.getChar(OrdStatus.FIELD));
            // If this is from EMSX only accept account #20000073
            String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
            if ("BLPDROP".equalsIgnoreCase(senderCompId)) {
                String account = message.getString(Account.FIELD);
                if (!"20000073".equalsIgnoreCase(account)) {
                    return false;
                }
            }
            // All of the sources are sending Fills and partial fills
            return ((execType.getValue() == ExecType.FILL || execType.getValue() == ExecType.PARTIAL_FILL) && orderStatus
                .getValue() != OrdStatus.NEW);
        }
        catch (FieldNotFound e)
        {
            log.error("Unable to determine execution type", e);
        }

        return false;
    }

    /**
     * Copied from old logic
     * 
     * @param fill
     * @return
     */
    private long insertFixFill (FixFill fill)
    {
        if (fill == null || fill.isOrderAck())
        {
            log.warn("Skipping execution report: " + fill);
            return -1;
        }

        FixFillDao dao = FixFillDao.getInstance();

        if (fill.isPossibleDuplicate())
        {
            FixFill dbFill = dao.findById(fill.getBeginString(), fill.getSenderCompId(), fill
                .getSenderSubId(), fill.getTradeDate(), fill.getExecutionId());

            if (dbFill != null)
            {
                return -1;
            }
        }

        return dao.persist(fill);
    }

    public String getPomsDestinationUri ()
    {
        return pomsDestinationUri;
    }

    public void setPomsDestinationUri (String pomsDestinationUri)
    {
        this.pomsDestinationUri = pomsDestinationUri;
    }

    public void setCmfTradeRecordDao (CmfTradeRecordDAO dao)
    {
        this.dao = dao;
    }
}
