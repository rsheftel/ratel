package malbec.fer;

import static malbec.fer.OrderTest.*;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import malbec.bloomberg.types.BBYellowKey;
import malbec.util.MessageUtil;

import org.testng.annotations.Test;

public class CancelRequestTest {

    @Test(groups = { "unittest"})
    public void testInsert() {
        // Start EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

        // First unit of work
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        String origUserOrderId = generateUserOrderId("O");
        
        CancelRequest cr = new CancelRequest(generateUserOrderId("C"), origUserOrderId);
        cr.setSymbol("ZVZZT");
        cr.setSide("SELL");
        cr.setClientAppName("UT-INSERT");
        cr.setStrategy("TEST-INSERT-STRATEGY");
        cr.setAccount("TEST-INSERT-ACCOUNT");
        cr.setOrderType("LIMIT");
        cr.setPlatform("TEST-SERVER");
        cr.setQuantity(BigDecimal.TEN);
        cr.setSecurityType("EQUITY");
        cr.setYellowKey(BBYellowKey.Equity);
        
        em.persist(cr);
        tx.commit();

        assertNotNull(cr.getId(), "Failed to insert order");
        assertNotNull(cr.getCreatedAt(), "Creation time not populated");
        assertNotNull(cr.getUpdatedAt(), "Update time not populated");

        // Insert a futures
        tx.begin();
            
        CancelRequest futuresCR = new CancelRequest(generateUserOrderId("C"), origUserOrderId);
        
        futuresCR.setSymbol("USM9");
        futuresCR.setSecurityIDSource("A");
        futuresCR.setSide("BUY");
        futuresCR.setClientAppName("UT-INSERT");
        futuresCR.setStrategy("TEST-INSERT-STRATEGY");
        futuresCR.setAccount("TEST-INSERT-ACCOUNT");
        futuresCR.setOrderType("LIMIT");
        futuresCR.setPlatform("TEST-SERVER");
        futuresCR.setQuantity(BigDecimal.TEN);
        futuresCR.setSecurityType("FUTURES");
        futuresCR.setYellowKey(BBYellowKey.Comdty);
        
        futuresCR.setOriginalUserOrderId(generateUserOrderId("O"));
        
        em.persist(futuresCR);
        System.err.println(futuresCR.getUserOrderId());
        tx.commit();
    }
    
    @Test(groups = { "unittest" })
    public void testCreateFromMap() {
        Map<String, String> crMap = createCancelRequestMap();
        
//        MessageUtil.setPlatform("TestPlatform", crMap);
        
        CancelRequest cr = new CancelRequest(crMap);
        
        assertEquals(cr.getOriginalUserOrderId(), MessageUtil.getOriginalUserOrderId(crMap).toUpperCase(), "Failed to set OriginalClientOrderId");
        assertNotNull(cr.getUserOrderId(), "Failed to set UserOrderId");
        assertEquals(cr.getUserOrderId(), MessageUtil.getUserOrderId(crMap).toUpperCase(), "Failed to set UserOrderId");
        
        assertEquals(cr.getClientUserId(), MessageUtil.getClientUserId(crMap), "Failed to set ClientUserId");
        assertEquals(cr.getClientHostname(), MessageUtil.getClientHostname(crMap), "Failed to set ClientHostname");
        assertEquals(cr.getClientAppName(), MessageUtil.getClientAppName(crMap), "Failed to set ClientAppName");
        
    }

    public static Map<String, String> createCancelRequestMap() {
        Map<String, String> crMap = new HashMap<String, String>();
        
        MessageUtil.setOriginalUserOrderId(crMap, generateUserOrderId("O"));
        MessageUtil.setUserOrderId(crMap, generateUserOrderId("C"));
        MessageUtil.setClientUserId("UTUser", crMap);
        MessageUtil.setClientHostname("UTMachine", crMap);
        MessageUtil.setClientAppName("UTApp", crMap);
        
        return crMap;
    }
        
    public static Map<String, String> createCancelReplaceRequestMap() {
        Map<String, String> crMap = new HashMap<String, String>();

        MessageUtil.setUserOrderId(crMap, generateUserOrderId("R"));
        MessageUtil.setClientUserId("UTUser", crMap);
        MessageUtil.setClientHostname("UTMachine", crMap);
        MessageUtil.setClientAppName("UTApp", crMap);
        
        return crMap;
    }

    public static CancelRequest generateCancelRequestFromOrder(Order order) {
        Map<String, String> orderMap = order.toMap();
        MessageUtil.setOriginalUserOrderId(orderMap, MessageUtil.getUserOrderId(orderMap));
        MessageUtil.setClientOrderId(orderMap, null);
        MessageUtil.setUserOrderId(orderMap, generateUserOrderId("C"));
        
        return new CancelRequest(orderMap);
    }
    
    public static CancelReplaceRequest generateCancelReplaceRequestFromOrder(Order order) {
        Map<String, String> orderMap = order.toMap();
        MessageUtil.setClientOrderId(orderMap, null);
        MessageUtil.setUserOrderId(orderMap, generateUserOrderId("R"));
        
        return new CancelReplaceRequest(orderMap);
    }

}
