package util;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSender {

    private static final String SENDER = "mail.from";
    private static final String RECIPIENT = "mail.to";

    public final Logger log = LoggerFactory.getLogger(getClass().getName());

    private Properties props;

    public EmailSender(Properties emailSettings) {
        props = emailSettings;

        // expected mail configuration
        // mail.host=mail.fftw.com
        // mail.from=Alert@fftw.com

    }

    public boolean sendMessage(String subject, String messageBody) {
        // get the address info
        try {
            InternetAddress fromAddress = new InternetAddress(props.getProperty(SENDER));
            Address[] toAddress = generatedRecipients(props.getProperty(RECIPIENT));

            Session mailSession = Session.getDefaultInstance(props);
            MimeMessage mailMessage = new MimeMessage(mailSession);
            
            mailMessage.setFrom(fromAddress);
            mailMessage.setRecipients(Message.RecipientType.TO, toAddress);
            mailMessage.setSubject(subject);
            mailMessage.setText(messageBody);
            Transport.send(mailMessage);
            return true;
        } catch (AddressException e) {
            log.error("Unable to set from address", e);
        } catch (MessagingException e) {
            log.error("Unable to send email", e);
        }
        
        return false;

    }

    private Address[] generatedRecipients(String receipientString) {
        String[] addressStrings = receipientString.split(",");
        Address[] addresses = new Address[addressStrings.length];

        for (int i =0; i < addressStrings.length; i++) {
            try {
                addresses[i] = new InternetAddress(addressStrings[i]);
            } catch (AddressException e) {
                log.warn("Unable to parse email address - " + addressStrings[i], e);
            }
        }

        return addresses;
    }

}
