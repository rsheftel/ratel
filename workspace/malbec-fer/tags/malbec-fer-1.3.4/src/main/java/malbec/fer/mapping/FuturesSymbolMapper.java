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

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    // the mapping tables, bloomberg to data; platform receiving to data; platform sending to data
    private Map<FuturesSymbolKey, FuturesSymbol> withPlatformReceivingKey = new HashMap<FuturesSymbolKey, FuturesSymbol>();
    private Map<FuturesSymbolKey, FuturesSymbol> withPlatformSendingKey = new HashMap<FuturesSymbolKey, FuturesSymbol>();
    private Map<FuturesSymbolKey, FuturesSymbol> withBloombergKey = new HashMap<FuturesSymbolKey, FuturesSymbol>();

    public FuturesSymbolMapper() {
        this(false);
    }

    public FuturesSymbolMapper(boolean initalize) {
        if (initalize) {
            initialize(withBloombergKey, withPlatformReceivingKey);
        }
    }

    public synchronized int initialize() {
        int inboundCount = initialize(withBloombergKey, withPlatformReceivingKey);
        
        return inboundCount;
    }

    @SuppressWarnings("unchecked")
    private synchronized int initialize(Map<FuturesSymbolKey, FuturesSymbol> bbMap, Map<FuturesSymbolKey, FuturesSymbol> platformMap) {
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
            if (fs.getPlatform() != null && fs.getPlatformReceivingRoot() != null && fs.getBloombergRoot() != null) {
                bbMap.put(new FuturesSymbolKey(fs.getPlatform(), fs.getBloombergRoot()), fs);
                platformMap.put(new FuturesSymbolKey(fs.getPlatform(), fs.getPlatformReceivingRoot()), fs);
            }
        }

        return results.size();
    }

    public synchronized int reload() {
        Map<FuturesSymbolKey, FuturesSymbol> bbMap = new HashMap<FuturesSymbolKey, FuturesSymbol>();
        Map<FuturesSymbolKey, FuturesSymbol> platformMap = new HashMap<FuturesSymbolKey, FuturesSymbol>();

        int reloadCount = initialize(bbMap, platformMap);
        
        withBloombergKey = bbMap;
        withPlatformReceivingKey = platformMap;

        return reloadCount;
    }

    /**
     * This is for testing purposes and not intended to be used.
     *  
     * @param platform
     * @param bloombergRoot
     * @param platformRoot
     */
    public void addBloombergMapping(String platform, String bloombergRoot, String platformReceivingRoot, String platformSendingRoot, BigDecimal priceMultiplier) {
        FuturesSymbol fs = new FuturesSymbol(platform, platformReceivingRoot, bloombergRoot, platformSendingRoot, priceMultiplier);
        // add to the bloomberg -> other mapping
        withBloombergKey.put(new FuturesSymbolKey(platform, bloombergRoot), fs);
        // add to the platform receiving-> other mapping
        withPlatformReceivingKey.put(new FuturesSymbolKey(platform, platformReceivingRoot), fs);
        // add to the platform sending -> other mapping
        withPlatformSendingKey.put(new FuturesSymbolKey(platform, platformSendingRoot), fs);

    }

    private static class FuturesSymbolKey {
        String platformId;
        String root; 

        public FuturesSymbolKey(String platform, String root) {
            this.platformId = platform.toUpperCase();
            this.root = root.toUpperCase();
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
            return platformId.equals(other.platformId) && root.equals(other.root);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return platformId.hashCode() *17 + root.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            
            sb.append("platformId=").append(platformId);
            sb.append(", root=").append(root);
            
            return sb.toString();
        }
    }

    /**
     * Using the platform, platform root and the month year return the Bloomberg symbol without
     * a yellow key.
     * 
     * @param platform
     * @param platformRoot
     * @param monthYear
     * @return
     */
    public String mapPlatformRootToBloombergSymbol(String platform, String platformRoot, String monthYear) {
        String bloombergRoot = lookupBloombergRoot(platform, platformRoot, true); 
       
        return combineRootMaturityMonthYear(bloombergRoot, monthYear);
    }
    
    /**
     * Given the platform and the Bloomberg root, return the corresponding platform root.
     * 
     * @param platform
     * @param bloombergRoot
     * @param defaultValue
     * @return
     */
    public String lookupPlatformRoot(String platform, String bloombergRoot, String defaultValue) {
        FuturesSymbol mappedSymbol = withBloombergKey.get(new FuturesSymbolKey(platform, bloombergRoot));
        
        if (mappedSymbol != null) {
            return mappedSymbol.getPlatformReceivingRoot();
        }
        
        return defaultValue;
    }

    /**
     * Given a platform and the platform root lookup the corresponding Bloomberg root, it none
     * found, return the platform symbol or null.
     * 
     * @param platform
     * @param platformRoot
     * @param useRoot - if true, return the platform symbol if no mapping, otherwise return null
     * @return
     */
    public String lookupBloombergRoot(String platform, String platformRoot, boolean useRoot) {
        FuturesSymbol mappedSymbol = withPlatformReceivingKey.get(new FuturesSymbolKey(platform, platformRoot));
       
        if (mappedSymbol == null && useRoot) {
            return platformRoot;
        } else if (mappedSymbol == null) {
            return null;
        }
        return mappedSymbol.getBloombergRoot();
    }
    
    /**
     * Given the platform and Bloomberg root, return the corresponding price multiplier to convert
     * a price from the platform value to a Bloomberg price.
     * 
     * @param platform
     * @param bbRoot
     * @return
     */
    public BigDecimal lookupToBloombergPriceMultiplier(String platform, String bbRoot) {
        FuturesSymbol mappedSymbol = withBloombergKey.get(new FuturesSymbolKey(platform, bbRoot));
        
        if (mappedSymbol == null) {
            return BigDecimal.ONE;
        }
        return mappedSymbol.getPriceMultiplier();
    }

    /**
     * Given the platform and the Bloomberg root, return the corresponding price multiplier to
     * convert the Bloomberg price to the platform price.
     * 
     * @param platform
     * @param bloombergRoot
     * @return
     */
    public BigDecimal lookupToPlatformPriceMultiplier(String platform, String bloombergRoot) {
        return BigDecimal.ONE.divide(lookupToBloombergPriceMultiplier(platform, bloombergRoot));
    }

    public String lookupPlatformSendingRoot(String platform, String bloombergRoot, boolean returnAsDefault) {
        FuturesSymbol mappedSymbol = withBloombergKey.get(new FuturesSymbolKey(platform, bloombergRoot));
        
        if (mappedSymbol == null && returnAsDefault) {
            return bloombergRoot;
        }
        
        if (mappedSymbol == null) {
            return null;
        }
        
        return mappedSymbol.getPlatformSendingRoot();
    }

}
