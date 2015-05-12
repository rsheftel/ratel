package com.fftw.sbp;

/**
 * An ID that represents a protocol session.
 */
public interface ProtocolSessionID {

    /**
     * Return a representation of the id as a string.
     *
     * The string should contain enough information to reconstruct the ProtocolSessionID.
     *
     * @return
     */
    String idAsString();

    /**
     * Create a ProtocolSessionID from the string.
     *
     *
     * @param strID
     * @return
     */
    void populateFromString(String strID);

}
