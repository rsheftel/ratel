package amazon;

import static amazon.AmazonSqsRequest.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;
import static util.Times.*;

import java.util.*;

import util.*;

public class SqsQ {

    private final String name;
    private String urlDoNotReference;
    private long lastDeleteTime;

    public SqsQ(String name) {
        this.name = name;
    }

    private String send(String message) {
        Tag response = response(url(), "SendMessage", map(
            "MessageBody", message
        ));
        return response.child("SendMessageResult").text("MessageId");
    }
    
    public String send(Object deserialized) {
    	return send(serialize(deserialized));
    }

    private synchronized String url() {
        if (urlDoNotReference == null) {
            if(lastDeleteTime != 0) {
                double secondsToCreationOk = 60.0 - reallySecondsSince(lastDeleteTime);
                if(secondsToCreationOk > 0.0) {
                    secondsToCreationOk++;
                    info("waiting " + secondsToCreationOk + " seconds to recreate queue");
                    sleep((long) secondsToCreationOk * 1000);
                }
            }
            Tag response = response("http://queue.amazonaws.com/", "CreateQueue", map(
                "QueueName", name
            ));
            urlDoNotReference = response.child("CreateQueueResult").text("QueueUrl");
        }
        return urlDoNotReference;
    }
    
    public static List<String> listQueues(String prefix) {
        Tag response = response("http://queue.amazonaws.com/", "ListQueues", map(
            "QueueNamePrefix", prefix
        ));
        List<String> result = empty();
        for (Tag child : response.child("ListQueuesResult").children("QueueUrl"))
            result.add(child.text());
        return result;
    }

    static class ReceiveMessageResponse {

        private final Tag response;
        private final SqsQ owner;

        public ReceiveMessageResponse(SqsQ owner, Tag result) {
            this.owner = owner;
            this.response = result;
        }

        public boolean isSuccess() {
            return response.hasChild("ReceiveMessageResult");
        }

        private Tag resultTag() {
            return response.child("ReceiveMessageResult");
        }

        public List<Message> messages() {
            List<Message> result = empty();
            for(Tag child : messageTags())
                result.add(new Message(owner, child));
            return result;
        }

        public boolean hasMessage() {
            return hasContent(messageTags());
        }

        private List<Tag> messageTags() {
            return resultTag().children("Message");
        }
        
        @Override public String toString() {
            return response.toString();
        }
    }

    public List<Message> messages(int timeoutMillis, int waitTimeMillis, int maxMessages, Integer visibilityTimeout) {
        int attempts = 0;
        int numAttempts = timeoutMillis / waitTimeMillis;
        while (attempts++ < numAttempts) {
            Map<String, String> params = map("QueueName", name, "MaxNumberOfMessages", maxMessages + "");
            if (visibilityTimeout != null) params.put("VisibilityTimeout", visibilityTimeout + "");
            Tag response = response(url(), "ReceiveMessage", params);
            ReceiveMessageResponse result = new ReceiveMessageResponse(this, response);
            if (result.isSuccess() && result.hasMessage()) return result.messages();
            sleep(waitTimeMillis);
        }
        return empty();
    }

    public List<Message> messagesBlocking() {
        return messagesBlocking(10, null);
    }
    
    public List<Message> messagesBlocking(int maxMessages, Integer visibilityTimeout) {
        int waitTime = 50;
        while(true) {
            List<Message> result = messages(waitTime, waitTime, maxMessages, visibilityTimeout);
            if(hasContent(result)) return result;
            if(waitTime < 10000) waitTime *= 2;
        }
    }

    public void drain() {
        for(
            List<Message> messages = messages(1000, 100, 10, null); 
            hasContent(messages); 
            messages = messages(1000, 100, 10, null)
        )
            for (Message message : messages)
                message.delete();
    }

    public void delete(String handle) {
        response(url(), "DeleteMessage", map("ReceiptHandle", handle));
    }

    public Message message() {
        List<Message> messages = messages(1000, 100, 1, null);
        if(isEmpty(messages))
            bomb("no messages to receive");
        return the(messages);
    }

    public void destroyDANGEROUS() {
        Map<String, String> empty = emptyMap();
        response(url(), "DeleteQueue", empty);
        lastDeleteTime = nowMillis();
        urlDoNotReference = null;
    }

    public String name() {
        return name;
    }

    public int size() {
        String url = url();
        info(url);
        Tag response = response(url, "GetQueueAttributes", map("AttributeName", "ApproximateNumberOfMessages"));
        return Integer.parseInt(response.child("GetQueueAttributesResult").child("Attribute").text("Value"));
    }

}
