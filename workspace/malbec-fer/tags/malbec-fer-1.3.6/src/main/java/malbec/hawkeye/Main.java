package malbec.hawkeye;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import malbec.fer.Order;
import malbec.fer.dao.OrderDao;
import malbec.jms.DefaultTextMessageProcessor;
import malbec.jms.DestinationHandler;
import malbec.util.AbstractJavaServiceWrapperMain;
import malbec.util.DateTimeUtil;
import malbec.util.EmailAlertManager;
import malbec.util.EmailSettings;
import malbec.util.MessageUtil;
import malbec.util.StringUtils;
import malbec.util.TaskService;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import systemdb.metadata.LiveOrders;
import systemdb.metadata.LiveOrders.LiveOrder;
import util.Log;

/**
 * Main class for the IronHawk monitor application.
 * 
 * The IronHawk monitors the interaction between Tomahawk and Ferret. This involves reading the Tomahawk's
 * orders table and the Ferret's FixOrder table and the Ferret's ActiveMQ order status topics.
 * 
 */
public class Main extends AbstractJavaServiceWrapperMain {

    static {
        TaskService.getInstance().createAndAddSingleThreadScheduled("HawkEyeMonitor");
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Connection brokerConnection;

    private DestinationHandler ferretOrderStatusHandler;
    private DestinationHandler ferretStateHandler;
    private String ferretState;

    private List<LiveOrder> previouslyExtraTomahawkOrders;
    private List<Order> previouslyExtraFerretOrders;

    private int tomahawkMaxId;
    private List<LiveOrder> lastTomahawkQuery;

    private long ferretMaxId;

    private List<Order> lastFerretQuery;

    private EmailAlertManager emailAlertManager;

    private HawkEyeConfiguration config;
    private boolean hadMoreTomahawkOrders = false;

    protected Main(HawkEyeConfiguration configuration) {
        loadBeanFactory("hawkeye.xml");
        EmailSettings emailSettings = (EmailSettings) getBeanFactory().getBean("GlobalEmailSettings");
        emailAlertManager = new EmailAlertManager(emailSettings);
        config = configuration;
    }

    protected Main() {
        loadBeanFactory("hawkeye.xml");
        EmailSettings emailSettings = (EmailSettings) getBeanFactory().getBean("GlobalEmailSettings");
        emailAlertManager = new EmailAlertManager(emailSettings);
        config = (HawkEyeConfiguration) getBeanFactory().getBean("HawkEyeConfig");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        final Main main = new Main();
        main.dailyInit();
        scheduleCompareTask(main);
        scheduleDailyReset(main);

        // start listening to the Ferret State topic
        try {
            main.startTopicHandlers();
        } catch (JMSException e) {
            main.log.error("Failed to start Broker handlers", e);
        }
        main.runLoop();
    }

    private String getFeretTopic() {
        StringBuilder sb = new StringBuilder();
        sb.append(config.getOrderStatusBase()).append(".>");

        return sb.toString();
    }

    public boolean isListeningToFerret() {
        return (ferretOrderStatusHandler != null && ferretOrderStatusHandler.isStarted());
    }

    /**
     * Start listening to the topics.
     * 
     */
    public void startTopicHandlers() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(config.getBrokerUrl());
        brokerConnection = factory.createConnection();
        brokerConnection.start();

        // Create the state listener
        ferretStateHandler = new DestinationHandler(brokerConnection, getFerretStateTopic(), true);
        ferretStateHandler.setTextMessageProcessor(new DefaultTextMessageProcessor() {

            @Override
            protected void onTextMessage(TextMessage textMessage, Map<String, String> mapMessage) {
                String ferretState = mapMessage.get("FERRETSTATE");
                log.info("Received Ferret state change: " + ferretState);
                if (ferretState != null) {
                    String oldState = getFerretState();
                    setFerretState(ferretState);
                    if (!ferretState.equalsIgnoreCase(oldState)) {
                        log.info("Changing Ferret state from: " + oldState + " to: " + ferretState);
                    }
                }
            }
        });
        ferretStateHandler.start();

        // Create the order status listener
        ferretOrderStatusHandler = new DestinationHandler(brokerConnection, getFeretTopic(), true);
        // Ignore anything before 12:00 AM of the current day
        DateTime today = new LocalDate().toDateTimeAtStartOfDay();
        ferretOrderStatusHandler.setTextMessageProcessor(new FerretStatusMessageProcessor(today) {
            @Override
            protected void validateStatusMessage(Map<String, String> mapMessage) {
                validateStatusMessageMain(mapMessage);
            }
        });
        ferretOrderStatusHandler.start();
    }

    protected boolean shouldFerretBeSending() {
        return "TICKET".equalsIgnoreCase(ferretState) || "DMA".equalsIgnoreCase(ferretState);
    }

    protected void setFerretState(String ferretState) {
        this.ferretState = ferretState;
    }

    private String getFerretStateTopic() {
        return config.getFerretStateTopic();
    }

    private static void scheduleDailyReset(final Main main) {
        int minutesBeforeFirst = 1440 - new DateTime().getMinuteOfDay() + 1;
        DateTime firstRoll = new DateTime().plusMinutes(minutesBeforeFirst);
        String firstRollString = DateTimeUtil.format(firstRoll);
        main.log.info("Scheduled daily roll-over " + minutesBeforeFirst + " minutes from now.  "
            + firstRollString);

        TaskService.getInstance().getExecutor("HawkEyeMonitor").scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                main.dailyInit();
            }
        }, minutesBeforeFirst, 1440, TimeUnit.MINUTES);
    }

    private static void scheduleCompareTask(final Main main) {
        TaskService.getInstance().getExecutor("HawkEyeMonitor").scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                main.compareTaskHandler();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    protected boolean validateStatusMessageMain(Map<String, String> mapMessage) {
        if (!shouldFerretBeSending()) {
            String status = MessageUtil.getStatus(mapMessage);
            if ("SENT".equalsIgnoreCase(status)) {
                log.error("Ferret in wrong state to be sending orders.");
                StringBuilder sb = new StringBuilder();
                sb.append("Ferret state: " + getFerretState()).append("\n");
                sb.append("Order message: ");
                sb.append(mapMessage);
                emailAlertManager.send("FerretProcessingOrdersError",
                    "Ferret Processed Order While in Wrong State", sb.toString());
                return false;
            } else if ("INVALID".equalsIgnoreCase(status)) {
                // These will not be in the Ferret database, add as much as we can to the
                // internal list
                Order invalidOrder = new Order(mapMessage);
                previouslyExtraFerretOrders.add(invalidOrder);
                log.warn("Invalid order received by the Ferret: " + mapMessage);
            }
        }
        return true;
    }

    public boolean ordersMatch(List<LiveOrder> tomahawkOrders, List<Order> ferretOrders) {
        // Add the previously miss matched orders here before the compare
        Map<String, LiveOrder> tomahawkOrdersToCompare = combinePreviousTomahawkWith(tomahawkOrders);
        Map<String, Order> ferretOrdersToCompare = combinePreviousFerretWith(ferretOrders);

        Map<String, List<String>> missMatched = CompareLogic.compareOrders(tomahawkOrdersToCompare.keySet(),
            ferretOrdersToCompare.keySet());

        updatePreviousTomahawk(tomahawkOrdersToCompare, missMatched);
        updatePrevisousFerret(ferretOrdersToCompare, missMatched);

        return missMatched.size() == 0;
    }

    private void updatePrevisousFerret(Map<String, Order> ferretOrdersToCompare,
        Map<String, List<String>> missMatched) {
        List<String> missingFromTomahawk = missMatched.get("TOMAHAWK");
        List<Order> newExtraFerretOrders = new ArrayList<Order>();

        if (missingFromTomahawk != null) {
            for (String userOrderId : missingFromTomahawk) {
                newExtraFerretOrders.add(ferretOrdersToCompare.get(userOrderId));
            }
        }
        previouslyExtraFerretOrders = newExtraFerretOrders;
    }

    private void updatePreviousTomahawk(Map<String, LiveOrder> tomahawkOrdersToCompare,
        Map<String, List<String>> missMatched) {
        List<String> missingFromFerret = missMatched.get("FERRET");
        List<LiveOrder> newExtraTomahawkOrders = new ArrayList<LiveOrder>();

        if (missingFromFerret != null) {
            for (String userOrderId : missingFromFerret) {
                newExtraTomahawkOrders.add(tomahawkOrdersToCompare.get(userOrderId));
            }
        }
        previouslyExtraTomahawkOrders = newExtraTomahawkOrders;
    }

    private Map<String, Order> combinePreviousFerretWith(List<Order> ferretOrders) {
        Map<String, Order> ferretOrdersToCompare = new TreeMap<String, Order>();

        // Add the previously unmatched orders
        for (Order lo : previouslyExtraFerretOrders) {
            ferretOrdersToCompare.put(lo.getUserOrderId(), lo);
        }
        // add the new orders to check
        for (Order lo : ferretOrders) {
            ferretOrdersToCompare.put(lo.getUserOrderId(), lo);
        }

        return ferretOrdersToCompare;
    }

    private Map<String, LiveOrder> combinePreviousTomahawkWith(Collection<LiveOrder> tomahawkOrders) {
        Map<String, LiveOrder> tomahawkOrdersToCompare = new TreeMap<String, LiveOrder>();

        // Add the previously unmatched orders
        for (LiveOrder lo : previouslyExtraTomahawkOrders) {
            if (lo.isFerret()) {
                tomahawkOrdersToCompare.put(lo.ferretOrderId(), lo);
            }
        }
        // add the new orders to check
        for (LiveOrder lo : tomahawkOrders) {
            if (lo.isFerret()) {
                tomahawkOrdersToCompare.put(lo.ferretOrderId(), lo);
            }
        }

        return tomahawkOrdersToCompare;
    }

    public List<Order> getExtraFerretOrders() {
        return previouslyExtraFerretOrders;
    }

    public List<LiveOrder> getExtraTomahawkOrders() {
        return previouslyExtraTomahawkOrders;
    }

    public synchronized void dailyInit() {
        log.info("Started dailyInit");
        Log.doNotDebugSqlForever();

        if (hadMoreTomahawkOrders) {
            StringBuilder sb = new StringBuilder(256);
            sb.append("Reseting out of sync flag and starting a new day.  ");
            sb.append("This does not mean that there are not errors!  ");
            sb.append("We are just not reporting them anymore.");
            
            emailAlertManager.send("DailyResetWithErrors",
                "Daily Reset - Tomahawk has orders not not known by Ferret", sb.toString());
            hadMoreTomahawkOrders = false;
        }
        
        previouslyExtraTomahawkOrders = new ArrayList<LiveOrder>();
        previouslyExtraFerretOrders = new ArrayList<Order>();

        // Access the singletons and load the max order before today
        LiveOrders lo = LiveOrders.ORDERS;
        tomahawkMaxId = lo.maxIdBeforeToday();

        OrderDao dao = OrderDao.getInstance();
        ferretMaxId = dao.maxIdBeforeToday();

        log.info("FerretMaxId=" + ferretMaxId);
        log.info("TomahawkMaxId=" + tomahawkMaxId);
    }

    public int getTomahawkMaxId() {
        return tomahawkMaxId;
    }

    public long getFerretMaxId() {
        return ferretMaxId;
    }

    public synchronized void compareTaskHandler() {
        // Tomahawk Orders
        List<LiveOrder> tomahawkOrders = queryTomahawk();

        // Ferret Orders
        List<Order> ferretOrders = queryFerret();
        
        compareAndReportMismatches(tomahawkOrders, ferretOrders);
    }

    public boolean compareAndReportMismatches(List<LiveOrder> tomahawkOrders, List<Order> ferretOrders) {
        boolean mismatchFound = false;
        
        if (!ordersMatch(tomahawkOrders, ferretOrders)) {
            DateTime now = new DateTime();
            log.error("Tomahawk and Ferret are out of sync!");
            int extraFerretCount = getExtraFerretOrders().size();
            int extraTomahawkCount = getExtraTomahawkOrders().size();

            if (extraFerretCount > 0) {
                mismatchFound = true;
                log.debug("Extra Ferret orders=" + extraFerretCount);
                StringBuilder sb = new StringBuilder(128 * extraFerretCount);
                List<Order> extraFerretOrders = getExtraFerretOrders();
                for (Order order : extraFerretOrders) {
                    sb.append(order.toString()).append("\n");
                }
                DateTime lastEmail = emailAlertManager.send("ExtraFerret",
                    "Ferret order(s) not known by Tomahawk", sb.toString());

                if (lastEmail.getMillis() >= now.getMillis()) {
                    log.info("Sent email ExtraFerret");
                }
            }
            // only report if we have two polls with errors
            if (extraTomahawkCount > 0) {
                if (!hadMoreTomahawkOrders) {
                    hadMoreTomahawkOrders = true;
                } else {
                    log.debug("Extra Tomahawk orders=" + extraTomahawkCount);
                    StringBuilder sb = new StringBuilder(128 * extraTomahawkCount);
                    List<LiveOrder> extraTomahawkOrders = getExtraTomahawkOrders();
                    for (LiveOrder order : extraTomahawkOrders) {
                        sb.append(order.toString()).append("\n");
                    }

                    DateTime lastEmail = emailAlertManager.send("ExtraTomahawk",
                        "Tomahawk order(s) not known by Ferret", sb.toString());

                    if (lastEmail.getMillis() >= now.getMillis()) {
                        log.info("Sent email ExtraTomahawk");
                    }
                }
            }
        } else {
            // everything is ok - reset flag
            hadMoreTomahawkOrders = false;
        }
        
        return mismatchFound;
    }

    synchronized List<LiveOrder> queryTomahawk() {
        int maxOrderId = getTomahawkMaxId();
        lastTomahawkQuery = LiveOrders.ORDERS.ordersAfter(maxOrderId);

        for (LiveOrder lo : lastTomahawkQuery) {
            maxOrderId = Math.max(maxOrderId, lo.id());
        }
        tomahawkMaxId = maxOrderId;

        return getLastTomahawkQuery();
    }

    List<LiveOrder> getLastTomahawkQuery() {
        return lastTomahawkQuery;
    }

    synchronized List<Order> queryFerret() {
        long maxOrderId = getFerretMaxId();
        lastFerretQuery = OrderDao.getInstance().newOrdersForClientAppAfter(maxOrderId, "TOMAHAWK");

        for (Order order : lastFerretQuery) {
            maxOrderId = Math.max(maxOrderId, order.getId());
        }
        ferretMaxId = maxOrderId;

        return getLastFerretQuery();
    }

    List<Order> getLastFerretQuery() {
        return lastFerretQuery;
    }

    @Override
    protected void systemShutdownHandler() {
        log.warn("System shutdown");
    }

    @Override
    protected void uncaughtException(Thread t, Throwable e) {
        log.error("UncaughtException on thread: " + t.getName(), e);
        emailAlertManager.send("UncaughtExceptionHandler", "Unhandled exception in HawkEye", StringUtils
            .exceptionToString(e));
    }

    public String getFerretState() {
        return ferretState;
    }

    public Connection getBrokerConnection() {
        return brokerConnection;
    }

}
