package com.fftw.metadb.domain;

import com.fftw.metadb.service.LiveSubscriber;
import com.fftw.metadb.util.TextUtil;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * Base class for <code>LiveSubscribers</code>.
 *
 * Logic that can be reused by subscribers will be here.
 */
public abstract class AbstractLiveSubscriber implements LiveSubscriber {
    private static final String INVALID_DATA = "-99";

    protected Map<String, String> createInvalidDataRecord() {
        Map<String, String> fieldMap = new HashMap<String, String>();

        fieldMap.put("HighPrice", INVALID_DATA);
        fieldMap.put("LastPrice", INVALID_DATA);
        fieldMap.put("LastVolume", INVALID_DATA);
        fieldMap.put("LowPrice", INVALID_DATA);
        fieldMap.put("OpenPrice", INVALID_DATA);
        fieldMap.put("LowPrice", INVALID_DATA);
        fieldMap.put("Timestamp", TextUtil.formatDate(new Date()));

        return fieldMap;
    }
}
