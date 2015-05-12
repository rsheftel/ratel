package com.fftw.positions.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.util.AbstractGenericCache;

/**
 * Cache for the fully specified key of Batch Position records
 */
public class BatchPositionCache extends AbstractGenericCache<PositionKey, BatchPosition> {

    public BatchPositionCache() {}

    public BatchPositionCache(Map<PositionKey, BatchPosition> newPositions) {
        super(newPositions);
    }

    public BatchPositionCache(Collection<BatchPosition> newPositions) {
        for (BatchPosition batchPosition : newPositions) {
            addBatchPosition(batchPosition);
        }
    }

    public BatchPosition getBatchPosition(PositionKey key) {
        return getCacheItem(key);
    }
    
    public BatchPosition addBatchPosition(BatchPosition op) {
        return addCacheItem(getKeyForItem(op), op);
    }

    public BatchPosition updateBatchPosition(BatchPosition op) {
        return addCacheItem(getKeyForItem(op), op);
    }

    public PositionKey getKeyForItem(BatchPosition batchPosition) {
        return new PositionKey(batchPosition.getSecurityId(), batchPosition.getProductCode(), batchPosition.getAccount(), batchPosition.getLevel1TagName(), batchPosition
            .getLevel2TagName(), batchPosition.getLevel3TagName(), batchPosition.getLevel4TagName(), batchPosition.getPrimeBroker(),
            batchPosition.getOnlineOpenPosition());
    }

    public PositionKey getKeyForItem(RtfOnlinePosition onlinePosition) {
        return new PositionKey(onlinePosition.getSecurityId(), onlinePosition.getProductCode(), onlinePosition.getAccount(), onlinePosition.getLevel1TagName(), onlinePosition
            .getLevel2TagName(), onlinePosition.getLevel3TagName(), onlinePosition.getLevel4TagName(), onlinePosition.getPrimeBroker(),
            onlinePosition.getOpenPosition());
    }
    
    public BatchPosition addOnlinePosition(RtfOnlinePosition onlinePosition) {
        BatchPosition batchPosition = getBatchPosition(onlinePosition);
        if (batchPosition == null) {
//            System.err.println("Adding online only record: " + onlinePosition);
            batchPosition = BatchPosition.valueOf(onlinePosition);
            addBatchPosition(batchPosition);
        } else {
            batchPosition.setOnlinePosition(onlinePosition);
        }

        return batchPosition;
    }
    public void addOnlinePositionAll(List<RtfOnlinePosition> onlinePositions) {
        for (RtfOnlinePosition onlinePosition : onlinePositions) {
            addOnlinePosition(onlinePosition);
        }
    }

    private BatchPosition getBatchPosition(RtfOnlinePosition onlinePosition) {
        PositionKey key = getKeyForItem(onlinePosition);
        
        return getCacheItem(key);
    }

    public int count() {
        return size();
    }

}