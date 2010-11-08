package org.ratel.mail;

import static org.ratel.mail.EmailAddress.*;
import static org.ratel.util.Arguments.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;

import java.io.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.ratel.util.*;
import org.ratel.file.*;

public class Email {

    private static final List<String> TYPES = list("problem", "notification");
    private final String subject;
    private String message;
    private final Emailer emailer;
    List<QFile> attachments = empty();

    public Email(Emailer emailer, String subject, String message) {
        this.emailer = emailer;
        this.subject = subject;
        this.message = message;
    }
    
    private static Email email(String subject, String message) {
        return new Email(Emailer.current(), subject, message);
    }
    
    public static Email problem(String subject, String message) {
        return email("PROBLEM: " + subject, message);
    }
    
    public static Email notification(String subject, String message) {
        return email("info: " + subject, message);
    }
    
    public static Email trade(String subject, String message) {
        return email("TRADE: " + subject, message);
    }

    public void sendTo(EmailAddress address) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "nymail2.fftw.com");
        props.put("mail.from", "quantys_system@malbecpartners.com");
        Session session = Session.getInstance(props, null);

        try {
            MimeMessage msg = new MimeMessage(session);
            address.addTo(msg);
            msg.setFrom();
            String theSubject = Systematic.isDevDb() ? "DEV: " : "";
            theSubject += subject;
            msg.setSubject(theSubject);
            msg.setSentDate(new Date());
            addContent(msg);
            emailer.send(msg);
            Log.info("sent to " + address + " subject:" + theSubject);
        } catch (MessagingException e) {
            throw bomb("failed to send to " + address + " message\n" + message, e);
        } catch (IOException e) {
            throw bomb("failed to send to " + address + "couldn't add attachments\nmessage\n" + message, e);
        }

    }

    private void addContent(MimeMessage msg) throws MessagingException, IOException {
        if (attachments.isEmpty()) {
            msg.setText(message);
            return;
        }
        Multipart stuff = new MimeMultipart();
        MimeBodyPart body = new MimeBodyPart();
        body.setText(message);            
        stuff.addBodyPart(body);
        for (QFile file : attachments) {
            MimeBodyPart part = new MimeBodyPart();
            part.attachFile(file.file());
            stuff.addBodyPart(part);
        }
        msg.setContent(stuff);    
    }

    public void append(String text) {
        message += text + "\n";
    }

    public void append(char c) {
        message += c + "";
    }
    
    public boolean hasContent() {
        return Strings.hasContent(message);
    }

    public void sendTo(String failureAddresses) {
        sendTo(new EmailAddress(failureAddresses));
    }

    public String content() {
        return message;
    }

    public void attach(QFile file) {
        bombUnless(file.exists(), "file\n" + file.path() + "\ncannot be attached.");
        attachments.add(file);
    }

    public static void requireValidAddress(String string, String usage) {
        bombUnless(string.matches(".*@.*"), usage);
    }

    public static void main(String[] in) throws IOException {
        doNotDebugSqlForever();
        Arguments args = arguments(in, list("type", "subject", "to", "content"));
        String type = args.get("type", "problem");
        String subject = args.get("subject");
        String to = args.get("to", TEAM.address());
        String content = args.get("content", "");
        bombUnless(TYPES.contains(type), "allowed -type args are " + TYPES);
        Email email = type.equals("problem") ? problem(subject, "") : notification(subject, "");
        if (Strings.isEmpty(content)) addStdIn(email);
        else email.append(content);
        email.sendTo(to);
    }

    private static void addStdIn(Email email) throws IOException {
        BufferedInputStream buf = new BufferedInputStream(System.in);
        int c;
        while ((c = buf.read()) != -1)
            email.append((char) c);
        buf.close();
    }

}
