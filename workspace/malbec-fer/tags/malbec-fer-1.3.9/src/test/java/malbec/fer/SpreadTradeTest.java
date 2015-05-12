package malbec.fer;

import static org.testng.Assert.*;

import java.math.BigDecimal;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.testng.annotations.Test;


public class SpreadTradeTest {

    @Test(groups = { "unittest" })
    public void testSpreadTradeCreateSetup() {
        
        SpreadTrade st = new SpreadTrade("TestPair1", "Ratio Day Chg");
        st.addLeg1("buy", "ZVZZT", "equity", "strategy-e", "CROSS", 100);
        st.addLeg2("sell", "ZWZZT", "equity", "strategy-e", "CROSS", 200);
        st.setSetup(1, 4, 1);
        
        st.setLeg1Initiate(true);
        st.setLeg2Initiate(true);
        
        assertTrue(st.isLeg1Set(), "Leg1 not set");
        assertTrue(st.isLeg2Set(), "Leg2 not set");
        assertTrue(st.haveInitiator(), "Initiator not set");
        
        assertEquals(st.getHedgeRatio(), new BigDecimal("0.500"), "Failed to calculate the hedge ratio");
        assertEquals(st.getSetupPositionObjective(), BigDecimal.valueOf(100), "Setup objective does match leg1 quantity");
        
        assertNull(st.getUnwindPositionObjective(), "Unwind objective is set");
    }
    
    @Test(groups = { "unittest" })
    public void testSpreadTradeCreateUnwind() {
        
        SpreadTrade st = createTestSpreadTrade();
        st.setLeg1Initiate(true);
        
        st.setUnwind(1, 4, 1);
        
        assertTrue(st.isLeg1Set(), "Leg1 not set");
        assertTrue(st.isLeg2Set(), "Leg2 not set");
        assertTrue(st.haveInitiator(), "Initiator not set");
        
        assertEquals(st.getHedgeRatio(), new BigDecimal("5.000"), "Failed to calculate the hedge ratio");
        assertEquals(st.getUnwindPositionObjective(), BigDecimal.valueOf(1000), "Unwind objective does match leg1 quantity");
        
        assertNull(st.getSetupPositionObjective(), "Setup objective is set");
    }
    
    @Test(groups = { "unittest" })
    public void testSpreadTradeCreateSetupUnwind() {
        
        SpreadTrade st = createTestSpreadTrade();
        st.setLeg2Initiate(true);
        st.setSetup(2, 5, 2);
        st.setUnwind(1, 4, 1);
        
        assertTrue(st.isLeg1Set(), "Leg1 not set");
        assertTrue(st.isLeg2Set(), "Leg2 not set");
        assertTrue(st.haveInitiator(), "Initiator not set");
        
        assertEquals(st.getHedgeRatio(), new BigDecimal("5.000"), "Failed to calculate the hedge ratio");
        assertEquals(st.getUnwindPositionObjective(), BigDecimal.valueOf(1000), "Unwind objective does match leg1 quantity");
        
        assertEquals(st.getSetupPositionObjective(), BigDecimal.valueOf(1000), "Setup objective does match leg1 quantity");
    }

    private SpreadTrade createTestSpreadTrade() {
        return createTestSpreadTrade(1000, 200);
    }
    
    private static SpreadTrade createTestSpreadTrade(int leg1Quantity, int leg2Quantity) {
        SpreadTrade st = new SpreadTrade("TestPair-" + System.nanoTime() / 100000, "Ratio Day Chg");
        st.setPlatform("UNIT-TEST");
        st.addLeg1("buy", "ZVZZT", "equity", "strategy-e", "CROSS", leg1Quantity);
        st.addLeg2("sell", "ZWZZT", "equity", "strategy-e", "CROSS", leg2Quantity);
        
        return st;
    }
    
    @Test(groups = { "unittest" })
    public void testSpreadTradeCreateSetupUnwindPersist() {
        // Do the persistence
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

        // First unit of work
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        
        SpreadTrade st = createTestSpreadTrade();
        st.setLeg1Initiate(true);
        st.setSetup(2, 5, 2);
        st.setUnwind(1, 4, 1);
        
        assertTrue(st.isLeg1Set(), "Leg1 not set");
        assertTrue(st.isLeg2Set(), "Leg2 not set");
        assertTrue(st.haveInitiator(), "Initiator not set");
        
        assertEquals(st.getHedgeRatio(), new BigDecimal("5.000"), "Failed to calculate the hedge ratio");
        assertEquals(st.getUnwindPositionObjective(), BigDecimal.valueOf(1000), "Unwind objective does match leg1 quantity");
        
        assertEquals(st.getSetupPositionObjective(), BigDecimal.valueOf(1000), "Setup objective does match leg1 quantity");
        
        // there would be logic to find the account based on the security type and strategy and platform
        // we will hard code it here
        st.setLeg1Account("UnitTestAccount");
        st.setLeg2Account("UnitTestAccount");
        
        em.persist(st);
        tx.commit();
    }
    

    @Test(groups = { "unittest" })
    public void testToMap() {
        SpreadTrade st = createTestSpreadTrade();
        
        Map<String, String> stAsMap = st.toMap();
        
        assertNotNull(stAsMap.get("PAIRID"), "PairID not in map");
        assertNotNull(stAsMap.get("PLATFORM"), "Platform not in map");
        
        assertNotNull(stAsMap.get("LEG1SYMBOL"), "Leg1 symbol not set.");
        assertNotNull(stAsMap.get("LEG2SYMBOL"), "Leg2 symbol not set.");
        assertFalse(containsNull(stAsMap), "A value contains null instead of empty string");
    }

    @Test(groups = { "unittest" })
    public void testFromMap() {
        SpreadTrade st = createTestSpreadTrade();
        
        Map<String, String> stAsMap = st.toMap();
        
        SpreadTrade fromMap = new SpreadTrade(stAsMap);
        
        assertEquals(fromMap.getPairId(), st.getPairId(), "PairId does not match");
        assertNotNull(fromMap.getLeg1().getSymbol(), "Leg1 symbol not populated");
        assertNotNull(fromMap.getLeg2().getSymbol(), "Leg2 symbol not populated");
        assertNotNull(fromMap.getHedgeRatio(), "HedgeRatio not calculated");
        assertEquals(st.getHedgeRatio(), fromMap.getHedgeRatio(), "HedgeRatio calculated incorrectly");
        
    }
    
    private boolean containsNull(Map<String, String> map) {
     
        for (String value : map.values()) {
            if (value == null) {
                return true;
            }
        }
        
        return false;
    }

    public static SpreadTrade createTestSpreadTrade(String platform, int leg1Quantity, int leg2Quantity) {
        SpreadTrade st = createTestSpreadTrade(leg1Quantity, leg2Quantity);
        st.setPlatform(platform);
        
        return st;
    }

}
