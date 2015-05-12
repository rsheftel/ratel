package malbec.fer.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class MarketTickersMapper {

    // Start EntityManagerFactory
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("SystemDB");

    private Map<String, MarketTicker> marketToBloomberg = new HashMap<String, MarketTicker>();

    public MarketTickersMapper() {
        this(false);
    }

    public MarketTickersMapper(boolean initalize) {
        if (initalize) {
            initialize(marketToBloomberg);
        }
    }

    public synchronized int initialize() {
        return initialize(marketToBloomberg);
    }

    @SuppressWarnings("unchecked")
    private synchronized int initialize(Map<String, MarketTicker> m2bMap) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<MarketTicker> results;

        try {
            Query query = em.createQuery("from MarketTicker");
            tx.begin();
            results = query.getResultList();
            tx.commit();
        } finally {
            em.close();
        }

        if (results == null) {
            return -1;
        }

        for (MarketTicker mt : results) {
            if (mt.getMarket() != null && mt.getBloomberg() != null) {
                m2bMap.put(mt.getMarket(), mt);
            }
        }

        return results.size();
    }
    
    public synchronized int reload() {
        Map<String, MarketTicker> m2bMap = new HashMap<String, MarketTicker>();
        
        int reloadCount = initialize(m2bMap);
        marketToBloomberg = m2bMap;
        
        return reloadCount;
    }

    /**
     * This is for testing purposes and not intended to be used.
     *  
     * @param platform
     * @param bloombergRoot
     * @param platformRoot
     */
    public void addMarketMapping(String market, String bloomberg, String tsdb) {
        MarketTicker mt = new MarketTicker(market, bloomberg, tsdb);
        // add to the bloomberg -> other mapping
        marketToBloomberg.put(market.toUpperCase(), mt);
    }

    public String lookupBloomberg(String market) {
        MarketTicker mt = marketToBloomberg.get(market.toUpperCase());
        
        if (mt != null) {
            return mt.getBloomberg();
        }
        
        return null;
    }

}
