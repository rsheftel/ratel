package malbec.fer.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import malbec.util.DateTimeUtil;

import org.joda.time.LocalDate;

public class FuturesSymbolMapper {

    // Start EntityManagerFactory
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    private Map<FuturesSymbolKey, FuturesSymbol> platformToBloomberg = new HashMap<FuturesSymbolKey, FuturesSymbol>();
    private Map<FuturesSymbolKey, FuturesSymbol> bloombergToPlatform = new HashMap<FuturesSymbolKey, FuturesSymbol>();

    public FuturesSymbolMapper() {
        this(false);
    }

    public FuturesSymbolMapper(boolean initalize) {
        if (initalize) {
            this.initialize(platformToBloomberg);
        }
    }

    public synchronized int initialize() {
        return initialize(platformToBloomberg);
    }

    @SuppressWarnings("unchecked")
    private synchronized int initialize(Map<FuturesSymbolKey, FuturesSymbol> psaMap) {
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
                psaMap.put(new FuturesSymbolKey(fs.getPlatform(), fs.getBloombergSymbol()), fs);
                
                bloombergToPlatform.put(new FuturesSymbolKey(fs.getPlatform(), fs.getPlatformSymbol()), fs);
            }
        }

        return results.size();
    }

    public synchronized int reload() {
        Map<FuturesSymbolKey, FuturesSymbol> psaMap = new HashMap<FuturesSymbolKey, FuturesSymbol>();

        int reloadCount = initialize(psaMap);
        this.platformToBloomberg = psaMap;

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
    public void addMapping(String platform, String bloombergSymbol, String platformSymbol) {
        platformToBloomberg.put(new FuturesSymbolKey(platform, bloombergSymbol), new FuturesSymbol(platform,
                platformSymbol, bloombergSymbol));
    }

    public int codeToMonth(char monthCode) {
        switch (monthCode) {
            case 'F': // January
                return 1;
            case 'G': // February
                return 2;
            case 'H': // March
                return 3;
            case 'J': // April
                return 4;
            case 'K': // May
                return 5;
            case 'M': // June
                return 6;
            case 'N': // July
                return 7;
            case 'Q': // August
                return 8;
            case 'U': // September
                return 9;
            case 'V': // October
                return 10;
            case 'X': // November
                return 11;
            case 'Z': // December
                return 12;
            default:
                throw new IllegalArgumentException("Invalid trade trade month code for futures trade: " + monthCode);
        }
    }
    
    public char monthToCode(int month) {
        switch (month) {
            case 1: // January
                return 'F';
            case 2: // February
                return 'G';
            case 3: // March
                return 'H';
            case 4: // April
                return 'J';
            case 5: // May
                return 'K';
            case 6: // June
                return 'M';
            case 7: // July
                return 'N';
            case 8: // August
                return 'Q';
            case 9: // September
                return 'U';
            case 10: // October
                return 'V';
            case 11: // November
                return 'X';
            case 12: // December
                return 'Z';
            default:
                throw new IllegalArgumentException("Invalid trade trade month for futures trade: " + month);
        }
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

    public String mapToBloombergSymbol(String platform, String symbolRoot, String monthYear) {
        String bloombergRoot = mapPlatformToBloomberg(platform, symbolRoot, true); 
        LocalDate ld = DateTimeUtil.getLocalDate(monthYear + "01");

        int year = ld.getYearOfCentury();
        // Bloomberg requires single digit years
        if (year >= 10) {
            year = year - (year / 10) * 10;
        }
        int month = ld.getMonthOfYear();
        char monthCode = monthToCode(month);
        
        return bloombergRoot+monthCode+ year;
    }
    
    public String extractMaturityMonthFromSymbol(String symbol) {
        String monthStr = symbol.substring(symbol.length() - 2, symbol.length() - 1);
        String month = String.format("%02d", codeToMonth(monthStr.charAt(0)));
        String yearStr = symbol.substring(symbol.length()-1, symbol.length());
        
        int year = 2000 + Integer.parseInt(yearStr);
        
        return String.valueOf(year) + month;
    }
    
    
    public String mapBloombergSymbolToPlatform(String platform, String symbolRoot, String defaultValue) {
        FuturesSymbol mappedSymbol = platformToBloomberg.get(new FuturesSymbolKey(platform, symbolRoot));
        
        if (mappedSymbol != null) {
            return mappedSymbol.getPlatformSymbol();
        }
        
        return defaultValue;
    }

    public String mapPlatformToBloomberg(String platform, String symbolRoot, boolean useRoot) {
        FuturesSymbol mappedSymbol = bloombergToPlatform.get(new FuturesSymbolKey(platform, symbolRoot));
       
        if (mappedSymbol == null && useRoot) {
            return symbolRoot;
        } else if (mappedSymbol == null) {
            return null;
        }
        return mappedSymbol.getBloombergSymbol();
    }
    
    public double lookupFuturesPriceMultipler(String platform, String futureRootSymbol) {
        FuturesSymbol mappedSymbol = bloombergToPlatform.get(new FuturesSymbolKey(platform, futureRootSymbol));
        
        if (mappedSymbol == null) {
            return 1.0d;
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
