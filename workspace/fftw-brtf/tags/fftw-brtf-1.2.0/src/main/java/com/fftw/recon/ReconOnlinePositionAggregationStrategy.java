package com.fftw.recon;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePositionAggregationStrategy;

public class ReconOnlinePositionAggregationStrategy extends RtfOnlinePositionAggregationStrategy {

    @Override
    public boolean canAggregate(RtfOnlinePosition augend, RtfOnlinePosition addend) {
        return augend.getSecurityId().equals(addend.getSecurityId())
            && augend.getAccount().equals(addend.getAccount());
    }

    @Override
    public OnlinePositionKey getAggregateKeyForItem(RtfOnlinePosition op) {
        return new OnlinePositionKey(op.getAccount(), op.getReconBloombergId());
    }
}
