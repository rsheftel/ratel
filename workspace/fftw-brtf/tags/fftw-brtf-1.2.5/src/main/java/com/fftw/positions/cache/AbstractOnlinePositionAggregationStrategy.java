package com.fftw.positions.cache;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.util.IAggregationCacheStrategy;

/**
 * Base class for aggregating online position records.
 * 
 */
public abstract class AbstractOnlinePositionAggregationStrategy implements
    IAggregationCacheStrategy<OnlinePositionKey, RtfOnlinePosition> {

    @Override
    public RtfOnlinePosition aggregate(RtfOnlinePosition augend, RtfOnlinePosition addend) {

        if (!canAggregate(augend, addend)) {
            throw new IllegalArgumentException("OnlinePositions cannot be aggregated, keys do not match");
        }

        // Do the math
        return augend.aggregate(addend);
    }

    @Override
    public RtfOnlinePosition convertToAggregate(RtfOnlinePosition item) {
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
