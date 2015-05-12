package com.fftw.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Emailer
{
    private static final Log logger = LogFactory.getLog(Emailer.class);

    private static Properties fMailServerConfig = new Properties();

    static
    {
        fetchConfig();
    }

    /**
     * Send a single email.
     */
    public void sendEmail (String subject, String body, String strMailToPropertyName)
    {
        // Here, no Authenticator argument is used (it is null).
        // Authenticators are used to prompt the user for user
        // name and password.
        Session session = Session.getDefaultInstance(fMailServerConfig, null);
        MimeMessage message = new MimeMessage(session);

        try
        {
            String addresses = fMailServerConfig
                .getProperty(strMailToPropertyName == null ? "mail.to" : strMailToPropertyName);
            String[] toEmailAddresses = addresses.split(",");
            Address[] toAddresses = new InternetAddress[toEmailAddresses.length];
            for (int i = 0; i < toEmailAddresses.length; i++)
            {
                toAddresses[i] = new InternetAddress(toEmailAddresses[i]);
            }
            message.setFrom(new InternetAddress(fMailServerConfig.getProperty("mail.from")));
            message.addRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        }
        catch (MessagingException ex)
        {
            logger.error("Cannot send email. " + ex);
        }
    }

    public void sendEmailWithFileAttachments (String subject, String body, String[] arrFileNames,
        String strMailToPropertyName)
    {
        Session session = Session.getDefaultInstance(fMailServerConfig, null);
        MimeMessage message = new MimeMessage(session);

        try
        {
            String addresses = fMailServerConfig
                .getProperty(strMailToPropertyName == null ? "mail.to" : strMailToPropertyName);
            String[] toEmailAddresses = addresses.split(",");
            Address[] toAddresses = new InternetAddress[toEmailAddresses.length];
            for (int i = 0; i < toEmailAddresses.length; i++)
            {
                toAddresses[i] = new InternetAddress(toEmailAddresses[i]);
            }
            message.setFrom(new InternetAddress(fMailServerConfig.getProperty("mail.from")));
            message.addRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(subject);
            Multipart mp = new MimeMultipart();
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setText(body);            
            mp.addBodyPart(mbp);
            for (String strFileName : arrFileNames)
            {
                mbp = new MimeBodyPart();
                // attach the file to the message
                FileDataSource fds = new FileDataSource(strFileName);
                mbp.setDataHandler(new DataHandler(fds));
                mbp.setFileName(fds.getName());
                // create the Multipart and add its parts to it                
                mp.addBodyPart(mbp);
            }
            // add the Multipart to the message
            message.setContent(mp);
            Transport.send(message);
        }
        catch (MessagingException ex)
        {
            logger.error("Cannot send email. " + ex);
        }
    }

    /**
     * Allows the config to be refreshed at runtime, instead of requiring a
     * restart.
     */
    public static void refreshConfig ()
    {
        fMailServerConfig.clear();
        fetchConfig();
    }

    /**
     * Open a specific text file containing mail server parameters, and populate
     * a corresponding Properties object.
     */
    private static void fetchConfig ()
    {
        InputStream input = null;
        try
        {
            logger.info("Calling fetchConfig() to load mail.properties file...");
            input = Emailer.class.getResourceAsStream("/mail.properties");
            fMailServerConfig.load(input);
            logger.info("Mail.properties file loaded");
        }
        catch (IOException ex)
        {
            logger.error("Cannot open and load mail server properties file: " + ex);
        }
        catch(Exception ex) {
            logger.error("Problems loading mail.properties file: " + ex);
        }
        finally
        {
            logger.info("In finally clause...");
            try
            {
                if (input != null)
                {
                    logger.info("Closing mail.properties file inputstream");
                    input.close();
                }
            }
            catch (IOException ex)
            {
                logger.error("Cannot close mail server properties file: " + ex);
            }
        }
    }

    public static void main (String[] aArguments)
    {
        Emailer emailer = new Emailer();
        emailer.sendEmail("Testing 1-2-3", "blah blah blah", null);
    }
}
