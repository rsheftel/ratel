package com.fftw.positions.cache;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.types.BBProductCode;

public class ReconOnlineRecordKey extends OnlinePositionKey {

    public ReconOnlineRecordKey(String securityId, BBProductCode productCode, String account) {
        super(securityId, productCode, account, null, null, null, null, null, null);
    }

}
