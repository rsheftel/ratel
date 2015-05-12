package com.fftw.positions.cache;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;

public class ExcelOnlineAggregationStrategy extends AbstractOnlinePositionAggregationStrategy {

    @Override
    public boolean canAggregate(RtfOnlinePosition augend, RtfOnlinePosition addend) {

        boolean requiredFields = augend.getSecurityId().equals(addend.getSecurityId())
            && augend.getAccount().equals(addend.getAccount());

        boolean optionalFields = equal(augend.getLevel1TagName(), addend.getLevel1TagName())
            && equal(augend.getLevel2TagName(), addend.getLevel2TagName())
            && equal(augend.getLevel3TagName(), addend.getLevel3TagName())
            && equal(augend.getLevel4TagName(), addend.getLevel4TagName());

        return requiredFields && optionalFields;
    }

    @Override
    public OnlinePositionKey getAggregateKeyForItem(RtfOnlinePosition item) {
        return new ExcelOnlineRecordKey(item.getSecurityId(), item.getProductCode(), item.getAccount(), item.getLevel1TagName(),
            item.getLevel2TagName(), item.getLevel3TagName(), item.getLevel4TagName());
    }

    @Override
    public OnlinePositionKey getAggregateKeyForKey(OnlinePositionKey key) {
        return new ExcelOnlineRecordKey(key.getSecurityId(), key.getProductCode(), key.getAccount(), key.getLevel1TagName(), key
            .getLevel2TagName(), key.getLevel3TagName(), key.getLevel4TagName());
    }

}
