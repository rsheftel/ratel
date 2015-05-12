package com.fftw.sbp;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Store the sequence number and sent messages.
 *
 * This allows us to recover in the event of a network error and the communication link
 * was dropped.  By store the messages sent, the target/receiver can request a resend.
 *
 * Implementations do not retain state after a restart.
 */
public interface MessageStore<E> {

    /**
     * The time the store was created
     *
     * @return
     */
    DateTime getLoadTime();

    /**
     * Get the next sequence number for the sender.
     *
     * @return
     */
    int getNextSenderSeqNum();

    /**
     * Get the next sequence number for the target/receiver
     * @return
     */
    int getNextReceiverSeqNum();

    /**
     * Set the next sequence number for the sender
     *
     * @param next
     */
    void setNextSenderSeqNum(int next);

    /**
     * Set the next sequence number for the target/receiver.
     *
     * @param next
     */
    void setNextReceiverSeqNum(int next);

    /**
     * Increment the next sequence number for the sender.
     *
     */
    void incrementNextSenderSeqNum();

    /**
     * Increment the next sequence number for the target/receiver.
     *
     */
    void incrementNextReceiverSeqNum();

    /**
     * Reset the state.
     *
     * Sequence numbers are reset and stored messages are cleared.
     */
    void reset();

    void setMessage(int sequenceNumber, E message);

    List<E> getMessages(int startSequenceNumber, int endSequenceNumber);

    E getMessage(int sequenceNumber);

}
