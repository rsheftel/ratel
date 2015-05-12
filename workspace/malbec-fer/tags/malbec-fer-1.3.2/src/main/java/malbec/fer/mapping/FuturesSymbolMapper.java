package malbec.fer.mapping;

import static malbec.util.FuturesSymbolUtil.combineRootMaturityMonthYear;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class FuturesSymbolMapper {

    // Start EntityManagerFactory
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    private Map<FuturesSymbolKey, FuturesSymbol> platformToBloomberg = new HashMap<FuturesSymbolKey, FuturesSymbol>();
    private Map<FuturesSymbolKey, FuturesSymbol> bloombergToPlatform = new HashMap<FuturesSymbolKey, FuturesSymbol>();
    private Map<FuturesSymbolKey, FuturesSymbolMap> bloombergRootToRicRoot = new HashMap<FuturesSymbolKey, FuturesSymbolMap>();    

    public FuturesSymbolMapper() {
        this(false);
    }

    public FuturesSymbolMapper(boolean initalize) {
        if (initalize) {
            initializeInbound(platformToBloomberg, bloombergToPlatform);
            initializeOutbound(bloombergRootToRicRoot);
        }
    }

    public synchronized int initialize() {
        int inboundCount = initializeInbound(platformToBloomberg, bloombergToPlatform);
        int outboundCount = initializeOutbound(bloombergRootToRicRoot);
        
        return inboundCount + outboundCount;
    }

    @SuppressWarnings("unchecked")
    private synchronized int initializeOutbound(Map<FuturesSymbolKey, FuturesSymbolMap> b2rMap) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<FuturesSymbolMap> results;

        try {
            Query query = em.createQuery("from FuturesSymbolMap");
            tx.begin();
            results = query.getResultList();
            tx.commit();
        } finally {
            em.close();
        }

        if (results == null) {
            return -1;
        }

        for (FuturesSymbolMap fs : results) {
            // We may have bad data, ignore the rows as they are not used
            if (fs.getPlatform() != null && fs.getBloombergRoot() != null) {
                b2rMap.put(new FuturesSymbolKey(fs.getPlatform(), fs.getBloombergRoot()), fs);
            }
        }

        return results.size();
    }
    
    @SuppressWarnings("unchecked")
    private synchronized int initializeInbound(Map<FuturesSymbolKey, FuturesSymbol> p2bMap, Map<FuturesSymbolKey, FuturesSymbol> b2pMap) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<FuturesSymbol> results;

        try {
            Query query = em.createQuery("from FuturesSymbol");
            tx.begin();
            results = query.getResultList();
            tx.commit();
        } finally {
            em.close();
        }

        if (results == null) {
            return -1;
        }

        for (FuturesSymbol fs : results) {
            // We may have bad data, ignore the rows as they are not used
            if (fs.getPlatform() != null && fs.getPlatformSymbol() != null && fs.getBloombergSymbol() != null) {
                p2bMap.put(new FuturesSymbolKey(fs.getPlatform(), fs.getBloombergSymbol()), fs);
                b2pMap.put(new FuturesSymbolKey(fs.getPlatform(), fs.getPlatformSymbol()), fs);
            }
        }

        return results.size();
    }

    public synchronized int reload() {
        Map<FuturesSymbolKey, FuturesSymbol> b2pMap = new HashMap<FuturesSymbolKey, FuturesSymbol>();
        Map<FuturesSymbolKey, FuturesSymbol> p2bMap = new HashMap<FuturesSymbolKey, FuturesSymbol>();

        Map<FuturesSymbolKey, FuturesSymbolMap> b2rMap = new HashMap<FuturesSymbolKey, FuturesSymbolMap>();
        
        int reloadCount = initializeInbound(p2bMap, b2pMap);
        reloadCount += initializeOutbound(b2rMap);
        
        platformToBloomberg = p2bMap;
        bloombergToPlatform = b2pMap;
        bloombergRootToRicRoot = b2rMap;

        return reloadCount;
    }

    /**
     * This is for testing purposes and not intended to be used.
     *  
     * @param platform
     * @param bloombergRoot
     * @param platformRoot
     */
    public void addBloombergMapping(String platform, String bloombergRoot, String platformRoot) {
        FuturesSymbol fs = new FuturesSymbol(platform, platformRoot, bloombergRoot);
        // add to the bloomberg -> other mapping
        platformToBloomberg.put(new FuturesSymbolKey(platform, bloombergRoot), fs);
        // add to the platform -> other mapping
        bloombergToPlatform.put(new FuturesSymbolKey(platform, platformRoot), fs);
    }

    public void addBloombergToRicMapping(String platform, String bloombergRoot, String ricRoot, BigDecimal multiplier) {
        FuturesSymbolMap fs = new FuturesSymbolMap(platform, bloombergRoot, ricRoot, multiplier);
        // add to the platform -> other mapping
        bloombergRootToRicRoot.put(new FuturesSymbolKey(platform, bloombergRoot), fs);
    }

    private static class FuturesSymbolKey {
        String platformID;
        String symbolRoot; // will be Bloomberg symbol

        public FuturesSymbolKey(String platform, String bloombergSymbol) {
            this.platformID = platform.toUpperCase();
            this.symbolRoot = bloombergSymbol.toUpperCase();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FuturesSymbolKey)) {
                return false;
            }

            FuturesSymbolKey other = (FuturesSymbolKey) obj;
            return platformID.equals(other.platformID) && symbolRoot.equals(other.symbolRoot);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return platformID.hashCode() *17 + symbolRoot.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            
            sb.append("platformID=").append(platformID);
            sb.append(", symbolRoot=").append(symbolRoot);
            
            return sb.toString();
        }
    }

    public String mapPlatformRootToBloombergSymbol(String platform, String symbolRoot, String monthYear) {
        String bloombergRoot = mapPlatformRootToBloombergRoot(platform, symbolRoot, true); 
       
        return combineRootMaturityMonthYear(bloombergRoot, monthYear);
    }
    
    public String mapBloombergRootToPlatformRoot(String platform, String bloombergRoot, String defaultValue) {
        FuturesSymbol mappedSymbol = platformToBloomberg.get(new FuturesSymbolKey(platform, bloombergRoot));
        
        if (mappedSymbol != null) {
            return mappedSymbol.getPlatformSymbol();
        }
        
        return defaultValue;
    }

    public String mapBloombergRootToRicRoot(String platform, String bloombergRoot) {
        FuturesSymbolMap mappedSymbol = bloombergRootToRicRoot.get(new FuturesSymbolKey(platform, bloombergRoot));
        
        if (mappedSymbol != null) {
            return mappedSymbol.getRicRoot();
        }
        
        return null;
    }

    public String mapPlatformRootToBloombergRoot(String platform, String symbolRoot, boolean useRoot) {
        FuturesSymbol mappedSymbol = bloombergToPlatform.get(new FuturesSymbolKey(platform, symbolRoot));
       
        if (mappedSymbol == null && useRoot) {
            return symbolRoot;
        } else if (mappedSymbol == null) {
            return null;
        }
        return mappedSymbol.getBloombergSymbol();
    }
    
    public double lookupFuturesInboundPriceMultiplier(String platform, String futureRootSymbol) {
        FuturesSymbol mappedSymbol = bloombergToPlatform.get(new FuturesSymbolKey(platform, futureRootSymbol));
        
        if (mappedSymbol == null) {
            return 1.0d;
        }
        return mappedSymbol.getPriceMultiplier();
    }

    public BigDecimal lookupFuturesOutboundPriceMultiplier(String platform, String bloombergRoot) {
        FuturesSymbolMap mappedSymbol = bloombergRootToRicRoot.get(new FuturesSymbolKey(platform, bloombergRoot));
        
        if (mappedSymbol == null) {
            return null;
        }
        return mappedSymbol.getPriceMultiplier();
    }
    
    public String lookupFuturesProductCode(String platform, String futureRootSymbol) {
        FuturesSymbol mappedSymbol = bloombergToPlatform.get(new FuturesSymbolKey(platform, futureRootSymbol));
        
        if (mappedSymbol == null) {
            return "CMDT";
        }
        return mappedSymbol.getBloombergProductCode();
    }
}
