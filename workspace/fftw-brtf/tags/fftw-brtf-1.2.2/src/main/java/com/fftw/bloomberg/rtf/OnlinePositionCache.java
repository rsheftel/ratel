package com.fftw.bloomberg.rtf;

import java.util.Map;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;
import com.fftw.util.AbstractGenericCache;

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

    public OnlinePositionKey getKeyForItem(RtfOnlinePosition value) {
        return new OnlinePositionKey(value.getSecurityId(), value.getAccount(), value.getLevel1TagName(),
                value.getLevel2TagName(), value.getLevel3TagName(), value.getLevel4TagName(), value.getPrimeBroker());
    }

}