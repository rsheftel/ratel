package com.fftw.bloomberg.rtf;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.rtf.messages.RtfOnlinePosition;

import java.util.Map;

/**
 *
 */
public class OnlinePositionCache extends GenericCache<PositionKey, RtfOnlinePosition> {


    public OnlinePositionCache() {

    }

    public OnlinePositionCache(Map<PositionKey, RtfOnlinePosition> newPositions) {
        super(newPositions);
    }

    public RtfOnlinePosition addOnlinePosition(RtfOnlinePosition op) {

        PositionKey key = new PositionKey(op.getSecurityId(), op.getAccount(), op.getLevel1TagName(),
                op.getLevel2TagName(), op.getLevel3TagName(), op.getLevel4TagName());
        return addCacheItem(key, op);
    }

    public RtfOnlinePosition updateOnlinePosition(RtfOnlinePosition op) {
        PositionKey key = new PositionKey(op.getSecurityId(), op.getAccount(), op.getLevel1TagName(),
                op.getLevel2TagName(), op.getLevel3TagName(), op.getLevel4TagName());


        return addCacheItem(key, op);
    }

}