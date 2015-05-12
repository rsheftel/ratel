package com.fftw.sbp;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class MemoryMessageStore<E> implements MessageStore<E> {

    private Map<Integer, E> messageStore = new HashMap<Integer, E>();
    private DateTime loadTime;

    private int nextSenderSeqNum;

    private int nextReceiverSeqNum;

    public MemoryMessageStore() {
        loadTime = new DateTime();
    }

    public DateTime getLoadTime() {
        return loadTime;
    }

    public int getNextSenderSeqNum() {
        return nextSenderSeqNum;
    }

    public int getNextReceiverSeqNum() {
        return nextReceiverSeqNum;
    }

    public void setNextSenderSeqNum(int next) {
        synchronized (this) {
            this.nextSenderSeqNum = next;
        }
    }

    public void setNextReceiverSeqNum(int next) {
        synchronized (this) {
            this.nextReceiverSeqNum = next;
        }
    }

    public void incrementNextSenderSeqNum() {
        synchronized (this) {
            nextSenderSeqNum++;
        }
    }

    public void incrementNextReceiverSeqNum() {
        synchronized (this) {
            nextReceiverSeqNum++;
        }
    }

    public void reset() {
        synchronized (this) {
            setNextReceiverSeqNum(1);
            setNextSenderSeqNum(1);
            messageStore = new HashMap<Integer, E>();
        }
    }

    public void setMessage(int sequenceNumber, E message) {
        messageStore.put(sequenceNumber, message);
    }

    public E getMessage(int sequenceNumber) {
        return messageStore.get(sequenceNumber);
    }

    public List<E> getMessages(int startSequenceNumber, int endSequenceNumber) {
        List<E> returnedMessages = new ArrayList<E>((endSequenceNumber - startSequenceNumber) + 1);
        for (int i = startSequenceNumber; i <= endSequenceNumber; i++) {
            returnedMessages.add(messageStore.get(i));
        }
        return returnedMessages;
    }
}
