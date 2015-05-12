package com.fftw.positions.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.positions.Position;
import com.fftw.util.IAggregationCacheStrategy;

/**
 * Aggregates <code>Position<code>s using the specified cache aggregation strategy.
 * 
 */
public class PositionAggregationCache {

    private Map<PositionKey, BatchPositionCache> cache = new HashMap<PositionKey, BatchPositionCache>();

    private final IAggregationCacheStrategy<PositionKey, Position> aggregator;

    public PositionAggregationCache(IAggregationCacheStrategy<PositionKey, Position> aggregationStrategy) {
        aggregator = aggregationStrategy;
    }

    public PositionAggregationCache(IAggregationCacheStrategy<PositionKey, Position> aggregationStrategy,
        Collection<BatchPosition> batchPositions) {
        this(aggregationStrategy, batchPositions, Collections.<RtfOnlinePosition>emptyList());
    }

    public PositionAggregationCache(Collection<BatchPosition> batchPositions) {
        this(new DefaultPositionAggregationStrategy(), batchPositions);
    }

    public PositionAggregationCache() {
        this(Collections.<BatchPosition> emptyList());
    }

    public PositionAggregationCache(IAggregationCacheStrategy<PositionKey, Position> aggregationStrategy,
        Collection<BatchPosition> batchPositions, Collection<RtfOnlinePosition> onlinePositions) {
        aggregator = aggregationStrategy;
        
        addBatchPositions(batchPositions);
        addOnlinePositions(onlinePositions);
    }

    public void addBatchPositions(Collection<BatchPosition> batchPositions) {
        for (BatchPosition batchPosition : batchPositions) {
            if (batchPosition.getTicker().equals("XLB") || batchPosition.getTicker().equals("TYH9P")) {
                System.err.println(batchPosition);
            }
            addBatchPosition(batchPosition);
        }
    }

    public void addOnlinePositions(Collection<RtfOnlinePosition> onlinePositions) {
        for (RtfOnlinePosition onlinePosition : onlinePositions) {
            addOnlinePosition(onlinePosition);
        }
    }

    protected BatchPositionCache getLocalCacheItem(PositionKey key) {
        return cache.get(key);
    }

    protected BatchPositionCache addLocalCacheItem(PositionKey key, BatchPositionCache cacheItem) {
        return cache.put(key, cacheItem);
    }

    public Position getPosition(PositionKey key) {

        BatchPositionCache cacheToAggregate = cache.get(key);

        if (cacheToAggregate != null) {
            return aggregator.convertToAggregate(aggregateCache(cacheToAggregate));
        }
        return null;
    }

    private Position aggregateCache(BatchPositionCache cacheToAggregate) {

        Position itemToReturn = null;

        for (BatchPosition op : cacheToAggregate.values()) {
            if (itemToReturn == null) {
                itemToReturn = op.getPosition();
            } else {
                itemToReturn = aggregator.aggregate(itemToReturn, op.getPosition());
            }
        }
        return itemToReturn;
    }

    public Collection<Position> values() {

        List<Position> positions = new LinkedList<Position>();

        for (BatchPositionCache positionCache : cache.values()) {
            positions.add(aggregator.convertToAggregate(aggregateCache(positionCache)));
        }
        return positions;
    }

    public Position addBatchPosition(BatchPosition batchPosition) {
        PositionKey key = getAggregateKeyForItem(batchPosition);

        BatchPositionCache aggregateItem = getLocalCacheItem(key);

        if (aggregateItem == null) {
            aggregateItem = new BatchPositionCache();
            addLocalCacheItem(key, aggregateItem);
        } 

        aggregateItem.addBatchPosition(batchPosition);

        return getPosition(key);
    }

    public Position addOnlinePosition(RtfOnlinePosition onlinePosition) {
        PositionKey key = getAggregateKeyForItem(onlinePosition);

        BatchPositionCache aggregateItem = getLocalCacheItem(key);

        if (aggregateItem == null) {
            aggregateItem = new BatchPositionCache();
            addLocalCacheItem(key, aggregateItem);
        }

        aggregateItem.addOnlinePosition(onlinePosition);

        return getPosition(key);
    }

    public int count() {
        return cache.size();
    }

    public PositionKey getAggregateKeyForItem(BatchPosition item) {
        return aggregator.getAggregateKeyForItem(item.getPosition());
    }

    public PositionKey getAggregateKeyForItem(RtfOnlinePosition item) {
        BatchPosition batchPosition = BatchPosition.valueOf(item);

        return aggregator.getAggregateKeyForItem(batchPosition.getPosition());
    }

    public PositionKey getAggregateKeyForKey(PositionKey key) {
        return aggregator.getAggregateKeyForKey(key);
    }

    Collection<PositionKey> getKeysWithAggregates() {
        List<PositionKey> keys = new LinkedList<PositionKey>();

        for (BatchPositionCache positionCache : cache.values()) {
            if (positionCache.size() > 1) {
                Collection<BatchPosition> positions = positionCache.values();
                for (BatchPosition position : positions) {
                    keys.add(aggregator.getAggregateKeyForItem(position.getPosition()));
                    break;
                }
            }
        }
        return keys;
    }

}
