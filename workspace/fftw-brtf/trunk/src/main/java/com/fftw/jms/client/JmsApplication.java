package com.fftw.jms.client;

import com.fftw.bloomberg.PositionRecord;

import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.JMSException;

/**
 *
 */
public interface JmsApplication {

    void fromQueue(TextMessage txtMsg, Session jmsSession) throws JMSException;

    String createPositionMessage(PositionRecord positionRecord);

    String createPurgeCommandMessage(PositionRecord positionRecord);

    String createHeartBeatMessage();
}
