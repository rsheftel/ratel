package com.fftw.bloomberg.rtf;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a cache of cached items.
 * <p/>
 * The first level of caching has a less specific key than the second level.  This allows us to remember
 * what needs to be aggregated together when we pull the key out.
 * <p/>
 * The 'get' returns an aggregated value using the less specific key.
 */
public class OnlinePositionAggregateCache {

    private Map<PositionKey, OnlinePositionCache> cache = new HashMap<PositionKey, OnlinePositionCache>();

    public OnlinePositionAggregateCache() {

    }

    private OnlinePositionCache getLocalCacheItem(PositionKey key) {
        return cache.get(key);
    }

    private OnlinePositionCache addLocalCacheItem(PositionKey key, OnlinePositionCache cacheItem) {
        return cache.put(key, cacheItem);
    }

    public RtfOnlinePosition getOnlinePosition(PositionKey key) {

        OnlinePositionCache cacheToAggregate = cache.get(key);

        if (cacheToAggregate != null) {
            RtfOnlinePosition itemToReturn = null;
            for (RtfOnlinePosition op : cacheToAggregate.values()) {
                if (itemToReturn == null) {
                    itemToReturn = op;
                } else {
                    itemToReturn = itemToReturn.aggregate(op);
                }
            }
            return itemToReturn;
        }
        return null;
    }

    public RtfOnlinePosition addOnlinePosition(RtfOnlinePosition op) {

        PositionKey key = getKeyForItem(op);

        OnlinePositionCache aggregateItem = getLocalCacheItem(key);

        if (aggregateItem == null) {
            aggregateItem = new OnlinePositionCache();
            addLocalCacheItem(key, aggregateItem);
        }

        aggregateItem.addCacheItem(aggregateItem.getKeyForItem(op), op);
        return getOnlinePosition(key);
    }

    public Collection<RtfOnlinePosition> values() {
        List<RtfOnlinePosition> positions = new ArrayList<RtfOnlinePosition>();

        for (OnlinePositionCache positionCache : cache.values()) {
            positions.addAll(positionCache.aggregatedValues());
        }
        return positions;
    }

    public PositionKey getKeyForItem(RtfOnlinePosition value) {
        return new PositionKey(value.getSecurityId(), value.getAccount(), value.getLevel1TagName(),
                value.getLevel2TagName(), value.getLevel3TagName(), value.getLevel4TagName());
    }

}