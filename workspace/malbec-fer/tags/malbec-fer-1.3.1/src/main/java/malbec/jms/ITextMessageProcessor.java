package malbec.jms;

import javax.jms.TextMessage;

public interface ITextMessageProcessor {

    void onTextMessage(TextMessage message);
}
