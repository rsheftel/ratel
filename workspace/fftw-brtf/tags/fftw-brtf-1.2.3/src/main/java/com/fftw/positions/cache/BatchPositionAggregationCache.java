package com.fftw.positions.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.util.IAggregationCacheStrategy;

/**
 * This provides a two-level cache of <cod>BatchPosition<code> objects that is the basis of
 * multiple implementations that need to aggregate the records differently.  
 * 
 * <code>BatchPosition</code> represent a snapshot of the current position.
 * 
 * This provides a way to aggregate the records and keep the original records intact.  
 * 
 * @param <AK>
 */
public class BatchPositionAggregationCache<AK> {

    private Map<AK, BatchPositionCache> cache = new HashMap<AK, BatchPositionCache>();

    private IAggregationCacheStrategy<AK, BatchPosition> aggregator;
    
    public BatchPositionAggregationCache(IAggregationCacheStrategy<AK, BatchPosition> aggregationStrategy) {
        aggregator = aggregationStrategy;
    }

    protected BatchPositionCache getLocalCacheItem(AK key) {
        return cache.get(key);
    }

    protected BatchPositionCache addLocalCacheItem(AK key, BatchPositionCache cacheItem) {
        return cache.put(key, cacheItem);
    }
    
    public BatchPosition getBatchPosition(AK key) {

        BatchPositionCache cacheToAggregate = cache.get(key);

        if (cacheToAggregate != null) {
            return aggregator.convertToAggregate(aggregateCache(cacheToAggregate));
        }
        return null;
    }

    private BatchPosition aggregateCache(BatchPositionCache cacheToAggregate) {
        
        BatchPosition itemToReturn = null;
        
        for (BatchPosition op : cacheToAggregate.values()) {
            if (itemToReturn == null) {
                itemToReturn = op;
            } else {
                itemToReturn = aggregator.aggregate(itemToReturn, op);
            }
        }
        return itemToReturn;
    }

    public Collection<BatchPosition> values() {
        
        List<BatchPosition> positions = new ArrayList<BatchPosition>(50);

        for (BatchPositionCache positionCache : cache.values()) {
            positions.add(aggregator.convertToAggregate(aggregateCache(positionCache)));
        }
        return positions;
    }
    
    public BatchPosition addBatchPosition(BatchPosition op) {
        AK key = getAggregateKeyForItem(op);

        BatchPositionCache aggregateItem = getLocalCacheItem(key);

        if (aggregateItem == null) {
            aggregateItem = new BatchPositionCache();
            addLocalCacheItem(key, aggregateItem);
        } /*else {
            System.out.println("Going to aggregate using key: " + key +"\nNew position: " + op);
        }*/

        aggregateItem.addCacheItem(aggregateItem.getKeyForItem(op), op);
        
        return getBatchPosition(key);
    }

    public int count() {
        return cache.size();
    }

    public AK getAggregateKeyForItem(BatchPosition item) {
        return aggregator.getAggregateKeyForItem(item);
    }

    public AK getAggregateKeyForKey(AK key) {
        return aggregator.getAggregateKeyForKey(key);
    }

    public Collection<AK> getKeysWithAggregates() {
        List<AK> keys = new LinkedList<AK>();

        for (BatchPositionCache positionCache : cache.values()) {
            if (positionCache.size() > 1) {
                Collection<BatchPosition> positions = positionCache.values();
                for (BatchPosition position : positions) {
                    keys.add(aggregator.getAggregateKeyForItem(position));
                    break;
                }
            }
        }
        return keys;
    }

}
