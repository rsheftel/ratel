package com.fftw.bloomberg.rtf;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePositionAggregationStrategy;
import com.fftw.util.IAggregationStrategy;

/**
 * This is a cache of cached items.
 * <p/>
 * The first level of caching has a less specific key than the second level.  This allows us to remember
 * what needs to be aggregated together when we pull the key out.
 * <p/>
 * The 'get' returns an aggregated value using the less specific key.
 */
public class OnlinePositionAggregateCache extends AbstractAggretableOnlinePositionCache<OnlinePositionKey>{

    //private Map<PositionKey, OnlinePositionCache> cache = new HashMap<PositionKey, OnlinePositionCache>();

    protected OnlinePositionAggregateCache(IAggregationStrategy<OnlinePositionKey, RtfOnlinePosition> aggregationStrategy) {
        super(aggregationStrategy);
    }

    public OnlinePositionAggregateCache() {
        this(new RtfOnlinePositionAggregationStrategy());
    }

}