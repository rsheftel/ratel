package org.ratel.mail;

import static org.ratel.mail.EmailAliasesTable.*;
import static org.ratel.util.Sequence.*;
import static org.ratel.util.Strings.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.ratel.db.*;
import org.ratel.db.columns.*;
public class EmailAddress {

    public static final EmailAddress TEAM = new EmailAddress("team");
    public static final EmailAddress US = new EmailAddress("us");
    public static final EmailAddress NOBODY = new EmailAddress("nobody@foo.com");
    
    
    private final String address;
    public EmailAddress(String address) {
        this.address = expanded(address);
    }
    
    public static String expanded(String name) {
        String[] names = name.split(",");
        for(int i : along(names))
            names[i] = EMAILS.munge(names[i]); 
        return join(",", names);
    }

    @Override public String toString() {
        return address;
    }

    public void addTo(MimeMessage msg) throws MessagingException {
        msg.setRecipients(Message.RecipientType.TO, address);
    }

    public Cell<?> in(StringColumn c) {
        return c.with(address);
    }

    public String address() {
        return address;
    }
}
