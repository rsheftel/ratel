package org.ratel.mail;

import javax.mail.*;
import javax.mail.internet.*;

public abstract class Emailer {

    abstract void send(MimeMessage msg) throws MessagingException;

    private static Emailer current = new RealEmailer();

    public static Emailer current() {
        return current;
    }

    static void setCurrent(Emailer emailer) {
        current = emailer;
    }
}
