package com.fftw.bloomberg;

/**
 *
 */
public interface PositionRecord {

    /**
     * Return a string representing the type of position.
     *
     * Either batch of online.
     * @return
     */
    String getPositionType();

    /**
     * Creates a string that can be sent over JMS.
     *  
     * @param delimiter
     * @return
     */
    String toTextMessage(String delimiter);
}
