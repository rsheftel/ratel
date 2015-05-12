package com.fftw.metadb.service;

/**
 * 
 */
public interface LiveSubscriber
{
    boolean subscribe(String name, LiveListener liveListener) throws Exception;
    void unsubscribe(String name, LiveListener liveListener) throws Exception;
}
