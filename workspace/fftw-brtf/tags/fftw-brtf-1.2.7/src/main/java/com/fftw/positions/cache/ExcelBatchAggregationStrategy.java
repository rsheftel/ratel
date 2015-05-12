package com.fftw.positions.cache;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.batch.messages.BatchPosition;

/**
 * Implement logic for the Legacy Excel watcher.
 * 
 * @deprecated This should not be used, but is since we have not migrated away from the old
 * aggregation of the Excel spreadsheet.
 */
@Deprecated
public class ExcelBatchAggregationStrategy extends AbstractBatchPositionAggregationStrategy {

    @Override
    public boolean canAggregate(BatchPosition augend, BatchPosition addend) {

        boolean requiredFields = augend.getSecurityId().equals(addend.getSecurityId())
            && augend.getAccount().equals(addend.getAccount());

        boolean optionalFields = equal(augend.getLevel1TagName(), addend.getLevel1TagName())
            && equal(augend.getLevel2TagName(), addend.getLevel2TagName())
            && equal(augend.getLevel3TagName(), addend.getLevel3TagName())
            && equal(augend.getLevel4TagName(), addend.getLevel4TagName());

        return requiredFields && optionalFields;
    }

    @Override
    public PositionKey getAggregateKeyForItem(BatchPosition item) {
        return new ExcelBatchPositionKey(item.getSecurityId(), item.getProductCode(), item.getAccount(), item
            .getLevel1TagName(), item.getLevel2TagName(), item.getLevel3TagName(), item.getLevel4TagName());
    }

    @Override
    public PositionKey getAggregateKeyForKey(PositionKey key) {
        return new ExcelBatchPositionKey(key.getSecurityId(), key.getProductCode(), key.getAccount(), key
            .getLevel1TagName(), key.getLevel2TagName(), key.getLevel3TagName(), key.getLevel4TagName());
    }

}
