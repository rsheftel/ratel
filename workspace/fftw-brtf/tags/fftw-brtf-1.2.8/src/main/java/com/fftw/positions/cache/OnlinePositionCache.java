package com.fftw.positions.cache;

import java.util.Collection;
import java.util.Map;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.util.AbstractGenericCache;

/**
 * Cache for the fully specified key of Online Position Records.
 * 
 */
public class OnlinePositionCache extends AbstractGenericCache<OnlinePositionKey, RtfOnlinePosition> {

    public OnlinePositionCache() {}

    public OnlinePositionCache(Map<OnlinePositionKey, RtfOnlinePosition> newPositions) {
        super(newPositions);
    }

    public OnlinePositionCache(Collection<RtfOnlinePosition> newPositions) {
        for (RtfOnlinePosition position : newPositions) {
            addOnlinePosition(position);
        }
    }

    public RtfOnlinePosition addOnlinePosition(RtfOnlinePosition op) {
        return addCacheItem(getKeyForItem(op), op);
    }

    public RtfOnlinePosition updateOnlinePosition(RtfOnlinePosition op) {
        return addCacheItem(getKeyForItem(op), op);
    }
    
    public OnlinePositionKey getKeyForItem(RtfOnlinePosition value) {
        return new OnlinePositionKey(value.getSecurityId(), value.getProductCode(), value.getAccount(), value.getLevel1TagName(),
            value.getLevel2TagName(), value.getLevel3TagName(), value.getLevel4TagName(), value
                .getPrimeBroker(), value.getOpenPosition());
    }

}