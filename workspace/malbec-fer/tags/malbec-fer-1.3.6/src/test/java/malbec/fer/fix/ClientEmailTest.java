package malbec.fer.fix;

import static org.testng.Assert.assertTrue;
import malbec.AbstractBaseTest;
import malbec.util.EmailSender;
import malbec.util.EmailSettings;

import org.testng.annotations.Test;

/**
 * Test the email functions for the client
 */
public class ClientEmailTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testEmailOnDisconnect() {
        
        EmailSettings emailSettings = new EmailSettings();
        
        emailSettings.setErrorToAddress("Mfranz@fftw.com");
        emailSettings.setFromAddress("Test EMail<MFranz@fftw.com>");
        
        EmailSender sender = new EmailSender(emailSettings.getAsProperties());
        
        assertTrue(sender.sendMessage("UnitTest Message", "Sent via default properties"), "Unable to send email");
        assertTrue(sender.sendMessage("UnitTest Message", "Sent via emailsettings as properties", emailSettings.getAsProperties()), "Unable to send email");
        assertTrue(sender.sendMessage("UnitTest Message", "Sent via emailsettings", emailSettings), "Unable to send email");
    }
}
