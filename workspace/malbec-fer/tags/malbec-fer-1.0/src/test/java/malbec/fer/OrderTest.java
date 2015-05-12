package malbec.fer;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import malbec.AbstractBaseTest;

import org.testng.annotations.Test;

public class OrderTest extends AbstractBaseTest {

    private static final String LIMIT_ORDER_QUANTITY_STR = "11";
    private static final String LIMIT_PRICE_STR = "12.54";
    private static final String STOP_PRICE_STR = "14.59";

    @Test(groups = { "unittest" })
    public void testSpecialOrderRules() {

        Order order = new Order();
        order.setClientOrderID("12345678901234567");
        assertFalse(order.getClientOrderID().equals("12345678901234567"), "ClientOrderID not shortened");
        assertTrue(order.getClientOrderID().length() == 16, "ClientOrderID not at max length");
        order.setClientOrderID("123 56789%123456");
        assertTrue(order.getClientOrderID().equals("12356789123456"),
                "ClientOrderID special character removal failed");
    }

    @Test(groups = { "unittest" })
    public void testInsert() {

        // Start EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

        // First unit of work
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Order order = new Order("IT-" + System.nanoTime(), "ZZVTZ", "EQUITY", "BUY", "LIMIT", new BigDecimal(
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

        Order futuresOrder = new Order("IT-" + System.nanoTime(), "USM9", "FUTURES", "BUY", "LIMIT",
                new BigDecimal(19), "TEST-SERVER");

        futuresOrder.setSecurityIDSource("A");
        futuresOrder.setAccount("ORDER-INSERT-ACCOUNT");
        futuresOrder.setStrategy("ORDER-INSERT-STRATEGY");
        em.persist(futuresOrder);
        tx.commit();

    }
    @Test(groups = { "unittest" })
    public void testDuplicatInsert() {
        // Start EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

        // First unit of work
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Order order = new Order("IT-" + System.nanoTime(), "ZZVTZ", "EQUITY", "BUY", "LIMIT", new BigDecimal(
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

        Order duplicateOrder = new Order(order.getClientOrderID(), "ZZVTZ", "EQUITY", "BUY", "LIMIT", new BigDecimal(
                12), "TEST-SERVER");

        try {
            em.persist(duplicateOrder);
            assertFalse(true, "Expected unique index violation on ClientOrderID");
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
        }

        em.close();
    }
    @Test(groups = { "unittest" })
    public void testInsertAndUpdate() {

        // Start EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

        // First unit of work
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Order order = new Order("IT-" + System.nanoTime(), "ZZVTZ", "EQUITY", "BUY", "LIMIT", new BigDecimal(
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
        em.persist(orderCopy);
        tx.commit();
        em.close();

        assertNotNull(orderCopy);
        assertNotNull(orderCopy.getCreatedAt(), "Creation time not populated");
        assertEquals(orderCopy.getCreatedAt(), createdAt, "Creation time modified on update");
        assertNotNull(orderCopy.getUpdatedAt(), "Update time not populated");
        assertNotSame(orderCopy.getUpdatedAt(), updatedAt, "Update time did not change");
    }

    @Test(groups = { "unittest" })
    public void testCreateFromMap() {
        Order order = createLimitOrder();

        assertNotNull(order.getClientOrderID(), "ClientOrderID not set");
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

        assertNotNull(order.getClientOrderID(), "ClientOrderID not set");
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
        
        assertNotNull(order.getClientOrderID(), "ClientOrderID not set");
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

    public Order createFuturesOrder() {
        return new Order(createFuturesOrderMap());
    }

    private Map<String, String> createFuturesOrderMap() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("ClientOrderID", "UT-" + System.nanoTime());
        orderRecord.put("Side", "SELL");
        orderRecord.put("OrderType", "LIMIT");
        orderRecord.put("LimitPrice", LIMIT_PRICE_STR);
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "USM9");
        orderRecord.put("SecurityType", "Futures");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("SecurityIDSource", "A"); // 
        orderRecord.put("Strategy", "TESTF");
        return orderRecord;
    }

    public Order createLimitOrder() {
        return new Order(createLimitOrderMap());
    }

    private Map<String, String> createLimitOrderMap() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("ClientOrderID", "UT-" + System.nanoTime());
        orderRecord.put("Side", "BUY");
        orderRecord.put("OrderType", "LIMIT");
        orderRecord.put("LimitPrice", LIMIT_PRICE_STR);
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "ZVZZT");
        orderRecord.put("SecurityType", "Equity");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("Strategy", "TEST.EQUITY");

        return orderRecord;
    }

    public Order createMarketOrder() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("ClientOrderID", "UT-" + System.nanoTime());
        orderRecord.put("Side", "BUY");
        orderRecord.put("OrderType", "MARKET");
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "ZVZZT");
        orderRecord.put("SecurityType", "Equity");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("Strategy", "TESTE");
        
        return new Order(orderRecord);
    }

    public Order createStopLimitOrder() {
        return new Order(createStopLimitOrderMap());
    }

    private Map<String, String> createStopLimitOrderMap() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("ClientOrderID", "UT-" + System.nanoTime());
        orderRecord.put("Side", "BUY");
        orderRecord.put("OrderType", "STOPLIMIT");
        orderRecord.put("LimitPrice", LIMIT_PRICE_STR);
        orderRecord.put("StopPrice", STOP_PRICE_STR);
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "ZVZZT");
        orderRecord.put("SecurityType", "Equity");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("Strategy", "TESTE");
        return orderRecord;
    }

    public Order createStopOrder() {
        return new Order(createStopOrderMap());
    }

    private Map<String, String> createStopOrderMap() {
        Map<String, String> orderRecord = new HashMap<String, String>();
        orderRecord.put("ClientOrderID", "UT-" + System.nanoTime());
        orderRecord.put("Side", "BUY");
        orderRecord.put("OrderType", "STOP");
        orderRecord.put("Quantity", LIMIT_ORDER_QUANTITY_STR);
        orderRecord.put("Symbol", "ZVZZT");
        orderRecord.put("SecurityType", "Equity");
        orderRecord.put("TimeInForce", "DAY");
        orderRecord.put("Strategy", "TESTE");
        
        return orderRecord;
    }
}
