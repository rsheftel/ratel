package com.fftw.positions.cache;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;

public class ReconAggregationStrategy extends ExcelOnlineAggregationStrategy {

    @Override
    public boolean canAggregate(RtfOnlinePosition augend, RtfOnlinePosition addend) {
        return augend.getSecurityId().equals(addend.getSecurityId())
            && augend.getAccount().equals(addend.getAccount());
    }

    @Override
    public OnlinePositionKey getAggregateKeyForItem(RtfOnlinePosition op) {
        return new ReconOnlineRecordKey(op.getReconBloombergId(), op.getProductCode(), op.getAccount());
    }
}
