package org.ratel.mail;

import static org.ratel.util.Errors.*;
import static org.ratel.util.Asserts.*;
import static org.ratel.util.Objects.*;

import java.io.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.ratel.util.*;

import junit.framework.*;

public class MockEmailer extends Emailer {

    public MockEmailer() {
        Emailer.setCurrent(this);
        disallowMessages();
    }
    
    public class Sent {
        public Sent(String subject, String message, Address[] sentTo) {
            this.subject = subject;
            this.message = message;
            for (Address address : sentTo) 
                this.sentTo.add(address.toString());
        }
        public final String subject;
        public final String message;
        public final List<String> sentTo = empty();
        public void hasContent(String matches) {
            assertMatches(matches, message);
        }
        public void hasReceiver(String matches) {
            assertMatches(matches, sentTo.toString());
        }
        public void hasReceiver(EmailAddress nobody) {
            hasReceiver(nobody.address());
        }
        public void hasSubject(String matches) {
            try {
                assertMatches(matches, subject);
            } catch (Throwable e) {
                throw bomb("unexpected subject found in " + this, e);
            }
        }
        
        @Override public String toString() {
            return "To: " + sentTo + "\nSubject: " + subject + " \n" + message+ "\n";
        }
        
    }
    
    private final List<Sent> sent = empty();
    private final List<MimeMessage> mimes = empty(); // but who likes a mime anyway
    private boolean disallowMessages = false;
    
    @Override
    public void send(MimeMessage msg) throws MessagingException {
        try {
            mimes.add(msg);
            sent.add(new Sent(msg.getSubject(), text(msg), msg.getAllRecipients()));
            if (disallowMessages) requireEmpty();
        } catch (IOException e) {
            throw bomb("", e);
        }
    }

    private String text(MimeMessage msg) throws IOException, MessagingException {
        if (msg.getContent() instanceof MimeMultipart) 
            return "see mime content part or reimplement this";
        return (String)msg.getContent();
    }

    public void disallowMessages() {
        disallowMessages = true;
        clear();
    }
    
    public void allowMessages() {
        disallowMessages = false;
    }
    
    public Sent sent() {
        requireSent(1);
        return the(sent);
    }

    public void clear() {
        sent.clear();
    }

    public void requireEmpty() {
        requireSent(0);
    }

    public void requireSent(int expected) throws AssertionFailedError {
        if (sent.size() == expected) return;
        String messages = "expected " + expected + " messages but got " + sent.size() + "\n";
        for (Sent m : sent) 
            try { messages += m; } 
            catch (Exception e) { throw bomb("failed getting content", e); }
        throw new AssertionFailedError(sent.isEmpty() ? "no messages sent" : "unexpected messages sent: \n" + messages);
    }

    public String message() {
        try {
            return the(sent()).message;
        } catch (Exception e) {
            throw bomb("", e);
        }
    }

    public Sent first() {
        return Objects.first(sent);
    }

    public MimeMessage sentMime() {
        return the(mimes);
    }

    public void reset() {
        Emailer.setCurrent(new RealEmailer());
    }

}
