package com.fftw.bloomberg.rtf;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class OnlinePositionCache extends AbstractGenericCache<OnlinePositionKey, RtfOnlinePosition> {

    public OnlinePositionCache() {

    }

    public OnlinePositionCache(Map<OnlinePositionKey, RtfOnlinePosition> newPositions) {
        super(newPositions);
    }

    public RtfOnlinePosition addOnlinePosition(RtfOnlinePosition op) {
        return addCacheItem(getKeyForItem(op), op);
    }

    public RtfOnlinePosition updateOnlinePosition(RtfOnlinePosition op) {
        return addCacheItem(getKeyForItem(op), op);
    }

    public RtfOnlinePosition aggregateOnlinePosition(RtfOnlinePosition op) {

        OnlinePositionKey key = getKeyForItem(op);

        RtfOnlinePosition cachedBp = getCacheItem(key);
        if (cachedBp != null) {
            op = cachedBp.aggregate(op);
        }
        return addCacheItem(key, op);
    }

    public Collection<RtfOnlinePosition> aggregatedValues() {

        RtfOnlinePosition aggregatedPosition = null;
        for (RtfOnlinePosition position : values()) {
            if (aggregatedPosition == null) {
                aggregatedPosition = position;
            } else {
                aggregatedPosition = aggregatedPosition.aggregate(position);
            }
        }

        List<RtfOnlinePosition> values = new ArrayList<RtfOnlinePosition>(1);
        values.add(aggregatedPosition);

        return values;
    }

    public OnlinePositionKey getKeyForItem(RtfOnlinePosition value) {
        return new OnlinePositionKey(value.getSecurityId(), value.getAccount(), value.getLevel1TagName(),
                value.getLevel2TagName(), value.getLevel3TagName(), value.getLevel4TagName(), value.getPrimeBroker());
    }

}