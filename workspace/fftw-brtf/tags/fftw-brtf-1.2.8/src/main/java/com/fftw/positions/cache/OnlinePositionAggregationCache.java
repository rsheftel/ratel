package com.fftw.positions.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.util.IAggregationCacheStrategy;

/**
 * This provides a two-level cache of <cod>RtfOnlinePosition<code> objects that is the basis of
 * multiple implementations that need to aggregate the records differently.  
 * 
 * <code>RtfOnlinePosition</code> represent a snapshot of the current position and not the change to the
 * position. This is both good and bad. It is good when you don't want to perform any math on the record, and
 * bad when you need to aggregate positions at a higher level.
 * 
 * This provides a way to aggregate the records and keep the original records intact.
 * 
 * @param <AK>
 */
public class OnlinePositionAggregationCache<AK> {

    private Map<AK, OnlinePositionCache> cache = new HashMap<AK, OnlinePositionCache>();

    private IAggregationCacheStrategy<AK, RtfOnlinePosition> aggregator;

    public OnlinePositionAggregationCache(IAggregationCacheStrategy<AK, RtfOnlinePosition> aggregationStrategy) {
        aggregator = aggregationStrategy;
    }

    protected OnlinePositionCache getLocalCacheItem(AK key) {
        return cache.get(key);
    }

    protected OnlinePositionCache addLocalCacheItem(AK key, OnlinePositionCache cacheItem) {
        return cache.put(key, cacheItem);
    }

    public RtfOnlinePosition getOnlinePosition(AK key) {

        OnlinePositionCache cacheToAggregate = cache.get(key);

        if (cacheToAggregate != null) {
            return aggregator.convertToAggregate(aggregateCache(cacheToAggregate));
        }
        return null;
    }

    private RtfOnlinePosition aggregateCache(OnlinePositionCache cacheToAggregate) {

        RtfOnlinePosition itemToReturn = null;

        for (RtfOnlinePosition op : cacheToAggregate.values()) {
            if (itemToReturn == null) {
                // TODO should this be the converToAggregate call?
                itemToReturn = op;
            } else {
                itemToReturn = aggregator.aggregate(itemToReturn, op);
            }
        }
        return itemToReturn;
    }

    public Collection<RtfOnlinePosition> values() {
        List<RtfOnlinePosition> positions = new LinkedList<RtfOnlinePosition>();

        for (OnlinePositionCache positionCache : cache.values()) {
            positions.add(aggregator.convertToAggregate(aggregateCache(positionCache)));
        }
        return positions;
    }

    public RtfOnlinePosition addOnlinePosition(RtfOnlinePosition op) {
        AK key = getAggregateKeyForItem(op);

        OnlinePositionCache aggregateItem = getLocalCacheItem(key);

        if (aggregateItem == null) {
            aggregateItem = new OnlinePositionCache();
            addLocalCacheItem(key, aggregateItem);
        }

        aggregateItem.addCacheItem(aggregateItem.getKeyForItem(op), op);
        return getOnlinePosition(key);
    }

    public int count() {
        return cache.size();
    }

    public AK getAggregateKeyForItem(RtfOnlinePosition op) {
        return aggregator.getAggregateKeyForItem(op);
    }

    public AK getAggregateKeyForKey(AK key) {
        return aggregator.getAggregateKeyForKey(key);
    }

    public Collection<AK> getKeysWithAggregates() {
        List<AK> keys = new LinkedList<AK>();

        for (OnlinePositionCache positionCache : cache.values()) {
            if (positionCache.size() > 1) {
                Collection<RtfOnlinePosition> positions = positionCache.values();
                for (RtfOnlinePosition position : positions) {
                    keys.add(aggregator.getAggregateKeyForItem(position));
                    break;
                }
            }
        }
        return keys;
    }

}
