package mail;

import javax.mail.*;
import javax.mail.internet.*;

public class RealEmailer extends Emailer {
    
	@Override
	public void send(MimeMessage msg) throws MessagingException {
        Transport.send(msg);
	}
	
	@Deprecated
	public static void use() {
	    Emailer.setCurrent(new RealEmailer());
	}

}
