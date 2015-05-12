package com.fftw.positions.cache;

import java.util.Collection;
import java.util.Collections;

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
}
