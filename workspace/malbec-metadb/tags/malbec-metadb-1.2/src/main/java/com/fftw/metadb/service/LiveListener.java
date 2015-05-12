package com.fftw.metadb.service;

import java.util.Map;

/**
 * Any class that wants to receive messages from <code>LiveSubscribers</code> will implement this interface.
 *
 * The data will be in a fieldName/fieldValue pair.  The key and value are <code>String</code>s
 */
public interface LiveListener
{
    /**
     * Data as strings in a key/value pair.
     * 
     * @param dataMap
     */
    void onData(Map<String, String> dataMap);
}
