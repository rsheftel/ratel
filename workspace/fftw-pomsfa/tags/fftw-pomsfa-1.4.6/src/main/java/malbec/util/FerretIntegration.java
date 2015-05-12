package malbec.util;

import java.util.Properties;

import com.fftw.util.Emailer;

public class FerretIntegration
{
    private FerretIntegration() {
        // prevent
    }
    
    public static Emailer createEmailer (EmailSettings emailSettings)
    {
        Properties props = emailSettings.getAsProperties();
        props.setProperty("mail.it.to", emailSettings.getErrorToList());
        
        return new Emailer(props);
    }
}
