package amazon;

import static util.Objects.*;
import util.*;

public class Message {

    private final Tag message;
    private final SqsQ owner;

    public Message(SqsQ owner, Tag message) {
        this.owner = owner;
        this.message = message;
    }

    private String text() {
        return message.text("Body");
    }
    
    public Object object() {
        return deserialize(text());
    }

    public void delete() {
        owner.delete(message.text("ReceiptHandle"));
    }

    public SqsQ replyQueue() {
        return replyQueue(message.text("MessageId"));
    }

    public static SqsQ replyQueue(String messageId) {
        return new SqsQ("reply_" + messageId);
    }
    
    @Override public String toString() {
        return message.toString();
    }
    
}