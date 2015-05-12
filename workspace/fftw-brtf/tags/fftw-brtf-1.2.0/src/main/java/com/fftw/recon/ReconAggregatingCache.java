package com.fftw.recon;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.AbstractAggretableOnlinePositionCache;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.util.IAggregationStrategy;

public class ReconAggregatingCache extends AbstractAggretableOnlinePositionCache<OnlinePositionKey>{

    
    protected ReconAggregatingCache(
        IAggregationStrategy<OnlinePositionKey, RtfOnlinePosition> aggregationStrategy) {
        super(aggregationStrategy);
    }

    public ReconAggregatingCache() {
       this(new ReconOnlinePositionAggregationStrategy());
    }
}
