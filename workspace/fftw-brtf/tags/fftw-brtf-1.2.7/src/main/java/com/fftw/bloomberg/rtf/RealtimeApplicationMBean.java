package com.fftw.bloomberg.rtf;

/**
 * JMX Real-time application interface
 */
public interface RealtimeApplicationMBean {

    boolean isEndOfDayReceived();

    /**
     * The next trading day in
     *
     * @param nextTradingDay
     */
    boolean forceReload(String nextTradingDay);

    String getCurrentTradingDay();

    String getPreviousTradingDay();

    String getNextTradingDay();

}
