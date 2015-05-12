package com.fftw.positions.cache;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.types.BBProductCode;

/**
 * Legacy key for Excel.
 * 
 */
public class ExcelBatchPositionKey extends PositionKey {

    public ExcelBatchPositionKey(String securityId, BBProductCode productCode, String account, String level1TagName,
        String level2TagName, String level3TagName, String level4TagName) {

        super(securityId, productCode, account, level1TagName, level2TagName, level3TagName, level4TagName, null, null);
    }

}
