package malbec.fer.mapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.joda.time.LocalDate;

public class ShortSellItemMapper {

    // Start EntityManagerFactory
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    private Map<String, ShortSellItem> shortSellMap = new HashMap<String, ShortSellItem>();

    public ShortSellItemMapper() {
        this(false);
    }

    public ShortSellItemMapper(boolean initalize) {
        if (initalize) {
            initialize(new LocalDate(), shortSellMap);
        }
    }

    public synchronized int initialize(LocalDate dateToLoad) {
        return initialize(dateToLoad, shortSellMap);
    }

    @SuppressWarnings("unchecked")
    private synchronized int initialize(LocalDate dateToLoad, Map<String, ShortSellItem> newShortSellMap) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<ShortSellItem> results;

        try {
            Query query = em.createQuery("from ShortSellItem where shareDate=:shareDate");
            tx.begin();
            query.setParameter("shareDate", dateToLoad.toDateMidnight().toDate());
            results = query.getResultList();
            tx.commit();
        } finally {
            em.close();
        }

        if (results == null) {
            return -1;
        }

        for (ShortSellItem shortSellItem : results) {
            newShortSellMap.put(createMapKey(shortSellItem), shortSellItem);
        }

        return results.size();
    }

    private String createMapKey(ShortSellItem shortSellItem) {
        return shortSellItem.getSource().toUpperCase() + "-" + shortSellItem.getSymbol().toUpperCase();
    }

    private String createMapKey(String primeBroker, String ticker) {
        return mapPrimeBrokerToSource(primeBroker) + "-" + ticker;
    }

    public synchronized int reload() {
        return reload(new LocalDate());
    }
    
    synchronized int reload(LocalDate dateToLoad) {
        Map<String, ShortSellItem> newMap = new HashMap<String, ShortSellItem>();

        int reloadCount = initialize(dateToLoad, newMap);
        this.shortSellMap = newMap;

        return reloadCount;
    }

    public BigDecimal sharesToShort(String primeBroker, String ticker) {
        String key = createMapKey(primeBroker, ticker);
        ShortSellItem item = shortSellMap.get(key);

        if (item == null) {
            return BigDecimal.ZERO;
        }

        return item.getQuantity();
    }

    public BigDecimal add(String primeBroker, String ticker, BigDecimal additionalShares) {
        String key = createMapKey(primeBroker, ticker);
        
        ShortSellItem item = shortSellMap.get(key);
        
        if (item == null) {
            item = new ShortSellItem(mapPrimeBrokerToSource(primeBroker), ticker, additionalShares);
            shortSellMap.put(key, item);
            
            return BigDecimal.ZERO;
        } else {
            BigDecimal previousShares = item.getQuantity();
            
            item.setQuantity(previousShares.add(additionalShares));
            
            return previousShares;
        }
    }

    private String mapPrimeBrokerToSource(String primeBroker) {
        if (primeBroker.equals("MFPB")) {
            return "MAN";
        }
        
        return primeBroker;
    }

    public BigDecimal subtract(String primeBroker, String ticker, BigDecimal shares) {
        String key = createMapKey(primeBroker, ticker);
        
        ShortSellItem item = shortSellMap.get(key);
        
        if (item == null) {
            item = new ShortSellItem(mapPrimeBrokerToSource(primeBroker), ticker, shares.negate());
            shortSellMap.put(key, item);
            
            return BigDecimal.ZERO;
        } else {
            BigDecimal previousShares = item.getQuantity();
            
            item.setQuantity(previousShares.subtract(shares));
            
            return previousShares;
        }
    }
}
