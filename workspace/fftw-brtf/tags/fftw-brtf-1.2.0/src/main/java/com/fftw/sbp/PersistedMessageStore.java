package com.fftw.sbp;

/**
 * Message stores that persist over a system restart.
 *
 */
public interface PersistedMessageStore extends MessageStore {

    /**
     * Refresh the state from the persistant store.
     *
     */
    void refreshFromStore();
    
}
