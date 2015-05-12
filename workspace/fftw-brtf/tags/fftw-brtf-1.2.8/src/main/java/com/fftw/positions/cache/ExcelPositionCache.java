package com.fftw.positions.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;

/**
 * Keep track of the batch and online positions using the Excel Aggregation Strategy
 */
public class ExcelPositionCache extends AbstractPositionCache {

    public ExcelPositionCache() {
        super(new OnlinePositionAggregationCache<OnlinePositionKey>(new ExcelOnlineAggregationStrategy()),
            new BatchPositionAggregationCache<PositionKey>(new ExcelBatchAggregationStrategy()));

    }

    public ExcelPositionCache(Collection<BatchPosition> batchPositions) {
        this(batchPositions, Collections.<RtfOnlinePosition> emptyList());
    }

    public ExcelPositionCache(Collection<BatchPosition> batchPositions,
        Collection<RtfOnlinePosition> onlinePositions) {

        this();
        
        addBatchPositionAll(batchPositions);
        addOnlinePositionAll(onlinePositions);
    }

    /* (non-Javadoc)
     * @see com.fftw.positions.cache.AbstractPositionCache#addBatchPositionAll(java.util.Collection)
     */
    @Override
    public void addBatchPositionAll(Collection<BatchPosition> batchPositions) {
        List<BatchPosition> copies = new ArrayList<BatchPosition>(batchPositions.size());
        
        for (BatchPosition batchPosition : batchPositions) {
            BatchPosition copy = batchPosition.copy();
            copy.setUseExcelKey(true);
            copies.add(copy);
        }
        super.addBatchPositionAll(copies);
    }

    /* (non-Javadoc)
     * @see com.fftw.positions.cache.AbstractPositionCache#addOnlinePositionAll(java.util.Collection)
     */
    @Override
    public void addOnlinePositionAll(Collection<RtfOnlinePosition> onlinePositions) {
        List<RtfOnlinePosition> copies = new ArrayList<RtfOnlinePosition>(onlinePositions.size());

        for (RtfOnlinePosition onlinePosition : onlinePositions) {
            RtfOnlinePosition copy = onlinePosition.copy();
            copy.setUseExcelKey(true);
            copies.add(copy);
        }

        super.addOnlinePositionAll(copies);
    }

    /* (non-Javadoc)
     * @see com.fftw.positions.cache.AbstractPositionCache#addBatchPosition(com.fftw.bloomberg.batch.messages.BatchPosition)
     */
    @Override
    public BatchPosition addBatchPosition(BatchPosition batchPosition) {
        BatchPosition copy = batchPosition.copy();
        copy.setUseExcelKey(true);
        
        return super.addBatchPosition(copy);
    }

    /* (non-Javadoc)
     * @see com.fftw.positions.cache.AbstractPositionCache#addOnlinePosition(com.fftw.bloomberg.rtf.messages.RtfOnlinePosition)
     */
    @Override
    public RtfOnlinePosition addOnlinePosition(RtfOnlinePosition onlinePosition) {
        RtfOnlinePosition copy = onlinePosition.copy();
        copy.setUseExcelKey(true);
        
        return super.addOnlinePosition(copy);
    }
}
