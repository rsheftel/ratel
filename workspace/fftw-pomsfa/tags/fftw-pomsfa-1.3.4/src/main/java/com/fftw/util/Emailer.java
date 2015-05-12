package com.fftw.util;

import java.util.List;
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

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.util.CmfRecordFormatter;

public class Emailer
{
    private static final Log logger = LogFactory.getLog(Emailer.class);

    private static Properties fMailServerConfig = new Properties();

    public Emailer (Properties configProperties)
    {
        fMailServerConfig = configProperties;
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
     * Send a message to the distribution lists.
     * 
     * @param subject
     * @param tradeRecord
     * @param includeTech
     *            whether or not this is technology related error and should
     *            include the technology team.
     */
    public void emailErrorMessage (String subject, CmfTradeRecord tradeRecord, boolean includeTech)
    {
        // Use the default toString implementation
        StringBuilder sb = new StringBuilder(1000);
        sb.append(subject).append("\n\n");
        sb.append(CmfRecordFormatter.formatForEmail(tradeRecord));

        try
        {
            if (includeTech)
            {
                sendEmail(subject, sb.toString(), "mail.it.to");
            }
            else
            {
                sendEmail(subject, sb.toString(), null);
            }
        }
        catch (Exception e)
        {
            logger.error("Unable to send email.", e);
        }
    }

    public void emailErrorMessage (String subject, String messageBody, boolean includeTech)
    {
        try
        {
            if (includeTech)
            {
                sendEmail(subject, messageBody, "mail.it.to");
            }
            else
            {
                sendEmail(subject, messageBody, null);
            }
        }
        catch (Exception e)
        {
            logger.error("Unable to send email.", e);
        }
    }

    public void emailErrorMessage (String subject, List<String> messages, boolean includeTech)
    {
        try
        {
            StringBuilder sb = new StringBuilder();

            for (String message : messages)
            {
                sb.append(message);
                sb.append("\n");
            }

            if (includeTech)
            {
                sendEmail(subject, sb.toString(), "mail.it.to");
            }
            else
            {
                sendEmail(subject, sb.toString(), null);
            }
        }
        catch (Exception e)
        {
            logger.error("Unable to send email.", e);
        }
    }
}
