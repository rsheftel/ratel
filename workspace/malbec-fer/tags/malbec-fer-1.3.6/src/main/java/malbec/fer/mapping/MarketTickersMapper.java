package malbec.fer.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import malbec.bloomberg.types.BBYellowKey;

public class MarketTickersMapper {

    // Start EntityManagerFactory
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("SystemDB");

    // market ticker to MarketData
    private Map<String, MarketTicker> marketToBloomberg = new HashMap<String, MarketTicker>();
    
    // BloombergRoot to FuturesSecurity
    private Map<String, FuturesSecurity> bloombergRootToFuturesSecurity = new HashMap<String, FuturesSecurity>();

    public MarketTickersMapper() {
        this(false);
    }

    public MarketTickersMapper(boolean initalize) {
        if (initalize) {
            initialize(marketToBloomberg);
        }
    }

    public synchronized int initialize() {
        int count1 = initialize(marketToBloomberg);
        int count2 = initializeFutures(bloombergRootToFuturesSecurity);
        
        return count1 + count2;
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
                m2bMap.put(mt.getMarket().toUpperCase(), mt);
            }
        }

        return results.size();
    }

    @SuppressWarnings("unchecked")
    private synchronized int initializeFutures(Map<String, FuturesSecurity> b2sMap) {
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
                FuturesSecurity security = new FuturesSecurity(mt.getBloombergRoot(), mt.getYellowKey());
                b2sMap.put(mt.getBloombergRoot().toUpperCase(), security);
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
    public void addMarketMapping(String market, String bloomberg, String yellowKey, String tsdb, String bbRoot) {
        MarketTicker mt = new MarketTicker(market, bloomberg, yellowKey, tsdb, bbRoot);
        FuturesSecurity security = new FuturesSecurity(bbRoot, mt.getYellowKey());
        
        // add to the market -> other mapping
        marketToBloomberg.put(market.toUpperCase(), mt);
        bloombergRootToFuturesSecurity.put(bbRoot.toUpperCase(), security);
    }

    public String lookupBloomberg(String market) {
        MarketTicker mt = marketToBloomberg.get(market.toUpperCase());
        
        if (mt != null) {
            return mt.getBloomberg();
        }
        
        return null;
    }

    public BBYellowKey lookupMarketYellowKey(String market) {
        MarketTicker mt = marketToBloomberg.get(market.toUpperCase());
        
        if (mt != null) {
            return mt.getYellowKey();
        }
        
        return BBYellowKey.Unknown;
    }

    public BBYellowKey lookupYellowKey(String bloombergRoot) {
        FuturesSecurity fs = bloombergRootToFuturesSecurity.get(bloombergRoot.toUpperCase());
        
        if (fs != null) {
            return fs.getYellowKey();
        }
        
        return BBYellowKey.Unknown;
    }

}
