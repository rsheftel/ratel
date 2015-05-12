package malbec.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import malbec.fer.NamedThreadFactory;
import malbec.fer.TaskService;

/**
 * Map the platform, strategy to account.
 * 
 * This uses the BADB.TRADING_STRATEGY table.
 * 
 */
public class StrategyAccountMapper {

    final private Logger log = LoggerFactory.getLogger(getClass());
    
    // Start EntityManagerFactory
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    private Map<PlatformStrategy, PlatformStrategyAccount> psaMap = new HashMap<PlatformStrategy, PlatformStrategyAccount>();

    static {
        TaskService.getInstance().addExecutor("SAM",
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("SAM-Reload")));
    }
    
    public StrategyAccountMapper() {
        this(false);
    }

    public StrategyAccountMapper(boolean initalize) {
        if (initalize) {
            this.initialize(psaMap);
        }
        // Schedule the reload to run every 15 minutes
        
        DateTime now = new DateTime();
        int minutes = now.getMinuteOfHour();
        int normalizedMinutes = minutes % 15;
        int minutesToWait = 15 - normalizedMinutes;
        
        ScheduledExecutorService executor = (ScheduledExecutorService) TaskService.getInstance().getExecutor("SAM");
        
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("SAM reload started");
                reload();
                log.info("SAM reload finished");
            }

        }, minutesToWait, 15, TimeUnit.MINUTES);

        log.info("Scheduled SAM reload timer to start in " + minutesToWait +" minutes");
    }

    public synchronized int reload() {
        Map<PlatformStrategy, PlatformStrategyAccount> psaMap = new HashMap<PlatformStrategy, PlatformStrategyAccount>();
        
        int reloadCount = initialize(psaMap);
        this.psaMap = psaMap;
        
        return reloadCount;
    }
    
    @SuppressWarnings("unchecked")
    public synchronized int initialize() {
        return initialize(psaMap);
    }
    
    @SuppressWarnings("unchecked")
    private synchronized int initialize(Map<PlatformStrategy, PlatformStrategyAccount> psaMap) {
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
            if (psa.platform != null && psa.strategy != null && psa.accountType != null) {
                psaMap.put(new PlatformStrategy(psa.platform, psa.strategy, psa.accountType), psa);
            }
        }

        return results.size();
    }

    /**
     * This is for testing purposes and not intended to be used.
     * 
     * @param platform
     * @param strategy
     * @param account
     * @param accountType
     */
    public void addMapping(String platform, String strategy, String accountType, String account) {
        psaMap.put(new PlatformStrategy(platform, strategy, accountType), new PlatformStrategyAccount(
                platform, strategy, account, accountType));
    }

    public synchronized String lookupAccount(String platform, String strategy, String accountType) {
        if (platform == null || strategy == null || accountType == null) {
            return null;
        }
        PlatformStrategyAccount psa = psaMap.get(new PlatformStrategy(platform, strategy, accountType));
        if (psa != null) {
            return psa.account;
        }

        return null;
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
