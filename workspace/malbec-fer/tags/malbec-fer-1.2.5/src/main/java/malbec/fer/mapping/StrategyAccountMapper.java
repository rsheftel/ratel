package malbec.fer.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;


/**
 * Map the platform, strategy to account.
 * 
 * This uses the BADB.TRADING_STRATEGY table.
 * 
 */
class StrategyAccountMapper {

    // Start EntityManagerFactory
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    private Map<PlatformStrategy, PlatformStrategyAccount> psaMap = new HashMap<PlatformStrategy, PlatformStrategyAccount>();
    
    private Map<PlatformAccount, PlatformStrategyAccount> paMap = new HashMap<PlatformAccount, PlatformStrategyAccount>();
    
    public StrategyAccountMapper() {
        this(false);
    }

    public StrategyAccountMapper(boolean initalize) {
        if (initalize) {
            this.initialize(psaMap, paMap);
        }
    }

    public synchronized int initialize() {
        return initialize(psaMap, paMap);
    }
  
    @SuppressWarnings("unchecked")
    private synchronized int initialize(Map<PlatformStrategy, PlatformStrategyAccount> psaMap, Map<PlatformAccount, PlatformStrategyAccount> paMap) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<PlatformStrategyAccount> results;

        try {
            Query query = em.createQuery("from PlatformStrategyAccount");
            tx.begin();
            results = query.getResultList();
            tx.commit();
        } finally {
            em.close();
        }

        if (results == null) {
            return -1;
        }

        for (PlatformStrategyAccount psa : results) {
            // We may have bad data, ignore the rows as they are not used
            if (psa.getPlatform() != null && psa.getStrategy() != null && psa.getAccountType() != null) {
                psaMap.put(new PlatformStrategy(psa.getPlatform(), psa.getStrategy(), psa.getAccountType()), psa);
                paMap.put(new PlatformAccount(psa.getPlatform(), psa.getAccount()), psa);
            }
        }

        return results.size();
    }

    public synchronized int reload() {
        Map<PlatformStrategy, PlatformStrategyAccount> psaMap = new HashMap<PlatformStrategy, PlatformStrategyAccount>();
        Map<PlatformAccount, PlatformStrategyAccount> paMap = new HashMap<PlatformAccount, PlatformStrategyAccount>();
        int reloadCount = initialize(psaMap, paMap);
        this.psaMap = psaMap;
        this.paMap = paMap;
        
        return reloadCount;
    }
    
    /**
     * This is for testing purposes and not intended to be used.
     * 
     * @param platform
     * @param strategy
     * @param account
     * @param accountType
     */
    public synchronized void addMapping(String platform, String strategy, String accountType, String account) {
        PlatformStrategyAccount psa = new PlatformStrategyAccount(platform, strategy, account, accountType);
        psaMap.put(new PlatformStrategy(platform, strategy, accountType), psa);
        paMap.put(new PlatformAccount(platform, account), psa);
    }

    public synchronized String lookupAccount(String platform, String strategy, String accountType) {
        if (platform == null || strategy == null || accountType == null) {
            return null;
        }
        PlatformStrategyAccount psa = psaMap.get(new PlatformStrategy(platform, strategy, accountType));
        if (psa != null) {
            return psa.getAccount();
        }

        return null;
    }
    
    public synchronized String lookupAccountType(String platform, String account) {
        PlatformStrategyAccount psa = paMap.get(new PlatformAccount(platform, account));
        if (psa != null) {
            return psa.getAccountType();
        }

        return null;
    }
    
    private static class PlatformAccount {
        private String platform;
        private String account;
        
        private PlatformAccount(String p, String a) {
            this.platform = p.toUpperCase();
            this.account = a.toUpperCase();
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return platform.hashCode() + account.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PlatformAccount)) {
                return false;
            }

            PlatformAccount other = (PlatformAccount) obj;
            return platform.equals(other.platform) && account.equals(other.account);
        }
        
        public String toString() {
            return "platform=" + platform+", account=" + account; 
            
        }
    }

    private static class PlatformStrategy {
        private String platform;
        private String strategy;
        private String securityType;

        private PlatformStrategy(String p, String s, String st) {
            platform = p.toUpperCase();
            strategy = s.toUpperCase();
            securityType = st.toUpperCase();
        }

        /**
         * @return the platform
         */
        String getPlatform() {
            return platform;
        }

        /**
         * @return the strategy
         */
        String getStrategy() {
            return strategy;
        }

        /**
         * @return the securityType
         */
        String getSecurityType() {
            return securityType;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PlatformStrategy)) {
                return false;
            }

            PlatformStrategy other = (PlatformStrategy) obj;
            return platform.equals(other.platform) && strategy.equals(other.strategy)
                    && securityType.equals(other.securityType);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return platform.hashCode() + strategy.hashCode() + securityType.hashCode();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);

            sb.append("platform=").append(platform);
            sb.append(", strategy=").append(strategy);
            sb.append(", securityType=").append(securityType);

            return sb.toString();
        }
    }
}
