package com.fftw.bloomberg.rtf.messages;

import static com.fftw.util.BigMath.add;

import java.math.BigDecimal;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.util.IAggregationStrategy;

public class RtfOnlinePositionAggregationStrategy implements IAggregationStrategy<OnlinePositionKey,RtfOnlinePosition> {

    @Override
    public RtfOnlinePosition aggregate(RtfOnlinePosition augend, RtfOnlinePosition addend) {
        if (!canAggregate(augend, addend)) {
            throw new IllegalArgumentException("OnlinePositions cannot be aggregated, keys do not match");
        }

        // Get a copy to change - no side effects!
        RtfOnlinePosition augendCopy = augend.copy();

        augendCopy.setOpenPosition(add(augendCopy.getOpenPosition(), addend.getOpenPosition()));
        augendCopy.setRealizedPL(add(augendCopy.getRealizedPL(), addend.getRealizedPL()));
        augendCopy.setCurrentPosition(add(augendCopy.getCurrentPosition(), addend.getCurrentPosition()));

        augendCopy.setTotalBuyVolume(add(augendCopy.getTotalBuyVolume(), addend.getTotalBuyVolume()));
        augendCopy.setTotalSellVolume(add(augendCopy.getTotalSellVolume(), addend.getTotalSellVolume()));

        augendCopy.setTotalNumberOfBuys(augendCopy.getTotalNumberOfBuys() + addend.getTotalNumberOfBuys());
        augendCopy.setTotalNumberOfSells(augendCopy.getTotalNumberOfSells() + addend.getTotalNumberOfSells());

        // wipe these out as we cannot deal with them
        augendCopy.setCurrentAverageCost(BigDecimal.ZERO);
        augendCopy.setOpenAverageCost(BigDecimal.ZERO);

        // ensure we have the contract size
        augendCopy.setContractSize(addend.getContractSize());

        return augendCopy;
    }

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

    private static boolean isEmptyOrNull(String s) {
        return (s == null || s.length() == 0);
    }

    private static boolean equal(String a, String b) {
        if (isEmptyOrNull(a) && isEmptyOrNull(b)) {
            return true;
        } else if (isEmptyOrNull(a) || isEmptyOrNull(b)) {
            return false;
        } else {
            return a.equals(b);
        }
    }

    @Override
    public OnlinePositionKey getAggregateKeyForItem(RtfOnlinePosition item) {
        return new OnlinePositionKey(item.getSecurityId(), item.getAccount(), item.getLevel1TagName(),
            item.getLevel2TagName(), item.getLevel3TagName(), item.getLevel4TagName());
    }
    
    
}
