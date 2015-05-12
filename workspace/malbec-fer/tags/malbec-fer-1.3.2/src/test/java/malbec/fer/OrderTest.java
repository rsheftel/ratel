package malbec.fer;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import malbec.AbstractBaseTest;
import malbec.bloomberg.types.BBYellowKey;
import malbec.fer.dao.OrderDao;
import malbec.fer.util.OrderValidation;

import org.joda.time.LocalDate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OrderTest extends AbstractBaseTest {

    private static final String LIMIT_ORDER_QUANTITY_STR = "11";
    private static final String LIMIT_PRICE_STR = "12.54";
    private static final String STOP_PRICE_STR = "14.59";
    private static final String VALID_USER_ORDER_ID = "123456";
    
    private EntityManagerFactory emf;
    

    @BeforeClass(groups = { "unittest" })
    public void loadEntityManager(){
        Properties props = buildDbProperties();
        emf = Persistence.createEntityManagerFactory("BADB", props);
    }
    
    @Test(groups = { "unittest" })
    public void testSpecialOrderRules() {

        LocalDate orderDate = new LocalDate();
        String orderDateString = orderDate.toString("YYYYMMdd");
        String testClientOrderId = orderDateString + "-0" + VALID_USER_ORDER_ID;
        
        Order order = new Order();
        String clientOrderId = OrderValidation.generateClientOrderId(orderDate, order.getClass(), VALID_USER_ORDER_ID);
        
        order.setClientOrderId(clientOrderId);
        assertTrue(order.getClientOrderId().length() == 16, "ClientOrderId not at max length");
        assertEquals(order.getClientOrderId(), testClientOrderId, "ClientOrderId not generated correctly");
    }

    @Test(groups = { "unittest", "dbinserttest" })
    public void testInsert() {
        // First unit of work
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        
        String userOrderId = generateUserOrderId("O");
        Order order = new Order(userOrderId, "ZVZZT", "EQUITY", BBYellowKey.Equity, "BUY", "LIMIT", new BigDecimal(
                12), "TEST-SERVER");

        order.setStrategy("TEST-INSERT-STRATEGY");
        order.setAccount("TEST-INSERT-ACCOUNT");
        order.setYellowKey(BBYellowKey.Equity);
        em.persist(order);
        tx.commit();

        assertNotNull(order.getId(), "Failed to insert order");
        assertNotNull(order.getCreatedAt(), "Creation time not populated");
        assertNotNull(order.getUpdatedAt(), "Update time not populated");

        // Insert a futures
        tx.begin();
        userOrderId = generateUserOrderId("O");
        Order futuresOrder = new Order(userOrderId, "USM9", "FUTURES", BBYellowKey.Comdty, "BUY", "LIMIT",
                new BigDecimal(19), "TEST-SERVER");

        futuresOrder.setSecurityIDSource("A");
        futuresOrder.setAccount("ORDER-INSERT-ACCOUNT");
        futuresOrder.setStrategy("ORDER-INSERT-STRATEGY");
        em.persist(futuresOrder);
        tx.commit();
    }

    @Test(groups = { "unittest" })
    public void testDBUpdates() {
        Order order = createLimitOrder("DATABASE");
        order.setStrategy("DB-TEST-STRATEGY");
        order.setAccount("DB-TEST-ACCOUNT");
        order.setSecurityType("Equity");

        OrderDao dao = OrderDao.getInstance();

        long id = dao.persistOrder(order);
        assertNotSame(id, -1, "Failed to insert test order");

        Order rt = dao.updateOrderWithOrderId(order.getClientOrderId(), OrderStatus.Accepted, String.valueOf(System.nanoTime()));

        assertNotNull(rt, "Failed to update order to accepted");
    }

    @Test(groups = { "unittest" })
    public void testDuplicatInsert() {
        // First unit of work
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        String userOrderId = generateUserOrderId("O");
        
        Order order = new Order(userOrderId, "ZVZZT", "EQUITY", BBYellowKey.Equity, "BUY", "LIMIT", new BigDecimal(
                12), "TEST-SERVER");
        
        order.setStrategy("TEST-INSERT-STRATEGY");
        order.setAccount("TEST-INSERT-ACCOUNT");

        em.persist(order);
        tx.commit();

        assertNotNull(order.getId(), "Failed to insert order");
        assertNotNull(order.getCreatedAt(), "Creation time not populated");
        assertNotNull(order.getUpdatedAt(), "Update time not populated");

        // Insert a futures
        tx.begin();

        Order duplicateOrder = new Order(order.getUserOrderId(), "ZVZZT", "EQUITY", BBYellowKey.Equity, "BUY", "LIMIT", new BigDecimal(
                12), "TEST-SERVER");

        try {
            em.persist(duplicateOrder);
            tx.commit();
            System.err.println(duplicateOrder.getClientOrderId());
            assertFalse(true, "Expected unique index violation on ClientOrderID");
        } catch (RuntimeException e) {
        	if (tx.isActive()) {
        		tx.rollback();
        	}
        }

        em.close();
    }
    @Test(groups = { "unittest" })
    public void testInsertAndUpdate() {
        // First unit of work
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        String userOrderId = generateUserOrderId("O");
        
        Order order = new Order(userOrderId, "ZVZZT", "EQUITY", BBYellowKey.Equity, "BUY", "LIMIT", new BigDecimal(
                12), "TEST-SERVER");

        order.setStrategy("TEST-INSERT-STRATEGY");
        order.setAccount("TEST-INSERT-ACCOUNT");
        em.persist(order);
        tx.commit();

        assertNotNull(order.getId(), "Failed to insert order");
        assertNotNull(order.getCreatedAt(), "Creation time not populated");
        assertNotNull(order.getUpdatedAt(), "Update time not populated");
        // Store these to compare with later
        Date createdAt = order.getCreatedAt();
        Date updatedAt = order.getUpdatedAt();

        tx.begin();

        Order orderCopy = em.getReference(Order.class, order.getId());
        orderCopy.setQuantity(orderCopy.getQuantity().add(BigDecimal.valueOf(1L)));
        assertEquals(OrderStatus.New, orderCopy.getStatus(), "Failed to read status correctly");
        em.persist(orderCopy);
        tx.commit();
        em.close();

        assertNotNull(orderCopy);
        assertNotNull(orderCopy.getCreatedAt(), "Creation time not populated");
        assertEquals(orderCopy.getCreatedAt(), createdAt, "Creation time modified on update");
        assertNotNull(orderCopy.getUpdatedAt(), "Update time not populated");
        assertNotSame(orderCopy.getUpdatedAt(), updatedAt, "Update time did not change");
    }

    /**
     * This is to be used to generate id for testing.
     * 
     * @param base
     * @return
     */
    public static String generateUserOrderId(String base) {
        sleep(100);
        String nanoTime = String.valueOf(System.nanoTime());
        
        int lengthNeeded = 6 - base.length();
        String userOrderId = base + nanoTime.substring(nanoTime.length() - lengthNeeded);
        return userOrderId;
    }
    
    private Properties buildDbProperties() {
        Properties props = new Properties();
        
        props.setProperty("hibernate.archive.autodetection", "class");
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.format_sql", "true");
        props.setProperty("hibernate.connection.driver_class", "net.sourceforge.jtds.jdbc.Driver");
        props.setProperty("hibernate.connection.url", "jdbc:jtds:sqlserver://SQLDEVTS:2433/BADB");
        props.setProperty("hibernate.connection.username", "sim");
        props.setProperty("hibernate.connection.password", "Sim5878");
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        return props;
    }

    @Test(groups = { "unittest" })
    public void testCreateFromMap() {
        Order order = createLimitOrder();

        assertNotNull(order.getUserOrderId(), "UserOrderID not set");
        assertNotNull(order.getSide(), "Side not set");
        assertNotNull(order.getOrderType(), "OrderType not set");
        assertNotNull(order.getLimitPrice(), "LimitPrice not set");
        assertEquals(new BigDecimal(LIMIT_PRICE_STR), order.getLimitPrice(),
                "Limit Price not converted correctly");
        assertNotNull(order.getQuantity(), "Quantity not set");
        assertEquals(new BigDecimal(LIMIT_ORDER_QUANTITY_STR), order.getQuantity(),
                "Quantity not converted correctly");
        assertNotNull(order.getSymbol(), "Symbol not set");
        assertNotNull(order.getTimeInForce(), "TimeInForce not set");
        // we do not store Handling Instructions
    }

    @Test(groups = { "unittest" })
    public void testConvertToMap() {
        // This is lazy, but it should test what we need
        Order order1 = new Order(createLimitOrderMap());
        Map<String, String> orderMap = order1.toMap();

        Order order = new Order(orderMap);

        assertNotNull(order.getUserOrderId(), "UserOrderID not set");
        assertNotNull(order.getSide(), "Side not set");
        assertNotNull(order.getOrderType(), "OrderType not set");
        assertNotNull(order.getLimitPrice(), "LimitPrice not set");
        assertEquals(new BigDecimal(LIMIT_PRICE_STR), order.getLimitPrice(),
                "Limit Price not converted correctly");
        assertNotNull(order.getQuantity(), "Quantity not set");
        assertEquals(new BigDecimal(LIMIT_ORDER_QUANTITY_STR), order.getQuantity(),
                "Quantity not converted correctly");
        assertNotNull(order.getSymbol(), "Symbol not set");
        assertNotNull(order.getTimeInForce(), "TimeInForce not set");
        // we do not store Handling Instructions
    }

    @Test(groups = { "unittest" })
    public void testFuturesOrder() {
        Order order = new Order(createFuturesOrderMap());
        order.setSecurityType("Futures");
        
        assertNotNull(order.getUserOrderId(), "UserOrderID not set");
        assertNotNull(order.getSide(), "Side not set");
        assertNotNull(order.getOrderType(), "OrderType not set");
        assertNotNull(order.getLimitPrice(), "LimitPrice not set");
        assertEquals(new BigDecimal(LIMIT_PRICE_STR), order.getLimitPrice(),
                "Limit Price not converted correctly");
        assertNotNull(order.getQuantity(), "Quantity not set");
        assertEquals(new BigDecimal(LIMIT_ORDER_QUANTITY_STR), order.getQuantity(),
                "Quantity not converted correctly");
        assertNotNull(order.getSymbol(), "Symbol not set");
        assertNotNull(order.getTimeInForce(), "TimeInForce not set");
        assertNotNull(order.getSecurityIDSource(), "Security ID Source not set");
        assertEquals(order.getSecurityType().toUpperCase(), "FUTURES", "Security type not set to Futures");
    }

    public static Order createFuturesOrder(String platform) {
        Order order = createFuturesOrder();
        order.setPlatform(platform);

        return order;
    }
    
    public static Order createFuturesOrder() {
        return new Order(createFuturesOrderMap());
    }

    private static Map<String, String> createFuturesOrderMap() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("UserOrderId", generateUserOrderId("O"));
        orderRecord.put("Side", "SELL");
        orderRecord.put("OrderType", "LIMIT");
        orderRecord.put("LimitPrice", LIMIT_PRICE_STR);
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "US.1C");
        orderRecord.put("SecurityType", "Futures");
        orderRecord.put("YellowKey", "Comdty");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("SecurityIDSource", "A"); // 
        orderRecord.put("Strategy", "TESTF");
        orderRecord.put("ClientUserID", "UTUSER");
        orderRecord.put("ClientAppName", "TestFramework");
        return orderRecord;
    }

    public static Order createLimitOrder() {
        return new Order(createLimitOrderMap());
    }

    public static Order createLimitOrder(String platform) {
        Order order = createLimitOrder();
        order.setPlatform(platform);

        return order;
    }
    
    private static Map<String, String> createLimitOrderMap() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("UserOrderId", generateUserOrderId("O"));
        orderRecord.put("Side", "BUY");
        orderRecord.put("OrderType", "LIMIT");
        orderRecord.put("LimitPrice", LIMIT_PRICE_STR);
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "ZVZZT");
        orderRecord.put("SecurityType", "Equity");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("Strategy", "TEST.EQUITY");
        orderRecord.put("YellowKey", "Equity");
        orderRecord.put("ClientUserID", "UTUSER");
        orderRecord.put("ClientAppName", "TestFramework");
        orderRecord.put("OrderDate", new LocalDate().toString());

        return orderRecord;
    }

    public Order createMarketOrder() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("UserOrderId", generateUserOrderId("O"));
        orderRecord.put("Side", "BUY");
        orderRecord.put("OrderType", "MARKET");
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "ZVZZT");
        orderRecord.put("SecurityType", "Equity");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("Strategy", "TESTE");
        orderRecord.put("ClientUserID", "UTUSER");
        orderRecord.put("ClientAppName", "TestFramework");
        
        return new Order(orderRecord);
    }

    public Order createStopLimitOrder() {
        return new Order(createStopLimitOrderMap());
    }

    private Map<String, String> createStopLimitOrderMap() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("UserOrderId", generateUserOrderId("O"));
        orderRecord.put("Side", "BUY");
        orderRecord.put("OrderType", "STOPLIMIT");
        orderRecord.put("LimitPrice", LIMIT_PRICE_STR);
        orderRecord.put("StopPrice", STOP_PRICE_STR);
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "ZVZZT");
        orderRecord.put("SecurityType", "Equity");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("Strategy", "TESTE");
        orderRecord.put("ClientUserID", "UTUSER");
        orderRecord.put("ClientAppName", "TestFramework");
        return orderRecord;
    }

    public Order createStopOrder() {
        return new Order(createStopOrderMap());
    }

    private Map<String, String> createStopOrderMap() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("UserOrderId", generateUserOrderId("O"));
        orderRecord.put("Side", "BUY");
        orderRecord.put("OrderType", "STOP");
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "ZVZZT");
        orderRecord.put("SecurityType", "Equity");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("Strategy", "TESTE");
        orderRecord.put("ClientUserID", "UTUSER");
        orderRecord.put("ClientAppName", "TestFramework");
        
        return orderRecord;
    }
    


}
