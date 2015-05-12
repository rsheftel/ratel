package com.fftw.positions.cache;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;

public class AccountStrategyAggregationStrategy extends ExcelOnlineAggregationStrategy {

    @Override
    public boolean canAggregate(RtfOnlinePosition augend, RtfOnlinePosition addend) {
        return augend.getSecurityId().equals(addend.getSecurityId())
            && augend.getAccount().equals(addend.getAccount())
            && augend.getLevel1TagName().equals(addend.getLevel1TagName());
    }

    @Override
    public OnlinePositionKey getAggregateKeyForItem(RtfOnlinePosition op) {
        return new OnlinePositionKey(op.getReconBloombergId(), op.getProductCode(), op.getAccount(), op.getLevel1TagName(), null, null, null, null, null);
    }
}
