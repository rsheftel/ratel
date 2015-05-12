package com.fftw.positions.cache;

import com.fftw.bloomberg.OnlinePositionKey;
import com.fftw.bloomberg.types.BBProductCode;

public class ExcelOnlineRecordKey extends OnlinePositionKey {

    public ExcelOnlineRecordKey(String securityId, BBProductCode productCode, String account, String level1TagName,
        String level2TagName, String level3TagName, String level4TagName) {
        super(securityId, productCode, account, level1TagName, level2TagName, level3TagName, level4TagName, null, null);
    }

}
