package com.fftw.metadb.service;

public interface LiveSubscriber
{
    boolean subscribe(String topic, LiveListener liveListener) throws Exception;
    void unsubscribe(String topic, LiveListener liveListener) throws Exception;
}
