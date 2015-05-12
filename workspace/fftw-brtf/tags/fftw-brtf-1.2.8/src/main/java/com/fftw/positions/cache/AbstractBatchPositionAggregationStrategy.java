package com.fftw.positions.cache;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;
import com.fftw.util.IAggregationCacheStrategy;

/**
 * Base class for aggregating batch position records.
 * 
 * @deprecated Aggregating batch records without online records leads to errors, use a better method
 */
@Deprecated 
public abstract class AbstractBatchPositionAggregationStrategy implements
    IAggregationCacheStrategy<PositionKey, BatchPosition> {

    @Override
    public BatchPosition aggregate(BatchPosition augend, BatchPosition addend) {

        if (!canAggregate(augend, addend)) {
            throw new IllegalArgumentException("BatchPositions cannot be aggregated, keys do not match");
        }

        // Do the math
        return augend.aggregate(addend);
    }

    @Override
    public BatchPosition convertToAggregate(BatchPosition item) {
        return item;
    }

    protected static boolean isEmptyOrNull(String s) {
        return (s == null || s.length() == 0);
    }

    protected static boolean equal(String a, String b) {
        if (isEmptyOrNull(a) && isEmptyOrNull(b)) {
            return true;
        } else if (isEmptyOrNull(a) || isEmptyOrNull(b)) {
            return false;
        } else {
            return a.equals(b);
        }
    }

}
