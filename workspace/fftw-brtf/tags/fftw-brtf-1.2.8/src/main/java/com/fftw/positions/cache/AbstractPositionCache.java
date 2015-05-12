package com.fftw.positions.cache;

import java.util.Collection;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;

public class AbstractPositionCache {

    protected OnlinePositionAggregationCache<OnlinePositionKey> onlineCache;

    protected BatchPositionAggregationCache<PositionKey> batchCache;

    protected AbstractPositionCache(OnlinePositionAggregationCache<OnlinePositionKey> onlineCache,
        BatchPositionAggregationCache<PositionKey> batchCache) {
        this.onlineCache = onlineCache;
        this.batchCache = batchCache;
    }

    public int batchCount() {
        return batchCache.count();
    }

    public BatchPosition getBatchPosition(PositionKey positionKey) {
        PositionKey strategyKey = batchCache.getAggregateKeyForKey(positionKey);
        System.err.println(strategyKey.hashCode());
        return batchCache.getBatchPosition(strategyKey);
    }

    public Collection<BatchPosition> batchValues() {
        return batchCache.values();
    }

    public BatchPosition addBatchPosition(BatchPosition batchPosition) {
        return batchCache.addBatchPosition(batchPosition);
    }

    public void addOnlinePositionAll(Collection<RtfOnlinePosition> onlinePositions) {
        for (RtfOnlinePosition onlinePosition : onlinePositions) {
            onlineCache.addOnlinePosition(onlinePosition);
        }
    }

    public void addBatchPositionAll(Collection<BatchPosition> batchPositions) {
        for (BatchPosition batchPosition : batchPositions) {
            batchCache.addBatchPosition(batchPosition);
        }
    }

    public RtfOnlinePosition getOnlinePosition(OnlinePositionKey key) {
        OnlinePositionKey strategyKey = onlineCache.getAggregateKeyForKey(key);
        return onlineCache.getOnlinePosition(strategyKey);
    }

    public Collection<RtfOnlinePosition> onlineValues() {
        return onlineCache.values();
    }

    public int onlineCount() {
        return onlineCache.count();
    }

    public RtfOnlinePosition addOnlinePosition(RtfOnlinePosition onlinePosition) {
        return onlineCache.addOnlinePosition(onlinePosition);
    }

    public Collection<PositionKey> getBatchKeysWithAggregates() {
        return batchCache.getKeysWithAggregates();
    }

    public Collection<OnlinePositionKey> getOnlineKeysWithAggregates() {
        return onlineCache.getKeysWithAggregates();
    }
    
    public BatchPosition getBatchPosition(RtfOnlinePosition onlinePosition) {
        OnlinePositionKey opk = onlineCache.getAggregateKeyForItem(onlinePosition);
    
        PositionKey batchKey = new PositionKey(opk.getSecurityId(), opk.getProductCode(), opk.getAccount(),
            opk.getLevel1TagName(), opk.getLevel2TagName(), opk.getLevel3TagName(), opk.getLevel4TagName(),
            opk.getPrimeBroker(), opk.getOpenPosition());
    
        return getBatchPosition(batchKey);
    }



}
